package com.lizy.myglide.load.resource.bitmap;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.lizy.myglide.load.DecodeFormat;
import com.lizy.myglide.load.Option;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.load.resource.bitmap.DownsampleStrategy.SampleSizeRounding;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.util.Preconditions;
import com.lizy.myglide.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;

/**
 * Created by lizy on 16-4-29.
 */
public class Downsampler {
    private final BitmapPool bitmapPool;
    private final ArrayPool byteArrayPool;
    private DisplayMetrics displayMetrics;

    private static final String TAG = "Downsampler";

    private final static int MARK_POSITION = 5 * 1024 * 1024;

    public static final Option<DecodeFormat> DECODE_FORMAT = Option.memory(
        "com.bumptech.glide.load.resource.bitmap.Downsampler.DecodeFormat", DecodeFormat.DEFAULT);

    private static final Set<ImageHeaderParser.ImageType> TYPES_THAT_USE_POOL_PRE_KITKAT =
            Collections.unmodifiableSet(
                    EnumSet.of(
                            ImageHeaderParser.ImageType.JPEG,
                            ImageHeaderParser.ImageType.PNG_A,
                            ImageHeaderParser.ImageType.PNG
                    )
            );

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
                        BitmapFactory.Options options,
                        DownsampleStrategy downsampleStrategy,
                        DecodeFormat decodeFormat,
                        int requestedWidth,
                        int requestedHeight,
                        DecodeCallbacks callbacks) throws IOException {
        int[] sourceDimensions = getDimensions(is, options, callbacks);
        int sourceWidth = sourceDimensions[0];
        int sourceHeight = sourceDimensions[1];
        String sourceMimeType = options.outMimeType;

        int orientation = getOrientation(is);
        int degressToRotate = TransformationUtils.getExitOrientaionDegrees(orientation);

        options.inPreferredConfig = getConfig(is, decodeFormat);
        if (options.inPreferredConfig != Bitmap.Config.ARGB_8888) {
            options.inDither = true;
        }

        calculateScaling(downsampleStrategy, degressToRotate, sourceWidth, sourceHeight,
                requestedWidth, requestedHeight, options);

        Bitmap downsampled = downsampleWithSize(is, options, bitmapPool,
                sourceWidth, sourceHeight, callbacks);
        callbacks.onDecodeComplete(bitmapPool, downsampled);

        Bitmap rotated = null;
        if (downsampled != null) {
            // If we scaled, the Bitmap density will be our inTargetDensity. Here we correct it back to
            // the expected density dpi.
            downsampled.setDensity(displayMetrics.densityDpi);

            rotated = TransformationUtils.rotateImageExif(bitmapPool, downsampled, orientation);
            if (!downsampled.equals(rotated)) {
                bitmapPool.put(downsampled);
            }
        }

        return rotated;
    }

    private static void calculateScaling(
                    DownsampleStrategy downsampleStrategy,
                    int degressToRotate,
                    int sourceWidth,
                    int sourceHeight,
                    int requestedWidth,
                    int requestedHeight,
                    BitmapFactory.Options options) {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }

        int targetWidth = requestedWidth == Target.SIZE_ORIGINAL ? sourceWidth : requestedWidth;
        int targetHeight = requestedHeight == Target.SIZE_ORIGINAL ? sourceHeight : requestedHeight;

        final float exactScaleFactor;
        if (degressToRotate == 90 || degressToRotate == 270) {
            exactScaleFactor = downsampleStrategy.getScaleFactor(sourceHeight, sourceWidth,
                    requestedWidth, requestedHeight);
        } else {
            exactScaleFactor = downsampleStrategy.getScaleFactor(sourceWidth, sourceHeight,
                    requestedWidth, requestedHeight);
        }

        if (exactScaleFactor <= 0f) {
            throw new IllegalArgumentException("cannot scale with factor: " + exactScaleFactor
                        + " with downsampleStrategy: " + downsampleStrategy);
        }

        SampleSizeRounding rounding = downsampleStrategy.getSampleSizeRounding(sourceWidth,
            sourceHeight, targetWidth, targetHeight);
        if (rounding == null) {
            throw new IllegalArgumentException("Cannot round with null rounding");
        }

        int outWidth = (int)(exactScaleFactor * targetWidth + 0.5f);
        int outHeight = (int)(exactScaleFactor * targetHeight + 0.5f);

        int widthScaleFactor = sourceWidth / outWidth;
        int heightScaleFactor = sourceHeight / outHeight;

        int scaleFactor = rounding == SampleSizeRounding.MEMORY
                ? Math.max(widthScaleFactor, heightScaleFactor)
                : Math.min(widthScaleFactor, heightScaleFactor);

        int powerOfTwoSampleSize = Math.max(1, Integer.highestOneBit(scaleFactor));
        if (rounding == SampleSizeRounding.MEMORY && powerOfTwoSampleSize < (1.f / exactScaleFactor)) {
            powerOfTwoSampleSize = powerOfTwoSampleSize << 1;
        }

        float adjustedScaleFactor = powerOfTwoSampleSize * exactScaleFactor;

        options.inSampleSize = powerOfTwoSampleSize;
        // Density scaling is only supported if inBitmap is null prior to KitKat. Avoid setting
        // densities here so we calculate the final Bitmap size correctly.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            options.inTargetDensity = (int) (1000 * adjustedScaleFactor + 0.5f);
            options.inDensity = 1000;
        }
        if (isScaling(options)) {
            options.inScaled = true;
        } else {
            options.inDensity = options.inTargetDensity = 0;
        }

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Calculate scaling\n"
                    + "source: [" + sourceWidth + "x" + sourceHeight + "]\n"
                    + "target: [" + targetWidth + "x" + targetHeight + "]\n"
                    + "exact scale factor: " + exactScaleFactor + "\n"
                    + "power of 2 sample size: " + powerOfTwoSampleSize + "\n"
                    + "adjusted scale factor: " + adjustedScaleFactor + "\n"
                    + "target density: " + options.inTargetDensity + "\n"
                    + "density: " + options.inDensity);
        }
    }

    private Bitmap downsampleWithSize(InputStream is, BitmapFactory.Options options,
                                      BitmapPool pool, int sourceWidth, int sourceHeight,
                                      DecodeCallbacks callbacks) throws IOException {
        // Prior to KitKat, the inBitmap size must exactly match the size of the bitmap we're decoding.
        if ((options.inSampleSize == 1 || Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT)
                && shouldUsePool(is)) {

            float densityMultiplier = isScaling(options)
                    ? (float) options.inTargetDensity / options.inDensity : 1f;

            int sampleSize = options.inSampleSize;
            int downsampledWidth = (int) Math.ceil(sourceWidth / (float) sampleSize);
            int downsampledHeight = (int) Math.ceil(sourceHeight / (float) sampleSize);
            int expectedWidth = Math.round(downsampledWidth * densityMultiplier);
            int expectedHeight = Math.round(downsampledHeight * densityMultiplier);

            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Calculated target [" + expectedWidth + "x" + expectedHeight + "] for source\n"
                        + "[" + sourceWidth + "x" + sourceHeight + "]\n"
                        + "sampleSize: " + sampleSize + "\n"
                        + "targetDensity: " + options.inTargetDensity + "\n"
                        + "density: " + options.inDensity + "\n"
                        + "density multiplier: " + densityMultiplier + "\n");
            }
            // If this isn't an image, or BitmapFactory was unable to parse the size, width and height
            // will be -1 here.
            if (expectedWidth > 0 && expectedHeight > 0) {
                setInBitmap(options, pool, expectedWidth, expectedHeight, options.inPreferredConfig);
            }
        }
        return decodeStream(is, options, callbacks);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void setInBitmap(BitmapFactory.Options options, BitmapPool bitmapPool, int width,
                                    int height, Bitmap.Config config) {
        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            // BitmapFactory will clear out the Bitmap before writing to it, so getDirty is safe.
            options.inBitmap = bitmapPool.getDirty(width, height, config);
        }
    }

    private boolean shouldUsePool(InputStream is) throws IOException {
        // On KitKat+, any bitmap (of a given config) can be used to decode any other bitmap
        // (with the same config).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return true;
        }

        is.mark(MARK_POSITION);
        try {
            final ImageHeaderParser.ImageType type = new ImageHeaderParser(is, byteArrayPool).getType();
            // We cannot reuse bitmaps when decoding images that are not PNG or JPG prior to KitKat.
            // See: https://groups.google.com/forum/#!msg/android-developers/Mp0MFVFi1Fo/e8ZQ9FGdWdEJ
            return TYPES_THAT_USE_POOL_PRE_KITKAT.contains(type);
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Cannot determine the image type from header", e);
            }
        } finally {
            is.reset();
        }
        return false;
    }

    private static boolean isScaling(BitmapFactory.Options options) {
        return options.inTargetDensity > 0 && options.inDensity > 0
                && options.inTargetDensity != options.inDensity;
    }

    private Bitmap.Config getConfig(InputStream is, DecodeFormat decodeFormat) throws IOException {
        if (decodeFormat == DecodeFormat.PREFER_ARGB_8888
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            return Bitmap.Config.ARGB_8888;
        }

        boolean hasAlpha = false;
        is.mark(MARK_POSITION);
        try {
            hasAlpha = new ImageHeaderParser(is, byteArrayPool).hasAlpha();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.reset();
        }

        return hasAlpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
    }

    //TODO:
    private int getOrientation(InputStream is) throws IOException {
        is.mark(MARK_POSITION);
        int orientation = ImageHeaderParser.UNKNOWN_ORIENTATION;
        try {
            orientation = new ImageHeaderParser(is, byteArrayPool).getOrientation();
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "cannot determine orientation from image header:" + e);
            }
        }finally {
            is.reset();
        }
        return orientation;
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
