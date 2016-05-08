package com.lizy.myglide.load.model;

import android.util.Log;

import com.lizy.myglide.load.Encoder;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lizy on 16-5-5.
 */
public class StreamEncoder implements Encoder<InputStream> {
    private final static String TAG = "StreamEncoder";
    private final ArrayPool arrayPool;

    public StreamEncoder(ArrayPool arrayPool) {
        this.arrayPool = arrayPool;
    }

    @Override
    public boolean encode(InputStream data, File file, Options options) {
        byte[] buffer = arrayPool.get(ArrayPool.STANDARD_BUFFER_SIZE_BYTES, byte[].class);
        boolean isSuccessed = false;
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int length;
            while ((length = data.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            isSuccessed = true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to decode data: " + e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccessed;
    }
}
