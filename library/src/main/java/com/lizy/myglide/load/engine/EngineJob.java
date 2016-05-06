package com.lizy.myglide.load.engine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.Pools;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.engine.executor.GlideExecutor;
import com.lizy.myglide.request.ResourceCallback;
import com.lizy.myglide.util.Util;
import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-5-4.
 */
public class EngineJob<R> implements DecodeJob.Callback<R>,
        FactoryPools.Poolable{

    private final static EngineResourceFactory DEFAULT_ENGINEROURCE_FACTORY = new EngineResourceFactory();

    private final StateVerifier stateVerifier = StateVerifier.newInstance();
    private final GlideExecutor sourceExecutor;
    private final GlideExecutor diskExecutor;
    private final EngineJobListener engineJobListener;
    private final EngineResourceFactory engineResourceFactory;
    private final Pools.Pool<EngineJob<?>> pool;

    private final List<ResourceCallback> cbs = new ArrayList<>(2);

    private Key key;
    private boolean isCacheable;

    private boolean hasResource;
    private boolean isFailed;

    private volatile boolean isCanceled;

    private EngineResource<?> engineResource;
    private GlideException exception;
    private DataSource dataSource;

    private DecodeJob<R> decodeJob;
    private Resource<R> resource;

    private static final int MSG_COMPLETE = 1;
    private static final int MSG_EXCEPTION = 2;
    private static final int MSG_CANCELLED = 3;

    private static final Handler MAIN_THREAD_HANDLER =
            new Handler(Looper.getMainLooper(), new MainThreadCallback());

    EngineJob(GlideExecutor sourceExecutor,
                     GlideExecutor diskExecutor,
                     EngineJobListener engineJobListener,
                     Pools.Pool<EngineJob<?>> pool) {
        this(sourceExecutor, diskExecutor, engineJobListener, pool, DEFAULT_ENGINEROURCE_FACTORY);
    }

    EngineJob(GlideExecutor sourceExecutor,
                     GlideExecutor diskExecutor,
                     EngineJobListener engineJobListener,
                     Pools.Pool<EngineJob<?>> pool,
                     EngineResourceFactory engineResourceFactory) {
        this.sourceExecutor = sourceExecutor;
        this.diskExecutor = diskExecutor;
        this.engineJobListener = engineJobListener;
        this.pool = pool;
        this.engineResourceFactory = engineResourceFactory;
    }

    EngineJob<R> init(Key key, boolean isCacheable) {
        this.key = key;
        this.isCacheable = isCacheable;
        return this;
    }

    @Override
    public void onResourceReady(Resource<R> resource, DataSource dataSource) {
        this.resource = resource;
        this.dataSource = dataSource;
        MAIN_THREAD_HANDLER.obtainMessage(MSG_COMPLETE, this).sendToTarget();
    }

    @Override
    public void onLoadFailed(GlideException e) {

    }

    @Override
    public void reschedule(DecodeJob<?> job) {
        if (isCanceled) {
            MAIN_THREAD_HANDLER.obtainMessage(MSG_CANCELLED).sendToTarget();
        } else {
            sourceExecutor.execute(job);
        }
    }

    @Override
    public StateVerifier getVerifier() {
        return stateVerifier;
    }

    static class EngineResourceFactory {
        <R> EngineResource<R> build(Resource<R> resource, boolean isCacheable) {
            return new EngineResource<>(resource, isCacheable);
        }
    }

    public void removeCallback(ResourceCallback cb) {

    }

    public void start(DecodeJob<R> decodeJob) {
        this.decodeJob = decodeJob;
        GlideExecutor executor = decodeJob.willDecodeFromCache() ? diskExecutor : sourceExecutor;
        executor.execute(decodeJob);
    }

    void addCallback(ResourceCallback cb) {
        Util.assertMainThread();
        stateVerifier.throwIfRecycled();
        if (hasResource) {
            cb.onResourceReady(engineResource, dataSource);
        } else if (isFailed) {
            cb.onLoadFailed(exception);
        } else {
            cbs.add(cb);
        }
    }

    private static class MainThreadCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            EngineJob job = (EngineJob) message.obj;
            switch (message.what) {
                case MSG_COMPLETE:
                    job.handleResultOnMainThread();
                    break;
                case MSG_EXCEPTION:
                    job.handleExceptionOnMainThread();
                    break;
                case MSG_CANCELLED:
                    job.handleCancelledOnMainThread();
                    break;
                default:
                    throw new IllegalStateException("Unrecognized message: " + message.what);
            }
            return true;
        }
    }

    private void handleCancelledOnMainThread() {
        stateVerifier.throwIfRecycled();
        if (!isCanceled) {
            throw new IllegalStateException("Not canceled");
        }
        engineJobListener.onEngineJobCancelled(this, key);
        release(false);
    }

    private void handleExceptionOnMainThread() {

    }

    private void handleResultOnMainThread() {
        stateVerifier.throwIfRecycled();
        if (isCanceled) {
            resource.recycle();
            release(false);
            return;
        } else if (cbs.isEmpty()) {
            throw new IllegalStateException("Receive a resource without any callbacks to notify");
        } else if (hasResource) {
            throw new IllegalStateException("Already has Resource");
        }
        engineResource = engineResourceFactory.build(resource, isCacheable);
        hasResource = true;

        engineResource.acquire();
        engineJobListener.onEngineJobComplete(key, engineResource);

        for (ResourceCallback cb : cbs) {
            engineResource.acquire();
            cb.onResourceReady(engineResource, dataSource);
        }

        engineResource.release();

        release(false);
    }

    private void release(boolean isRemoveFromQueue) {
        Util.assertMainThread();
        cbs.clear();
        key = null;
        engineResource = null;
        resource = null;
        hasResource = false;
        isCanceled = false;
        decodeJob.release(isRemoveFromQueue);
        decodeJob = null;
        exception = null;
        dataSource = null;
        pool.release(this);
    }
}
