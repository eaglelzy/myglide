package com.lizy.myglide.load.resource.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-4-29.
 */
public class LazyBitmapDrawableResource implements Resource<BitmapDrawable>{

    private final Bitmap bitmap;
    private final Resources resources;
    private final BitmapPool bitmapPool;

    public static LazyBitmapDrawableResource obtain(Resources resources, BitmapPool bitmapPool,
                                                    Bitmap bitmap) {
        return new LazyBitmapDrawableResource(bitmap, resources, bitmapPool);
    }

    public LazyBitmapDrawableResource(Bitmap bitmap, Resources resources, BitmapPool bitmapPool) {
        this.bitmap = bitmap;
        this.resources = resources;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Class<BitmapDrawable> getResourceClass() {
        return BitmapDrawable.class;
    }

    @Override
    public BitmapDrawable get() {
        return new BitmapDrawable(resources, bitmap);
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
