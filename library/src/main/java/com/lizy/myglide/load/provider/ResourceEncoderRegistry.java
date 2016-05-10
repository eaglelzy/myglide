package com.lizy.myglide.load.provider;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.ResourceEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-5-10.
 */
public class ResourceEncoderRegistry {
    private final List<Entry<?>> encoders = new ArrayList<>();

    public synchronized <Z> void add(Class<Z> resourceClass, ResourceEncoder<Z> resourceEncoder) {
        encoders.add(new Entry<>(resourceClass, resourceEncoder));
    }

    @Nullable
    public synchronized <Z> ResourceEncoder<Z> get(Class<Z> resourceClass) {
        int size = encoders.size();
        for (int i = 0; i < size; i++) {
            Entry<?> entry = encoders.get(i);
            if (entry.handles(resourceClass)) {
                return (ResourceEncoder<Z>) entry.encoder;
            }
        }
        return null;
    }

    final static class Entry<T> {
        private final Class<T> resourceClass;
        private final ResourceEncoder<T> encoder;

        public Entry(Class<T> resourceClass, ResourceEncoder<T> encoder) {
            this.resourceClass = resourceClass;
            this.encoder = encoder;
        }

        private boolean handles(Class<?> resourceClass) {
            return this.resourceClass.isAssignableFrom(resourceClass);
        }

    }
}
