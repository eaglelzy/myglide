package com.lizy.myglide.load;


import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;

import java.security.MessageDigest;
import java.util.Map;

/**
 * Created by lizy on 16-4-27.
 */
public final class Options implements Key {

    private final ArrayMap<Option<?>, Object> values = new ArrayMap<>();

    public void putAll(Options other) {
        values.putAll((SimpleArrayMap<? extends Option<?>, ?>) other.values);
    }

    public <T> Options set(Option<T> option, T value) {
        values.put(option, value);
        return this;
    }

    public <T> T get(Option<T> option) {
        return values.containsKey(option) ? (T)values.get(option) : option.getDefaultValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Options options = (Options) o;

        return values.equals(options.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "Options{" +
                "values=" + values +
                '}';
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        for (Map.Entry<Option<?>, Object> entry : values.entrySet()) {
            updateDiskCacheKey(entry.getKey(), entry.getValue(), messageDigest);
        }
    }

    private <T> void updateDiskCacheKey(Option<T> option, Object value, MessageDigest messageDigest) {
        option.update((T)value, messageDigest);
    }
}
