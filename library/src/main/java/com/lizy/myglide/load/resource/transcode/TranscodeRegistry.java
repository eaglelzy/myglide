package com.lizy.myglide.load.resource.transcode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-4-28.
 */
public class TranscodeRegistry {
    private final List<Entry<?, ?>> transcoders = new ArrayList<>();

    public synchronized <Z, R> void register(Class<Z> decodedClass, Class<R> transcodedClass,
                                             ResourceTranscoder<Z, R> transcoder) {
        transcoders.add(new Entry<>(decodedClass, transcodedClass, transcoder));
    }

    @SuppressWarnings("unchecked")
    public synchronized <Z, R> ResourceTranscoder<Z, R> get(Class<Z> resourceClass,
                                                Class<R> transcodedClass) {
        if (transcodedClass.isAssignableFrom(resourceClass)) {
            return (ResourceTranscoder<Z, R>) UnitTranscoder.get();
        }
        for (Entry<?, ?> entry : transcoders) {
            if (entry.handles(resourceClass, transcodedClass)) {
                return (ResourceTranscoder<Z, R>) entry.transcoder;
            }
        }

        throw new IllegalArgumentException(
                "No transcoder registered to transcode from " + resourceClass + " to " + transcodedClass);
    }

    public synchronized <Z, R> List<Class<R>> getTranscodeClasses(Class<Z> resourceClass,
                                                          Class<R> transcodeClass) {
        List<Class<R>> transcodeClasses = new ArrayList<>();
        if (transcodeClass.isAssignableFrom(resourceClass)) {
            transcodeClasses.add(transcodeClass);
            return transcodeClasses;
        }

        for (Entry<?, ?> entry : transcoders) {
            if (entry.handles(resourceClass, transcodeClass)) {
                transcodeClasses.add(transcodeClass);
            }
        }

        return transcodeClasses;
    }

    private static final class Entry<Z, R> {
        private final Class<Z> fromClass;
        private final Class<R> toClass;
        private final ResourceTranscoder<Z, R> transcoder;

        Entry(Class<Z> fromClass, Class<R> toClass, ResourceTranscoder<Z, R> transcoder) {
            this.fromClass = fromClass;
            this.toClass = toClass;
            this.transcoder = transcoder;
        }

        public boolean handles(Class<?> fromClass, Class<?> toClass) {
            return this.fromClass.isAssignableFrom(fromClass) && toClass.isAssignableFrom(this.toClass);
        }

        @Override
        public String toString() {
            return fromClass.getSimpleName() + "--" + toClass.getSimpleName() + "--"
                    + transcoder.getClass().getSimpleName() + "\n";
        }
    }
}
