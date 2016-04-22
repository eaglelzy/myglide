package com.lizy.myglide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-4-21.
 */
public class AttributeStrategy implements LruPoolStrategy {

    private final KeyPool keyPool = new KeyPool();
    private final GroupLinkedMap<Key, Bitmap> groupLinkedMap = new GroupLinkedMap<>();

    @Override
    public void put(Bitmap bitmap) {
        final Key key = keyPool.get(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        groupLinkedMap.put(key, bitmap);
    }

    @Nullable
    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        final Key key = keyPool.get(width, height, config);
        return groupLinkedMap.get(key);
    }

    @Nullable
    @Override
    public Bitmap removeLast() {
        return groupLinkedMap.removeLast();
    }

    @Override
    public String logBitmap(Bitmap bitmap) {
        return getBitmapString(bitmap);
    }

    @Override
    public String logBitmap(int width, int height, Bitmap.Config config) {
        return getBitmapString(width, height, config);
    }

    @Override
    public int getSize(Bitmap bitmap) {
        return Util.getBitmapByteSize(bitmap);
    }

    private static String getBitmapString(Bitmap bitmap) {
        return getBitmapString(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    }

    private static String getBitmapString(int width, int height, Bitmap.Config config) {
        return "[" + width + "x" + height + "], " + config;
    }

    private static class KeyPool extends BaseKeyPool<Key> {

        public Key get(int width, int height, Bitmap.Config config) {
            Key key = get();
            key.init(width, height, config);
            return key;
        }

        @Override
        protected Key create() {
            return new Key(this);
        }
    }

    private static class Key implements Poolable {

        private final KeyPool keyPool;

        private int width;
        private int height;
        private Bitmap.Config config;

        public Key(KeyPool keyPool) {
            this.keyPool = keyPool;
        }

        public void init(int width, int height, Bitmap.Config config) {
            this.width = width;
            this.height = height;
            this.config = config;
        }

        @Override
        public void offer() {
            keyPool.offer(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (width != key.width) return false;
            if (height != key.height) return false;
            return config == key.config;

        }

        @Override
        public int hashCode() {
            int result = width;
            result = 31 * result + height;
            result = 31 * result + (config != null ? config.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return getBitmapString(width, height, config);
        }
    }
}
