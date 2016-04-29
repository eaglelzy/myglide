package com.lizy.myglide.load.provider;

import android.support.v4.util.ArrayMap;

import com.lizy.myglide.load.engine.LoadPath;
import com.lizy.myglide.util.MultiClassKey;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by lizy on 16-4-29.
 */
public class LoadPathCache {

    private Map<MultiClassKey, LoadPath<?, ?, ?>> cache = new ArrayMap<>();

    private AtomicReference<MultiClassKey> cacheKey = new AtomicReference<>();

    public boolean contains(Class<?> dataClass, Class<?> resourceClass, Class<?> transcodeClass) {
        MultiClassKey key = getKey(dataClass, resourceClass, transcodeClass);
        boolean result;
        synchronized (cache) {
            result = cache.containsKey(key);
        }
        cacheKey.set(key);
        return result;
    }

    // TODO：该方法没有在整个方法加synchronized关键字，而是通过使用AtomicReference缩小synchronized使用范围，漂亮
    public <Data, TResource, Transcode> LoadPath<Data, TResource, Transcode> get(
            Class<Data> dataClass, Class<TResource> resourceClass, Class<Transcode> transcodeClass) {
        MultiClassKey key = getKey(dataClass, resourceClass, transcodeClass);
        LoadPath<?, ?, ?> result;
        synchronized (cache) {
            result = cache.get(key);
        }
        cacheKey.set(key);
        return (LoadPath<Data, TResource, Transcode>) result;
    }

    public void put(Class<?> dataClass, Class<?> resourceClass, Class<?> transcodeClass,
                    LoadPath<?, ?, ?> loadPath) {
        synchronized (cache) {
            cache.put(new MultiClassKey(dataClass, resourceClass, transcodeClass), loadPath);
        }
    }

    private MultiClassKey getKey(Class<?> dataClass, Class<?> resourceClass, Class<?> transcodeClass) {
        MultiClassKey key = cacheKey.getAndSet(null);
        if (key == null) {
            key = new MultiClassKey();
        }

        key.set(dataClass, resourceClass, transcodeClass);
        return key;
    }
}
