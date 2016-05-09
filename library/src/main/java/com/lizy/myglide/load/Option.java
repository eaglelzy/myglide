package com.lizy.myglide.load;

import android.support.annotation.Nullable;

import java.security.MessageDigest;

/**
 * Created by lizy on 16-5-9.
 */
public final class Option<T> {
    private final String key;
    private final T defaultValue;
    private final CacheKeyUpdater<T> cacheKeyUpdater;
    private volatile byte[] keyBytes;

    private final static CacheKeyUpdater<Object> EMPTY_UPDATER = new CacheKeyUpdater<Object>() {
        @Override
        public void update(byte[] keyBytes, Object value, MessageDigest messageDigest) {

        }
    };

    public static <T> Option<T> memory(String key) {
        return new Option<>(key, null, Option.<T>emptyUpdater());
    }

    public static <T> Option<T> memory(String key, T defaultValue) {
        return new Option<>(key, defaultValue, Option.<T>emptyUpdater());
    }

    public static <T> Option<T> disk(String key, CacheKeyUpdater<T> cacheKeyUpdater) {
        return new Option<>(key, null /*defaultValue*/, cacheKeyUpdater);
    }

    public static <T> Option<T> disk(String key, T defaultValue, CacheKeyUpdater<T> cacheKeyUpdater) {
        return new Option<>(key, defaultValue, cacheKeyUpdater);
    }

    private static <T> CacheKeyUpdater<T> emptyUpdater() {
        return (CacheKeyUpdater<T>) EMPTY_UPDATER;
    }

    Option(String key, T defaultValue, CacheKeyUpdater<T> cacheKeyUpdater) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.cacheKeyUpdater = cacheKeyUpdater;
    }

    @Nullable
    public T getDefaultValue() {
        return defaultValue;
    }

    public void update(T value, MessageDigest messageDigest) {
        cacheKeyUpdater.update(getKeyBytes(), value, messageDigest);
    }

    private byte[] getKeyBytes() {
        if (keyBytes == null) {
            keyBytes = key.getBytes(Key.CHARSET);
        }
        return keyBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option<?> option = (Option<?>) o;

        return key != null ? key.equals(option.key) : option.key == null;

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Option{" +
                "key='" + key + '\'' +
                '}';
    }

    interface CacheKeyUpdater<T> {
        void update(byte[] keyBytes, T value, MessageDigest messageDigest);
    }
}
