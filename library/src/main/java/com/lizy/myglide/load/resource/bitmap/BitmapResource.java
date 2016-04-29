package com.lizy.myglide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.util.Preconditions;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-4-29.
 */
public class BitmapResource implements Resource<Bitmap> {
    private final BitmapPool bitmapPool;
    private final Bitmap bitmap;

    public BitmapResource(Bitmap bitmap, BitmapPool bitmapPool) {
        this.bitmapPool = Preconditions.checkNotNull(bitmapPool, "BitmapPool cannot be null");
        this.bitmap = Preconditions.checkNotNull(bitmap, "Bitmap cannot be null");
    }

    @Nullable
    public static BitmapResource obtain(@Nullable Bitmap bitmap, BitmapPool bitmapPool) {
        if (bitmap == null) {
            return null;
        } else {
            return new BitmapResource(bitmap, bitmapPool);
        }
    }

    @Override
    public Class<Bitmap> getResourceClass() {
        return Bitmap.class;
    }

    @Override
    public Bitmap get() {
        return bitmap;
    }

    @Override
    public int getSize() {
        return Util.getBitmapByteSize(bitmap);
    }

    @Override
    public void recycle() {
        bitmapPool.put(bitmap);
    }
}
