package com.lizy.myglide.load.model;

import com.lizy.myglide.util.LruCache;
import com.lizy.myglide.util.Util;

import java.util.Queue;

/**
 * Created by lizy on 16-4-28.
 */
public class ModelCache<A, B> {

    private final static int DEFAULT_SIZE = 250;

    private final LruCache<ModelKey<A>, B> lruCache;

    public ModelCache() {
        this(DEFAULT_SIZE);
    }
    public ModelCache(int size) {
        lruCache = new LruCache<ModelKey<A>, B>(size) {
            @Override
            protected void onItemEvicted(ModelKey<A> key, B item) {
                key.release();
            }
        };
    }

    public B get(A model, int width, int height) {
        ModelKey<A> modelKey = ModelKey.get(model, width, height);
        B result = lruCache.get(modelKey);
        modelKey.release();
        return result;
    }
    public void put(A model, int width, int height, B value) {
        ModelKey<A> modelKey = ModelKey.get(model, width, height);
        lruCache.put(modelKey, value);
    }

    public void clear() {
        lruCache.clearMemory();
    }

    final static class ModelKey<A> {
        private static final Queue<ModelKey<?>> QUEUE = Util.createQueue(0);

        private int width;
        private int height;
        private A model;

        static <A> ModelKey<A> get(A model, int width, int height) {
            ModelKey<A> modelKey;
            synchronized (QUEUE) {
                modelKey = (ModelKey<A>)QUEUE.poll();
            }
            if (modelKey == null) {
                modelKey = new ModelKey<>();
            }
            modelKey.init(model, width, height);
            return modelKey;
        }

        public void release() {
            synchronized (QUEUE) {
                QUEUE.offer(this);
            }
        }

        private void init(A model, int width, int height) {
            this.model = model;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ModelKey<?> modelKey = (ModelKey<?>) o;

            if (width != modelKey.width) return false;
            if (height != modelKey.height) return false;
            return model != null ? model.equals(modelKey.model) : modelKey.model == null;

        }

        @Override
        public int hashCode() {
            int result = width;
            result = 31 * result + height;
            result = 31 * result + (model != null ? model.hashCode() : 0);
            return result;
        }
    }
}
