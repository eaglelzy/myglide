package com.lizy.myglide.load.resource.bitmap;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;

import com.lizy.myglide.load.DecodeFormat;
import com.lizy.myglide.load.Option;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.util.Preconditions;
import com.lizy.myglide.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

/**
 * Created by lizy on 16-4-29.
 */
public class Downsampler {
    private final BitmapPool bitmapPool;
    private final ArrayPool byteArrayPool;
    private DisplayMetrics displayMetrics;

    private final static int MARK_POSITION = 5 * 1024 * 1024;

    public static final Option<DecodeFormat> DECODE_FORMAT = Option.memory(
        "com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeFormat", DecodeFormat.DEFAULT);

    public static final Option<DownsampleStrategy> DOWNSAMPLE_STRATEGY =
        Option.memory("com.lizy.myglide.load.resource.bitmap.Downsampler.DownsampleStrategy",
            DownsampleStrategy.AT_LEAST);

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

    public Resource<Bitmap> decode(InputStream is, int requestedWidth, int requestedHeight,
                                   Options options, DecodeCallbacks callbacks) throws IOException {

        Preconditions.checkArgument(is.markSupported(), "You must provide an inputstream support mark()");

        byte[] bytesForOptions = byteArrayPool.get(ArrayPool.STANDARD_BUFFER_SIZE_BYTES, byte[].class);
        BitmapFactory.Options bitmapFactoryOptions = getDefaltOptions();
        bitmapFactoryOptions.inTempStorage = bytesForOptions;

        DecodeFormat decodeFormat = options.get(DECODE_FORMAT);
        DownsampleStrategy downsampleStrategy = options.get(DOWNSAMPLE_STRATEGY);

        try {
            Bitmap result = decodeFromWrappedInputStream(is, bitmapFactoryOptions, downsampleStrategy,
                    decodeFormat, requestedWidth, requestedHeight, callbacks);
            return BitmapResource.obtain(result, bitmapPool);
        } finally {
            releaseOptions(bitmapFactoryOptions);
            byteArrayPool.put(bytesForOptions, byte[].class);
        }
    }

    private Bitmap decodeFromWrappedInputStream(
                        InputStream is,
                        BitmapFactory.Options bitmapFactoryOptions,
                        DownsampleStrategy downsampleStrategy,
                        DecodeFormat decodeFormat,
                        int requestedWidth,
                        int requestedHeight,
                        DecodeCallbacks callbacks) throws IOException {
        int[] sourceDimensions = getDimensions(is, bitmapFactoryOptions, callbacks);
        int sourceWidth = sourceDimensions[0];
        int sourceHeight = sourceDimensions[1];
        String sourceMimeType = bitmapFactoryOptions.outMimeType;

        bitmapFactoryOptions.inSampleSize = 1;

        int orientation = getOrientation(is);

        //TODO:
        return decodeStream(is, bitmapFactoryOptions, callbacks);
    }

    //TODO:
    private int getOrientation(InputStream is) {
        return 0;
    }

    private static int[] getDimensions(
            InputStream is,
            BitmapFactory.Options options,
            DecodeCallbacks callbacks) throws IOException {
        options.inJustDecodeBounds = true;
        decodeStream(is, options, callbacks);
        options.inJustDecodeBounds = false;
        return new int[]{options.outWidth, options.outHeight};
    }

    private static Bitmap decodeStream(
            InputStream is,
            BitmapFactory.Options options,
            DecodeCallbacks callbacks) throws IOException {
        if (options.inJustDecodeBounds) {
            is.mark(MARK_POSITION);
        } else {
            callbacks.onObtainBounds();
        }

        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;
        String outMimeType = options.outMimeType;
        final Bitmap result;
        TransformationUtils.getBitmapDrawableLock().lock();
        try {
            result = BitmapFactory.decodeStream(is, null, options);
        } catch (IllegalArgumentException e) {
            throw newIoExceptionForInBitmapAssertion(e, sourceWidth, sourceHeight, outMimeType, options);
        } finally {
            TransformationUtils.getBitmapDrawableLock().unlock();
        }

        if (options.inJustDecodeBounds) {
            is.reset();
        }
        return result;
    }

    private static final Queue<BitmapFactory.Options> OPTIONS_QUEUE = Util.createQueue(0);

    private synchronized static BitmapFactory.Options getDefaltOptions() {
        BitmapFactory.Options result;
        synchronized (OPTIONS_QUEUE) {
            result = OPTIONS_QUEUE.poll();
        }
        if (result == null) {
            result = new BitmapFactory.Options();
            resetOptions(result);
        }
        return result;
    }

    private static void releaseOptions(BitmapFactory.Options decodeBitmapOptions) {
        resetOptions(decodeBitmapOptions);
        synchronized (OPTIONS_QUEUE) {
            OPTIONS_QUEUE.offer(decodeBitmapOptions);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void resetOptions(BitmapFactory.Options decodeBitmapOptions) {
        decodeBitmapOptions.inTempStorage = null;
        decodeBitmapOptions.inDither = false;
        decodeBitmapOptions.inScaled = false;
        decodeBitmapOptions.inSampleSize = 1;
        decodeBitmapOptions.inPreferredConfig = null;
        decodeBitmapOptions.inJustDecodeBounds = false;
        decodeBitmapOptions.inDensity = 0;
        decodeBitmapOptions.inTargetDensity = 0;
        decodeBitmapOptions.outWidth = 0;
        decodeBitmapOptions.outHeight = 0;
        decodeBitmapOptions.outMimeType = null;

        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            decodeBitmapOptions.inBitmap = null;
            decodeBitmapOptions.inMutable = true;
        }
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static IOException newIoExceptionForInBitmapAssertion(
            IllegalArgumentException e,
            int outWidth, int outHeight, String outMimeType, BitmapFactory.Options options) {
        return new IOException("Exception decoding bitmap"
                + ", outWidth: " + outWidth
                + ", outHeight: " + outHeight
                + ", outMimeType: " + outMimeType
                + ", inBitmap: " + getInBitmapString(options), e);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static String getInBitmapString(BitmapFactory.Options options) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                ? getBitmapString(options.inBitmap) : null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getBitmapString(Bitmap bitmap) {
        final String result;
        if (bitmap == null) {
            result = null;
        } else {
            String sizeString = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    ? " (" + bitmap.getAllocationByteCount() + ")" : "";
            result = "[" + bitmap.getWidth() + "x" + bitmap.getHeight() + "] " + bitmap.getConfig()
                    + sizeString;
        }
        return result;
    }

}
