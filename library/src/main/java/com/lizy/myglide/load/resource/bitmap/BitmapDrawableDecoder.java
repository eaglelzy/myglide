package com.lizy.myglide.load.resource.bitmap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.ResourceDecoder;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.util.Preconditions;

import java.io.IOException;

/**
 * Created by lizy on 16-4-29.
 */
public class BitmapDrawableDecoder<DataType> implements ResourceDecoder<DataType, BitmapDrawable> {

    private final ResourceDecoder<DataType, Bitmap> decoder;
    private final Resources resources;
    private final BitmapPool bitmapPool;


    public BitmapDrawableDecoder(Resources resources, BitmapPool bitmapPool,
                                 ResourceDecoder<DataType, Bitmap> decoder) {
        this.resources = Preconditions.checkNotNull(resources);
        this.bitmapPool = Preconditions.checkNotNull(bitmapPool);
        this.decoder = Preconditions.checkNotNull(decoder);
    }

    @Override
    public boolean handles(DataType source, Options options) throws IOException {
        return decoder.handles(source, options);
    }

    @Override
    public Resource<BitmapDrawable> decode(DataType source, int width, int height, Options options) throws IOException {
        Resource<Bitmap> bitmapResource = decoder.decode(source, width, height, options);
        if (bitmapResource == null) {
            return null;
        }
        return LazyBitmapDrawableResource.obtain(resources, bitmapPool, bitmapResource.get());
    }
}
