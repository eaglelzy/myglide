package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.util.Preconditions;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-5-5.
 */
public class EngineResource<Z> implements Resource<Z> {

    private final Resource<Z> resource;
    private final boolean isCacheable;

    private int acquired;

    private Key key;
    private ResourceListener listener;

    private boolean isRecycled;

    interface ResourceListener {
        void onResourceReleased(Key key, EngineResource<?> resource);
    }

    public void setResourceListener(Key key, ResourceListener listener) {
        this.key = key;
        this.listener = listener;
    }

    public EngineResource(Resource<Z> toWrap, boolean isCacheable) {
        this.resource = Preconditions.checkNotNull(toWrap);
        this.isCacheable = isCacheable;
    }

    boolean isCacheable() {
        return isCacheable;
    }

    @Override
    public Class<Z> getResourceClass() {
        return resource.getResourceClass();
    }

    @Override
    public Z get() {
        return resource.get();
    }

    @Override
    public int getSize() {
        return resource.getSize();
    }

    @Override
    public void recycle() {
        if (acquired > 0) {
            throw new IllegalStateException("cannot recycle a resource while it is still acquire");
        }
        if (isRecycled) {
            throw new IllegalStateException("cannot recycle a resource that has already been recycled");
        }
        isRecycled = true;
        resource.recycle();
    }

    public void acquire() {
        if (isRecycled) {
            throw new IllegalStateException("Cannot acquire a recycled resource");
        }
        if (Util.isOnBackgroudThread()) {
            throw new IllegalThreadStateException("Must call acquire() on the main thread");
        }
        ++acquired;
    }

    public void release() {
        if (acquired <= 0) {
            throw new IllegalStateException("Cannot release a recycled or not yet acquire resource");
        }
        if (Util.isOnBackgroudThread()) {
            throw new IllegalThreadStateException("Must call release() on the main thread");
        }
        if (--acquired == 0) {
            listener.onResourceReleased(key, this);
        }
    }
}
