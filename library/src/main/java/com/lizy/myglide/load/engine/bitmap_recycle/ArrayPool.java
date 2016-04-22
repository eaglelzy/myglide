package com.lizy.myglide.load.engine.bitmap_recycle;

/**
 * Created by lizy on 16-4-22.
 */
public interface ArrayPool {

    int STANDARD_BUFFER_SIZE_BYTES = 64 * 1024;

    <T> void put(T array, Class<T> classType);

    <T> T get(int size, Class<T> classType);

    void clearMemory();

    void trimMemory(int level);
}
