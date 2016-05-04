package com.lizy.myglide.load.resource.bitmap;

import android.content.Context;
import android.graphics.Bitmap;

import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;

import java.security.MessageDigest;

/**
 * Created by lizy on 16-5-4.
 */
public class CircleCrop extends BitmapTransformation {
    private static final String ID = "com.lizy.myglide.resource.bitmap.CircleCrop";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    public CircleCrop(Context context) {
        super(context);
    }

    public CircleCrop(BitmapPool bitmapPool) {
        super(bitmapPool);
    }

    @Override
    protected Bitmap transform(BitmapPool bitmapPool, Bitmap toTransform, int destWidth, int destHeight) {
        return TransformationUtils.circleCrop(bitmapPool, toTransform, destWidth, destHeight);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CircleCrop;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
