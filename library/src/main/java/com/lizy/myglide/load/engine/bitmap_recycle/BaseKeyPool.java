package com.lizy.myglide.load.engine.bitmap_recycle;

import android.support.annotation.NonNull;

import com.lizy.myglide.util.Util;

import java.util.Queue;

/**
 * Created by lizy on 16-4-21.
 */
abstract class BaseKeyPool<T extends Poolable> {

    private static final int MAX_SIZE = 20;
    private final Queue<T> queue = Util.createQueue(MAX_SIZE);

    @NonNull
    protected T get() {
        T result = queue.poll();
        if (result == null) {
            result = create();
        }
        return result;
    }

    public void offer(T key) {
        if (queue.size() < MAX_SIZE) {
            queue.offer(key);
        }
    }

    protected abstract T create();
}
