package com.lizy.myglide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Created by lizy on 16-4-21.
 */
public interface LruPoolStrategy {
    void put(Bitmap bitmap);

    @Nullable
    Bitmap get(int width, int height, Bitmap.Config config);

    @Nullable
    Bitmap removeLast();

    String logBitmap(Bitmap bitmap);

    String logBitmap(int width, int height, Bitmap.Config config);

    int getSize(Bitmap bitmap);
}
