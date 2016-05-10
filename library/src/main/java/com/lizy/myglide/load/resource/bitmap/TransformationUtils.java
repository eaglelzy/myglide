package com.lizy.myglide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;

import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lizy on 16-5-4.
 */
public class TransformationUtils {

    public static final int PAINT_FLAGS = Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG;
    private static final int CIRCLE_CROP_PAINT_FLAGS = PAINT_FLAGS | Paint.ANTI_ALIAS_FLAG;
    private static final Paint DEFAULT_PAINT = new Paint(PAINT_FLAGS);
    private static final Paint CIRCLE_CROP_SHAPE_PAINT = new Paint(CIRCLE_CROP_PAINT_FLAGS);
    private static final Paint CIRCLE_CROP_BITMAP_PAINT;

    private static final Lock BITMAP_DRAWABLE_LOCK = "XT1097".equals(Build.MODEL)
      // TODO: Switch to Build.VERSION_CODES.LOLLIPOP_MR1 when apps have updated target API levels.
      && Build.VERSION.SDK_INT == 22
      ? new ReentrantLock()
      : new NoLock();

    static {
        CIRCLE_CROP_BITMAP_PAINT = new Paint(CIRCLE_CROP_PAINT_FLAGS);
        CIRCLE_CROP_BITMAP_PAINT.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    public static Lock getBitmapDrawableLock() {
        return BITMAP_DRAWABLE_LOCK;
    }

    public static Bitmap circleCrop(BitmapPool pool,
                                    Bitmap inBitmap,
                                    int destWidth,
                                    int destHeight) {
        int destMinEdge = Math.min(destHeight, destWidth);
        float radius = destMinEdge / 2;

        int srcWidth = inBitmap.getWidth();
        int srcHeight = inBitmap.getHeight();

        float scaleX = destMinEdge / (float)srcWidth;
        float scaleY = destMinEdge / (float)srcHeight;

        float maxScale = Math.max(scaleX, scaleY);

        float scaleWidth = srcWidth * maxScale;
        float scaleHeight = srcHeight * maxScale;
        float left = (destMinEdge - scaleWidth) / 2;
        float top = (destMinEdge - scaleHeight) / 2;

        RectF destRectF = new RectF(left, top, left + scaleWidth, top + scaleHeight);

        Bitmap toTransform = getAlphaSafeBitmap(pool, inBitmap);

        Bitmap result = pool.get(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        setAlphaIfAvailabel(result, true);

        BITMAP_DRAWABLE_LOCK.lock();
        try {
            Canvas canvas = new Canvas(result);
            canvas.drawCircle(radius, radius, radius, CIRCLE_CROP_SHAPE_PAINT);
            canvas.drawBitmap(toTransform, null, destRectF, CIRCLE_CROP_BITMAP_PAINT);
            clear(canvas);
        } finally {
            BITMAP_DRAWABLE_LOCK.unlock();
        }

        return result;
    }

    private static void clear(Canvas canvas) {
        canvas.setBitmap(null);
    }

    private static void setAlphaIfAvailabel(Bitmap bitmap, boolean hasAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 && bitmap != null) {
            bitmap.setHasAlpha(hasAlpha);
        }
    }

    private static Bitmap getAlphaSafeBitmap(BitmapPool pool, Bitmap inBitmap) {
        if (Bitmap.Config.ARGB_8888 == inBitmap.getConfig()) {
            return inBitmap;
        }

        Bitmap argbBitmap = pool.get(inBitmap.getWidth(), inBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(argbBitmap).drawBitmap(inBitmap, 0, 0, null);
        return argbBitmap;
    }

    private static final class NoLock implements Lock {
        @Override
        public void lock() {
            // do nothing
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            // do nothing
        }

        @Override
        public boolean tryLock() {
            return true;
        }

        @Override
        public boolean tryLock(long time, @NonNull TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void unlock() {
            // do nothing
        }

        @NonNull
        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("Should not be called");
        }
    }
}
