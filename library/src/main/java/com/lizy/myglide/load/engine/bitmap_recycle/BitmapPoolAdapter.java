package com.lizy.myglide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Created by lizy on 16-5-4.
 */
public class BitmapPoolAdapter implements BitmapPool {
    @Override
    public int getMaxSize() {
        return 0;
    }

    @Override
    public void setSizeMultiplier(float sizeMultiplier) {

    }

    @Override
    public void put(Bitmap bitmap) {
        bitmap.recycle();
    }

    @NonNull
    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, config);
    }

    @NonNull
    @Override
    public Bitmap getDirty(int width, int height, Bitmap.Config config) {
        return get(width, height, config);
    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void trimMemory(int level) {

    }
}
