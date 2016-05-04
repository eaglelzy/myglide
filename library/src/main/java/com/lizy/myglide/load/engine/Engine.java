package com.lizy.myglide.load.engine;

import android.support.v4.util.Pools;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.Priority;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.cache.DiskCache;
import com.lizy.myglide.load.engine.cache.MemoryCache;
import com.lizy.myglide.load.engine.executor.GlideExecutor;
import com.lizy.myglide.request.ResourceCallback;
import com.lizy.myglide.util.pool.FactoryPools;

import java.util.Map;

/**
 * Created by lizy on 16-5-3.
 */
public class Engine {

    private static final int JOB_POOL_SIZE = 150;

    public Engine(MemoryCache memoryCache,
                  DiskCache.Factory diskCacheFactory,
                  GlideExecutor diskCacheExecutor,
                  GlideExecutor sourceExecutor) {
    }


    public <R> LoadStatus load(
            GlideContext glideContext,
            Object model,
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
            boolean isMemoryCacheable,
            ResourceCallback cb) {

        return null;
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

}
