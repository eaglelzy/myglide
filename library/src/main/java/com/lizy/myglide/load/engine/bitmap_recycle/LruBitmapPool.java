package com.lizy.myglide.load.engine.bitmap_recycle;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lizy on 16-4-21.
 */
public class LruBitmapPool implements BitmapPool {

    private static final String TAG = "LruBitmapPool";
    private static final Bitmap.Config DEFAULT_BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private final LruPoolStrategy strategy;
    private final int initialMaxSize;
    private final Set<Bitmap.Config> allowedConfigs;
    private final BitmapTracker tracker;

    private int maxSize;
    private int currentSize;
    private int hits;
    private int misses;
    private int puts;
    private int evictions;

    public LruBitmapPool(int initialMaxSize, LruPoolStrategy strategy, Set<Bitmap.Config> allowedConfigs) {
        this.strategy = strategy;
        this.initialMaxSize = initialMaxSize;
        this.allowedConfigs = allowedConfigs;
        maxSize = initialMaxSize;
        tracker = new NullBitmapTracker();
    }

    public LruBitmapPool(int initialMaxSize) {
        this(initialMaxSize, getDefaultStatery(), getDefaultAllowedConfigs());
    }

    public LruBitmapPool(int initialMaxSize, Set<Bitmap.Config> allowedConfigs) {
        this(initialMaxSize, getDefaultStatery(), allowedConfigs);
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void setSizeMultiplier(float sizeMultiplier) {
        maxSize = Math.round(maxSize * sizeMultiplier);
        evict();
    }

    @Override
    public void put(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("bitmap must be not null!");
        }
        if (bitmap.isRecycled()) {
            throw new IllegalStateException("Cannot pool recycled bitmap");
        }

        if (!bitmap.isMutable() || strategy.getSize(bitmap) > maxSize
                || !allowedConfigs.contains(bitmap.getConfig())) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Reject bitmap from pool"
                        + ", bitmap:" + strategy.logBitmap(bitmap)
                        + ", isMutable:" + bitmap.isMutable()
                        + ", is allow config:" + allowedConfigs.contains(bitmap.getConfig()));
            }
            bitmap.recycle();
            return;
        }

        final int size = strategy.getSize(bitmap);
        strategy.put(bitmap);
        tracker.add(bitmap);

        puts++;
        currentSize+=size;

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Put bitmap to pool=" + strategy.logBitmap(bitmap));
        }
        dump();

        evict();
    }

    @NonNull
    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        Bitmap bitmap = getDirtyOrNull(width, height, config);
        if (bitmap != null) {
            bitmap.eraseColor(Color.TRANSPARENT);
        } else {
            bitmap = Bitmap.createBitmap(width, height, config);
        }
        return bitmap;
    }

    @NonNull
    @Override
    public Bitmap getDirty(int width, int height, Bitmap.Config config) {
        Bitmap bitmap = getDirtyOrNull(width, height, config);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, config);
        }
        return bitmap;
    }

    @Nullable
    private synchronized Bitmap getDirtyOrNull(int width, int height, Bitmap.Config config) {
        final Bitmap bitmap = strategy.get(width, height, config != null ? config : DEFAULT_BITMAP_CONFIG);
        if (bitmap == null) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Missing bitmap=" + strategy.logBitmap(width, height, config));
            }
            misses++;
        } else {
            hits++;
            currentSize -= strategy.getSize(bitmap);
            tracker.add(bitmap);
            normalize(bitmap);
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Get bitmap=" + strategy.logBitmap(width, height, config));
        }
        dump();
        return bitmap;
    }

    private void normalize(Bitmap bitmap) {
        maybeSetAlpha(bitmap);
        maybeSetPreMultiplied(bitmap);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void maybeSetAlpha(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
            bitmap.setHasAlpha(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void maybeSetPreMultiplied(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bitmap.setPremultiplied(true);
        }
    }

    @Override
    public void clearMemory() {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "clearMemory");
        }
        trimToSize(0);
    }

    @Override
    public void trimMemory(int level) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "trimMemory, level=" + level);
        }
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            clearMemory();
        } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            trimToSize(maxSize / 2);
        }
    }

    private void dump() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            dumpUnchecked();
        }
    }

    private void dumpUnchecked() {
        Log.v(TAG, "Hits=" + hits + ", misses=" + misses + ", puts=" + puts + ", evictions=" + evictions
                + ", currentSize=" + currentSize + ", maxSize=" + maxSize + "\nStrategy=" + strategy);
    }

    private void evict() {
        trimToSize(maxSize);
    }

    private static LruPoolStrategy getDefaultStatery() {
        final LruPoolStrategy stratery;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            stratery = new SizeConfigStrategy();
        } else {
            stratery = new SizeConfigStrategy();

        }
        return stratery;
    }

    private static Set<Bitmap.Config> getDefaultAllowedConfigs () {
        Set<Bitmap.Config> configs = new HashSet<>();
        configs.addAll(Arrays.asList(Bitmap.Config.values()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            configs.add(null);
        }

        return Collections.unmodifiableSet(configs);
    }

    private synchronized void trimToSize(int size) {
        while (currentSize > size) {
            final Bitmap removed = strategy.removeLast();
            if (removed == null) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Size missmatch, reseting");
                    dumpUnchecked();
                }
                currentSize = 0;
                return;
            }

            tracker.remove(removed);
            currentSize -= strategy.getSize(removed);
            evictions++;
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Evicting bitmap=" + strategy.logBitmap(removed));
            }
            dump();
            removed.recycle();
        }
    }


    private interface BitmapTracker {
        void add(Bitmap bitmap);

        void remove(android.graphics.Bitmap bitmap);
    }

    @SuppressWarnings("unused")
    // Only used for debugging
    private static class ThrowingBitmapTracker implements BitmapTracker {
        private final Set<Bitmap> bitmaps = Collections.synchronizedSet(new HashSet<Bitmap>());

        @Override
        public void add(Bitmap bitmap) {
            if (bitmaps.contains(bitmap)) {
                throw new IllegalStateException(
                        "Can't add already added bitmap: " + bitmap + " [" + bitmap.getWidth() + "x" + bitmap
                                .getHeight() + "]");
            }
            bitmaps.add(bitmap);
        }

        @Override
        public void remove(Bitmap bitmap) {
            if (!bitmaps.contains(bitmap)) {
                throw new IllegalStateException("Cannot remove bitmap not in tracker");
            }
            bitmaps.remove(bitmap);
        }
    }

    private static class NullBitmapTracker implements BitmapTracker {

        @Override
        public void add(Bitmap bitmap) {

        }

        @Override
        public void remove(Bitmap bitmap) {

        }
    }

}
