package com.lizy.myglide.request;

import android.support.v4.util.Pools;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.engine.GlideException;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.request.target.SizeReadyCallback;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.util.Util;
import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

/**
 * Created by lizy on 16-5-4.
 */
public class SingleRequest<R> implements Request,
        SizeReadyCallback,
        ResourceCallback,
        FactoryPools.Poolable {

    private final StateVerifier stateVerifier = StateVerifier.newInstance();

    private GlideContext context;
    private Object model;
    private int overrideWidth;
    private int overrideHeight;
    private Target<R> target;

    private Status status;
    private enum Status {
        /**
         * Created but not yet running.
         */
        PENDING,
        /**
         * In the process of fetching media.
         */
        RUNNING,
        /**
         * Waiting for a callback given to the Target to be called to determine target dimensions.
         */
        WAITING_FOR_SIZE,
        /**
         * Finished loading media successfully.
         */
        COMPLETE,
        /**
         * Failed to load media, may be restarted.
         */
        FAILED,
        /**
         * Cancelled by the user, may not be restarted.
         */
        CANCELLED,
        /**
         * Cleared by the user with a placeholder set, may not be restarted.
         */
        CLEARED,
        /**
         * Temporarily paused by the system, may be restarted.
         */
        PAUSED,
    }

    private static final Pools.Pool<SingleRequest<?>> POOL = FactoryPools.simple(150,
            new FactoryPools.Factory<SingleRequest<?>>() {
                @Override
                public SingleRequest<?> create() {
                    return new SingleRequest<Object>();
                }
            });

    private SingleRequest() {}

    public static <R> SingleRequest<R> obtain(
            GlideContext context,
            Object model,
            Target<R> target,
            int overrideWidth,
            int overrideHeight) {
        SingleRequest<R> request = (SingleRequest<R>) POOL.acquire();
        if (request == null) {
            request = new SingleRequest<>();
        }

        request.init(context,
                model,
                target,
                overrideWidth,
                overrideHeight);

        return request;
    }

    private void init(GlideContext context,
                      Object model,
                      Target<R> target,
                      int overrideWidth,
                      int overrideHeight ) {
        this.context = context;
        this.model = model;
        this.target = target;
        this.overrideWidth = overrideWidth;
        this.overrideHeight = overrideHeight;
        status = Status.PENDING;
    }

    @Override
    public void begin() {
        if (model == null) {
            onLoadFailed(new GlideException("model is null"));
            return;
        }

        status = Status.WAITING_FOR_SIZE;
        if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
            onSizeReady(overrideWidth, overrideHeight);
        } else {
            target.getSize(this);
        }
    }

    @Override
    public void pause() {
        clear();
        status = Status.PAUSED;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isPause() {
        return Status.PAUSED == status;
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING || status == Status.WAITING_FOR_SIZE;
    }

    @Override
    public boolean isComplete() {
        return status == Status.COMPLETE;
    }

    @Override
    public boolean isResourceSet() {
        return isComplete();
    }

    @Override
    public boolean isCanceled() {
        return status == Status.CANCELLED || status == Status.CLEARED;
    }

    @Override
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    @Override
    public void recycle() {

    }

    @Override
    public void onSizeReady(int width, int height) {
    }

    @Override
    public StateVerifier getVerifier() {
        return stateVerifier;
    }

    @Override
    public void onResourceReady(Resource<?> resource, DataSource dataSource) {

    }

    @Override
    public void onLoadFailed(GlideException e) {

    }
}
