package com.lizy.myglide.load.engine;

import android.support.v4.util.Pools;

import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

/**
 * Created by lizy on 16-5-10.
 */
public final class LockedResource<Z> implements Resource<Z>,
        FactoryPools.Poolable {

    private static final Pools.Pool<LockedResource<?>> POOL = FactoryPools.threadSafe(20,
            new FactoryPools.Factory <LockedResource<?>>() {
                @Override
                public LockedResource<?> create() {
                    return new LockedResource<Object>();
                }
            });

    private final StateVerifier stateVerifier = StateVerifier.newInstance();

    private Resource<Z> toWrap;
    private boolean isRecycled;
    private boolean isLocked;

    static <Z> LockedResource<Z> obtain(Resource<Z> toWrap) {
        LockedResource<Z> lockedResource = (LockedResource<Z>) POOL.acquire();
        lockedResource.init(toWrap);
        return lockedResource;
    }

    private LockedResource() {}

    private void init(Resource<Z> toWrap) {
        this.toWrap = toWrap;
        isRecycled = false;
        isLocked = true;
    }

    @Override
    public Class<Z> getResourceClass() {
        return toWrap.getResourceClass();
    }

    @Override
    public Z get() {
        return toWrap.get();
    }

    @Override
    public int getSize() {
        return toWrap.getSize();
    }

    @Override
    public synchronized void recycle() {
        stateVerifier.throwIfRecycled();

        isRecycled = true;
        if (!isLocked) {
            toWrap.recycle();
            release();
        }
    }

    public synchronized void unlock() {
        stateVerifier.throwIfRecycled();
        if (!isLocked) {
            throw new IllegalStateException("Already unlocked");
        }

        isLocked = false;
        if (isRecycled) {
            recycle();
        }
    }

    private void release() {
        toWrap = null;
        POOL.release(this);
    }

    @Override
    public StateVerifier getVerifier() {
        return stateVerifier;
    }
}
