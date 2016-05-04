package com.lizy.myglide.load.resource.bitmap;

import android.content.Context;
import android.graphics.Bitmap;

import com.lizy.myglide.Glide;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.Transformation;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-5-4.
 */
public abstract class BitmapTransformation implements Transformation<Bitmap> {

    private final BitmapPool bitmapPool;

    public BitmapTransformation(Context context) {
        this(Glide.get(context).getBitmapPool());
    }

    public BitmapTransformation(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
        if (!Util.isValidDimensions(outWidth, outHeight)) {
            throw new IllegalArgumentException("Cannot apply transformation on width=" + outWidth
                    + " height=" + outHeight);
        }
        Bitmap toTransform = resource.get();
        int targetWidth = outWidth == Target.SIZE_ORIGINAL ? toTransform.getWidth() : outWidth;
        int targetHeight = outHeight == Target.SIZE_ORIGINAL ? toTransform.getHeight() : outHeight;

        Bitmap transformed = transform(bitmapPool, toTransform, targetWidth, targetHeight);

        final Resource<Bitmap> result;
        if (toTransform == transformed) {
            result = resource;
        } else {
            result = BitmapResource.obtain(transformed, bitmapPool);
        }
        return result;
    }

    abstract protected Bitmap transform(BitmapPool bitmapPool,
                                        Bitmap toTransform,
                                        int destWidth,
                                        int destHeight);
}
