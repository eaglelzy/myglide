package com.lizy.myglide.load.engine.bitmap_recycle;

import java.util.Arrays;

/**
 * Created by lizy on 16-4-22.
 */
public final class ByteArrayAdapter implements ArrayAdapterInterface<byte[]> {
    private static final String TAG = "ByteArrayPool";

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public int getArrayLength(byte[] array) {
        return array.length;
    }

    @Override
    public void resetArray(byte[] array) {
        Arrays.fill(array, (byte) 0);
    }

    @Override
    public byte[] newArray(int length) {
        return new byte[length];
    }

    @Override
    public int getElementSizeInBytes() {
        return 1;
    }
}
