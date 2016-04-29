package com.lizy.myglide.load.resource.transcode;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.load.resource.bitmap.LazyBitmapDrawableResource;

/**
 * Created by lizy on 16-4-29.
 */
public class BitmapDrawableTanscoder implements ResourceTranscoder<Bitmap, BitmapDrawable> {
    private final Resources resources;
    private final BitmapPool bitmapPool;

    public BitmapDrawableTanscoder(Resources resources, BitmapPool bitmapPool) {
        this.resources = resources;
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<BitmapDrawable> transcode(Resource<Bitmap> toTranscode) {
        return LazyBitmapDrawableResource.obtain(resources, bitmapPool, toTranscode.get());
    }
}
