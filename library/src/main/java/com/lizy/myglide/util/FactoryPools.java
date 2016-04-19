package com.lizy.myglide.util;

import android.support.v4.util.Pools;
import android.support.v4.util.Pools.Pool;

/**
 * Created by lizy on 16-4-18.
 */
public class FactoryPools {

    public static <T> Pool<T> simply(int size, Factory<T> factory) {
        return new FactoryPool<>(factory, new Pools.SimplePool<T>(size));
    }

    public static <T> Pool<T> threadSafe(int size, Factory<T> factory) {
        return new FactoryPool<>(factory, new Pools.SynchronizedPool<T>(size));
    }

    public interface Factory<T> {
        T create();
    }

    private final static class FactoryPool<T> implements Pool<T> {

        private Factory<T> factory;
        private Pool<T> pool;

        public FactoryPool(Factory<T> factory, Pool<T> pool) {
            this.factory = factory;
            this.pool = pool;
        }

        @Override
        public T acquire() {
            T result = pool.acquire();
            if (result == null) {
                result = factory.create();
            }
            return result;
        }

        @Override
        public boolean release(T instance) {
            return pool.release(instance);
        }
    }
}
