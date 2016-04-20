package com.lizy.myglide.util;

import android.support.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lizy on 16-4-19.
 */
public class LruCache<T, Y> {
    private final LinkedHashMap<T, Y> cache = new LinkedHashMap<>(100, 0.75f, true);
    private final int initialMaxSize;
    private int maxSize;
    private int currentSize;

    public LruCache(int size) {
        initialMaxSize = size;
        maxSize = size;
    }

    public void setSizeMultiplier(float multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier must be >= 0");
        }
        maxSize = Math.round(maxSize * multiplier);
        evict();
    }

    protected void onItemEvicted(T key, Y item) {
        // optional override
    }

    public synchronized int getMaxSize() {
        return maxSize;
    }

    public synchronized int getCurrentSize() {
        return currentSize;
    }

    public synchronized boolean contains(T key) {
        return cache.containsKey(key);
    }

    public synchronized Y put(T key, Y item) {
        final int itemSize = getSize(item);
        if (itemSize >= maxSize) {
            onItemEvicted(key, item);
            return null;
        }

        final Y result = cache.put(key, item);
        if (item != null) {
            currentSize += getSize(item);
        }
        if (result != null) {
            currentSize -= getSize(result);
        }

        evict();

        return result;
    }

    @Nullable
    public synchronized Y get(T key) {
        return cache.get(key);
    }

    @Nullable
    public synchronized Y remove(T key) {
        final Y value = cache.remove(key);
        if (value != null) {
            currentSize -= getSize(value);
        }
        return value;
    }

    private int getSize(Y item) {
        return 1;
    }

    protected synchronized void trimToSize(int size) {
        Map.Entry<T, Y> last;
        while (currentSize > size) {
            last = cache.entrySet().iterator().next();
            final Y toRemove = last.getValue();
            currentSize -= getSize(toRemove);
            final T key = last.getKey();
            cache.remove(key);
            onItemEvicted(key, toRemove);
        }
    }


    public void clearMemory() {
        trimToSize(0);
    }

    private void evict() {
        trimToSize(maxSize);
    }
}
