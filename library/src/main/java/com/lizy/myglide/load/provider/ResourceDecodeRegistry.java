package com.lizy.myglide.load.provider;

import com.lizy.myglide.load.ResourceDecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-4-28.
 */
public class ResourceDecodeRegistry {

    private final List<Entry<?, ?>> decoders = new ArrayList<>();

    public synchronized <T, R> List<ResourceDecoder<T, R>> getDecoders(Class<T> dataClass,
                  Class<R> resourceClass) {
        List<ResourceDecoder<T, R>> result = new ArrayList<>();
        for (Entry<?, ?> entry : decoders) {
            if (entry.handles(dataClass, resourceClass)) {
                result.add((ResourceDecoder<T, R>) entry.decoder);
            }
        }
        return result;
    }

    public synchronized <T, R> List<Class<R>> getResourceClasses(Class<T> dataClass,
                 Class<R> resourceClass) {
        List<Class<R>> result = new ArrayList<>();
        for (Entry<?, ?> entry : decoders) {
            if (entry.handles(dataClass, resourceClass)) {
                result.add((Class<R>) entry.resourceClass);
            }
        }
        return result;
    }

    public synchronized <T, R> void append(ResourceDecoder<T, R> decoder, Class<T> dataClass,
                                      Class<R> resourceClass) {
        decoders.add(new Entry<>(decoder, dataClass, resourceClass));
    }

    public synchronized <T, R> void prepend(ResourceDecoder<T, R> decoder, Class<T> dataClass,
                                           Class<R> resourceClass) {
        decoders.add(0, new Entry<>(decoder, dataClass, resourceClass));
    }

    private static class Entry<T, R> {
        private final Class<T> dataClass;
        private final Class<R> resourceClass;
        private ResourceDecoder<T, R> decoder;

        public Entry(ResourceDecoder<T, R> decoder, Class<T> dataClass, Class<R> resourceClass) {
            this.dataClass = dataClass;
            this.resourceClass = resourceClass;
            this.decoder = decoder;
        }

        public boolean handles(Class<?> dataClass, Class<?> resourceClass) {
            return dataClass.isAssignableFrom(dataClass)
                    && resourceClass.isAssignableFrom(this.resourceClass);
        }

        @Override
        public String toString() {
            return dataClass.getSimpleName() + "--" + resourceClass.getSimpleName() + "--"
                    + decoder.getClass().getSimpleName() + "\n";
        }
    }
}
