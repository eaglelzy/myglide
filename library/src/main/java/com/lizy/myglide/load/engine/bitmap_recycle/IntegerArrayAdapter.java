package com.lizy.myglide.load.engine.bitmap_recycle;

import java.util.Arrays;

/**
 * Created by lizy on 16-4-22.
 */
public class IntegerArrayAdapter implements ArrayAdapterInterface<int[]> {
    private static final String TAG = "IntegerArrayPool";

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public int getArrayLength(int[] array) {
        return array.length;
    }

    @Override
    public void resetArray(int[] array) {
        Arrays.fill(array, 0);
    }

    @Override
    public int[] newArray(int length) {
        return new int[length];
    }

    @Override
    public int getElementSizeInBytes() {
        return 4;
    }
}
