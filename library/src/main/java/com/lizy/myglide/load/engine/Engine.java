package com.lizy.myglide.load.engine;

import android.support.v4.util.Pools;
import android.util.Log;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.Priority;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.cache.DiskCache;
import com.lizy.myglide.load.engine.cache.DiskCacheAdapter;
import com.lizy.myglide.load.engine.cache.MemoryCache;
import com.lizy.myglide.load.engine.executor.GlideExecutor;
import com.lizy.myglide.request.ResourceCallback;
import com.lizy.myglide.util.LogTime;
import com.lizy.myglide.util.Util;
import com.lizy.myglide.util.pool.FactoryPools;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizy on 16-5-3.
 */
public class Engine implements EngineJobListener,
        MemoryCache.ResourceRemovedListener,
        EngineResource.ResourceListener {

    private static final int JOB_POOL_SIZE = 150;

    private static final String TAG = "Engine";

    private final MemoryCache cache;
    private final Map<Key, EngineJob> jobs;
    private final Map<Key, WeakReference<EngineResource<?>>> activeResources;
    private final EngineKeyFactory keyFactory;
    private final EngineJobFactory engineJobFactory;
    private final DecodeJobFactory decodeJobFactory;
    private final DecodeJob.DiskCacheProvider diskCacheProvider;

    public Engine(MemoryCache memoryCache,
                  DiskCache.Factory diskCacheFactory,
                  GlideExecutor diskCacheExecutor,
                  GlideExecutor sourceExecutor) {
        this(memoryCache, diskCacheFactory, diskCacheExecutor, sourceExecutor,
                null, null, null, null, null);
    }

    public Engine(MemoryCache memoryCache,
                  DiskCache.Factory diskCacheFactory,
                  GlideExecutor diskCacheExecutor,
                  GlideExecutor sourceExecutor,
                  Map<Key, EngineJob> jobs,
                  EngineKeyFactory keyFactory,
                  Map<Key, WeakReference<EngineResource<?>>> activeResources,
                  EngineJobFactory engineJobFactory,
                  DecodeJobFactory decodeJobFactory) {
        this.cache = memoryCache;
        this.diskCacheProvider = new LazyDiskCacheProvider(diskCacheFactory);

        if (keyFactory == null) {
            keyFactory = new EngineKeyFactory();
        }
        this.keyFactory = keyFactory;

        if (engineJobFactory == null) {
            engineJobFactory = new EngineJobFactory(diskCacheExecutor, sourceExecutor, this);
        }
        this.engineJobFactory = engineJobFactory;

        if (jobs == null) {
            jobs = new HashMap<>();
        }
        this.jobs = jobs;

        if (activeResources == null) {
            activeResources = new HashMap<>();
        }
        this.activeResources = activeResources;

        if (decodeJobFactory == null) {
            decodeJobFactory = new DecodeJobFactory(diskCacheProvider);
        }
        this.decodeJobFactory = decodeJobFactory;

        cache.setResourceRemovedListener(this);
    }


    public <R> LoadStatus load(
            GlideContext glideContext,
            Object model,
            Key signature,
            int width,
            int height,
            Class<R> transcodeClass,
            Class<?> resourceClass,
            Priority priority,
            DiskCacheStrategy diskCacheStrategy,
            Map<Class<?>, Transformation<?>> transformations,
            boolean isTransformationRequired,
            Options options,
            boolean isMemoryCacheable,
            ResourceCallback cb) {
        Util.assertMainThread();
        long startTime = LogTime.getLogTime();

        EngineKey key = keyFactory.build(model, signature, width, height, transformations,
                resourceClass, transcodeClass, options);

        EngineResource<?> cached = loadFromCache(key, isMemoryCacheable);
        if (cached != null) {
            cb.onResourceReady(cached, DataSource.MEMORY_CACHE);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("load resource form cache", startTime, key);
            }
            return null;
        }

        EngineResource<?> active = loadFromActiveResources(key, isMemoryCacheable);
        if (active != null) {
            cb.onResourceReady(active, DataSource.MEMORY_CACHE);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("load resource form active", startTime, key);
            }
            return null;
        }

        EngineJob<R> current = jobs.get(key);
        if (current != null) {
            current.addCallback(cb);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Added to existing load", startTime, key);
            }
            return new LoadStatus(cb, current);
        }

        EngineJob<R> engineJob = engineJobFactory.build(key, isMemoryCacheable);
        DecodeJob<R> decodeJob = decodeJobFactory.build(glideContext,
                model,
                key,
                signature,
                width,
                height,
                resourceClass,
                transcodeClass,
                priority,
                diskCacheStrategy,
                transformations,
                isTransformationRequired,
                options,
                engineJob);
        jobs.put(key, engineJob);
        engineJob.addCallback(cb);
        engineJob.start(decodeJob);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Started new load", startTime, key);
        }
        return new LoadStatus(cb, engineJob);
    }

    private EngineResource<?> loadFromActiveResources(EngineKey key, boolean isMemoryCacheable) {
        if (!isMemoryCacheable) {
            return null;
        }

        EngineResource<?> actived = null;
        WeakReference<EngineResource<?>> activeRef = activeResources.get(key);
        if (activeRef != null) {
            actived = activeRef.get();
            if (actived != null) {
                actived.acquire();
            } else {
                activeResources.remove(key);
            }
        }

        return actived;
    }

    private static void logWithTimeAndKey(String log, long startTime, Key key) {
        Log.v(TAG, log + " in " + LogTime.getElapsedMillis(startTime) + "ms, key: " + key);
    }

    private EngineResource<?> loadFromCache(Key key, boolean isMemoryCacheable) {
        if (!isMemoryCacheable) {
            return null;
        }
        EngineResource<?> cached = getEngineResourceFromCache(key);

        if (cached != null) {
            cached.acquire();
            activeResources.put(key, new ResourceWeakReference(key, cached, getReferenceQueue()));
        }

        return cached;
    }

    public void release(Resource resource) {
        Util.assertMainThread();
        if (resource instanceof EngineResource) {
            ((EngineResource)resource).release();
        } else {
            throw new IllegalArgumentException("Cannot release anything but an EngineResource");
        }
    }

    @Override
    public void onResourceReleased(Key key, EngineResource<?> resource) {
        Util.assertMainThread();
        activeResources.remove(key);
        if (resource.isCacheable()) {
            cache.put(key, resource);
        } else {
            //TODO:
//            resourceRecycler.recycle(resource);
        }
    }

    private static class ResourceWeakReference extends WeakReference<EngineResource<?>>{
        private final Key key;
        public ResourceWeakReference(Key key,
                                     EngineResource<?> r,
                                     ReferenceQueue<? super EngineResource<?>> q) {
            super(r, q);
            this.key = key;
        }
    }

    private ReferenceQueue<EngineResource<?>> getReferenceQueue() {
        //TODO:
        return new ReferenceQueue<>();
    }

    private EngineResource<?> getEngineResourceFromCache(Key key) {
        Resource<?> resource = cache.remove(key);
        final EngineResource result;
        if (resource == null) {
            result = null;
        } else if (resource instanceof EngineResource) {
            result = (EngineResource)resource;
        } else {
            result = new EngineResource(resource, true /*isCacheMemory*/);
        }
        return result;
    }

    @Override
    public void onEngineJobComplete(Key key, EngineResource<?> resource) {
        Util.assertMainThread();
        if (resource != null) {
            resource.setResourceListener(key, this);
            if (resource.isCacheable()) {
                activeResources.put(key, new WeakReference<EngineResource<?>>(resource));
            }
        }

        jobs.remove(key);
    }

    @Override
    public void onEngineJobCancelled(EngineJob engineJob, Key key) {
        Util.assertMainThread();
        EngineJob current = jobs.get(key);
        if (engineJob.equals(current)) {
            jobs.remove(key);
        }
    }

    @Override
    public void onResourceRemoved(Resource<?> removed) {

    }

    static class EngineJobFactory {
        private final GlideExecutor diskExecutor;
        private final GlideExecutor sourceExecutor;
        private final EngineJobListener listener;

        public EngineJobFactory(GlideExecutor diskExecutor,
                                GlideExecutor sourceExecutor,
                                EngineJobListener listener) {
            this.diskExecutor = diskExecutor;
            this.sourceExecutor = sourceExecutor;
            this.listener = listener;
        }

        private final Pools.Pool<EngineJob<?>> pool = FactoryPools.simple(JOB_POOL_SIZE,
                new FactoryPools.Factory<EngineJob<?>>() {
                    @Override
                    public EngineJob<?> create() {
                        return new EngineJob<Object>(sourceExecutor,
                                diskExecutor,
                                listener,
                                pool);
                    }
                });

        <R> EngineJob<R> build(Key key, boolean isCacheable) {
            EngineJob<R> engineJob = (EngineJob<R>) pool.acquire();
            return engineJob.init(key, isCacheable);
        }
    }

    // Visible for testing.
    static class DecodeJobFactory {
        private final DecodeJob.DiskCacheProvider diskCacheProvider;
        private final Pools.Pool<DecodeJob<?>> pool = FactoryPools.simple(JOB_POOL_SIZE,
                new FactoryPools.Factory<DecodeJob<?>>() {
                    @Override
                    public DecodeJob<?> create() {
                        return new DecodeJob<Object>(diskCacheProvider, pool);
                    }
                });
        private int creationOrder;

        DecodeJobFactory(DecodeJob.DiskCacheProvider diskCacheProvider) {
            this.diskCacheProvider = diskCacheProvider;
        }

        @SuppressWarnings("unchecked")
        <R> DecodeJob<R> build(GlideContext glideContext,
                               Object model,
                               EngineKey loadKey,
                               Key signature,
                               int width,
                               int height,
                               Class<?> resourceClass,
                               Class<R> transcodeClass,
                               Priority priority,
                               DiskCacheStrategy diskCacheStrategy,
                               Map<Class<?>, Transformation<?>> transformations,
                               boolean isTransformationRequired,
                               Options options,
                               DecodeJob.Callback<R> callback) {
            DecodeJob<R> result = (DecodeJob<R>) pool.acquire();
            return result.init(
                    glideContext,
                    model,
                    loadKey,
                    signature,
                    width,
                    height,
                    resourceClass,
                    transcodeClass,
                    priority,
                    diskCacheStrategy,
                    transformations,
                    isTransformationRequired,
                    options,
                    callback,
                    creationOrder++);
        }
    }

    public static class LoadStatus {
        private final EngineJob engineJob;
        private final ResourceCallback cb;

        public LoadStatus(ResourceCallback cb, EngineJob engineJob) {
            this.cb = cb;
            this.engineJob = engineJob;
        }

        public void cancel() {
            engineJob.removeCallback(cb);
        }
    }

    private static class LazyDiskCacheProvider implements DecodeJob.DiskCacheProvider {

        private final DiskCache.Factory factory;
        private volatile DiskCache diskCache;

        LazyDiskCacheProvider(DiskCache.Factory factory) {
            this.factory = factory;
        }

        @Override
        public DiskCache getDiskCache() {
            if (diskCache == null) {
                synchronized (this) {
                    if (diskCache == null) {
                        diskCache = factory.build();
                    }
                    if (diskCache == null) {
                        diskCache = new DiskCacheAdapter();
                    }
                }
            }
            return diskCache;
        }
    }
}
