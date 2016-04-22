package com.lizy.myglide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

/**
 * Created by lizy on 16-4-21.
 */
public interface BitmapPool {

    int getMaxSize();

    void setSizeMultiplier(float sizeMultiplier);

    void put(Bitmap bitmap);

    @NonNull
    Bitmap get(int width, int height, Bitmap.Config config);

    @NonNull
    Bitmap getDirty(int width, int height, Bitmap.Config config);

    void clearMemory();

    void trimMemory(int level);
}
