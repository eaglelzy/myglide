package com.lizy.myglide.provider;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.Encoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-5-5.
 */
public class EncoderRegistry {

    private final List<Entry<?>> encoders = new ArrayList<>();

    @Nullable
    public synchronized <T> Encoder<T> getEncoder(Class<T> dataClass) {
        for (Entry<?> entry : encoders) {
            if (entry.handles(dataClass)) {
                return (Encoder<T>) entry.encoder;
            }
        }
        return null;
    }
    public synchronized <T> void add(Class<T> dataClass, Encoder<T> encoder) {
        encoders.add(new Entry<T>(dataClass, encoder));
    }

    private static class Entry<T> {
        private final Class<T> dataClass;
        private final Encoder<T> encoder;

        public Entry(Class<T> dataClass, Encoder<T> encoder) {
            this.dataClass = dataClass;
            this.encoder = encoder;
        }

        boolean handles(Class<?> dataClass) {
            return this.dataClass.isAssignableFrom(dataClass);
        }
    }
}
