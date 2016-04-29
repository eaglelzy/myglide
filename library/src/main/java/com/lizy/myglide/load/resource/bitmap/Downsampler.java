package com.lizy.myglide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.util.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lizy on 16-4-29.
 */
public class Downsampler {
    private final BitmapPool bitmapPool;
    private final ArrayPool byteArrayPool;
    private DisplayMetrics displayMetrics;

    private static final DecodeCallbacks EMPTY_CALLBACKS = new DecodeCallbacks() {
        @Override
        public void onObtainBounds() {
            // Do nothing.
        }

        @Override
        public void onDecodeComplete(BitmapPool bitmapPool, Bitmap downsampled) throws IOException {
            // Do nothing.
        }
    };

    public Downsampler(DisplayMetrics displayMetrics, BitmapPool bitmapPool,
                       ArrayPool byteArrayPool) {
        this.displayMetrics = Preconditions.checkNotNull(displayMetrics);
        this.bitmapPool = Preconditions.checkNotNull(bitmapPool);
        this.byteArrayPool = Preconditions.checkNotNull(byteArrayPool);
    }

    public Resource<Bitmap> decode(InputStream is, int requestedWidth, int requestedHeight,
                                   Options options) throws IOException {
        return decode(is, requestedWidth, requestedHeight, options, EMPTY_CALLBACKS);
    }

    //TODO:decode implations
    public Resource<Bitmap> decode(InputStream is, int requestedWidth, int requestedHeight,
                                   Options options, DecodeCallbacks callbacks) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    public boolean handles(InputStream source) {
        return true;
    }

    /**
     * Callbacks for key points during decodes.
     */
    public interface DecodeCallbacks {
        void onObtainBounds();
        void onDecodeComplete(BitmapPool bitmapPool, Bitmap downsampled) throws IOException;
    }
}
