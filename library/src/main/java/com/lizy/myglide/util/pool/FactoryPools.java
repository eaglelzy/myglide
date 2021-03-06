package com.lizy.myglide.util.pool;

import android.support.v4.util.Pools;
import android.support.v4.util.Pools.Pool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-4-18.
 */
public class FactoryPools {
    private static final int DEFAULT_POOL_SIZE = 20;

    private static final Resetter<Object> EMPTY_RESETTER = new Resetter<Object>() {
        @Override
        public void reset(Object object) {
            // Do nothing.
        }
    };

    private FactoryPools() {}

    public static <T extends Poolable> Pool<T> simple(int size, Factory<T> factory) {
        return build(new Pools.SimplePool<T>(size), factory, FactoryPools.<T>getResetter());
    }

    public static <T extends Poolable> Pool<T> threadSafe(int size, Factory<T> factory) {
        return build(new Pools.SynchronizedPool<T>(size), factory, FactoryPools.<T>getResetter());
    }

    public static <T> Pool<List<T>> threadSafeList() {
        return threadSafeList(DEFAULT_POOL_SIZE);
    }

    public static <T> Pool<List<T>> threadSafeList(int size) {
        return build(new Pools.SynchronizedPool<List<T>>(size), new Factory<List<T>>() {
            @Override
            public List<T> create() {
                return new ArrayList<>();
            }
        }, new Resetter<List<T>>() {
            @Override
            public void reset(List<T> object) {
                object.clear();
            }
        });
    }

    private static <T> Pool<T> build(Pool<T> pool, Factory<T> factory,
                                  Resetter<T> resetter) {
        return new FactoryPool<>(factory, pool, resetter);
    }

    public interface Factory<T> {
        T create();
    }

    public interface Poolable {
        StateVerifier getVerifier();
    }

    public interface Resetter<T> {
        void reset(T object);
    }

    private static <T> Resetter<T> getResetter() {
        return (Resetter<T>) EMPTY_RESETTER;
    }

    private final static class FactoryPool<T> implements Pool<T> {

        private Factory<T> factory;
        private Pool<T> pool;
        private final Resetter<T> resetter;

        public FactoryPool(Factory<T> factory, Pool<T> pool, Resetter<T> resetter) {
            this.factory = factory;
            this.pool = pool;
            this.resetter = resetter;
        }

        @Override
        public T acquire() {
            T result = pool.acquire();
            if (result == null) {
                result = factory.create();
            }
            if (result instanceof Poolable) {
                ((Poolable)result).getVerifier().setRecycled(false);
            }
            return result;
        }

        @Override
        public boolean release(T instance) {
            if (instance instanceof Poolable) {
                ((Poolable)instance).getVerifier().setRecycled(true);
            }
            resetter.reset(instance);
            return pool.release(instance);
        }
    }
}
