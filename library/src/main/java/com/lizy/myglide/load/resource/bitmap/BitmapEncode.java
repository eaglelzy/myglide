package com.lizy.myglide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.util.Log;

import com.lizy.myglide.load.EncodeStrategy;
import com.lizy.myglide.load.Option;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.ResourceEncoder;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.util.LogTime;
import com.lizy.myglide.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by lizy on 16-5-10.
 */
public class BitmapEncode implements ResourceEncoder<Bitmap> {
    private static final String TAG = "BitmapEncode";

    public static final Option<Integer> COMPRESS_QUALITY = Option.memory(
            "com.lizy.myglide.load.resource.bitmap.BitmapEncode.CompressQuality", 90);

    public static final Option<Bitmap.CompressFormat> COMPRESS_FORMAT_OPTION = Option.memory(
            "com.lizy.myglide.load.resource.bitmap.BitmapEncode.CompressFormat");
    @Override
    public EncodeStrategy getEncodeStrategy(Options option) {
        return EncodeStrategy.TRANSFORMED;
    }

    @Override
    public boolean encode(Resource<Bitmap> resource, File file, Options options) {
        long start = LogTime.getLogTime();
        Bitmap bitmap = resource.get();
        boolean isSuccessed = false;
        int quality = options.get(COMPRESS_QUALITY);
        Bitmap.CompressFormat format = getFormat(bitmap, options);
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(format, quality, os);
            isSuccessed = true;
        } catch (FileNotFoundException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Failed to encode bitmap:" + e);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Compressed with type: " + format + " of size " + Util.getBitmapByteSize(bitmap)
                    + " in " + LogTime.getElapsedMillis(start));
        }
        return isSuccessed;
    }

    private Bitmap.CompressFormat getFormat(Bitmap bitmap, Options options) {
        Bitmap.CompressFormat format = options.get(COMPRESS_FORMAT_OPTION);
        if (format != null) {
            return format;
        } else if (bitmap.hasAlpha()) {
            return Bitmap.CompressFormat.PNG;
        } else {
            return Bitmap.CompressFormat.JPEG;
        }
    }
}
