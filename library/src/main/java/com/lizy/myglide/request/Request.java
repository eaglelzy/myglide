package com.lizy.myglide.request;

/**
 * Created by lizy on 16-5-1.
 */
public interface Request {

    void begin();

    void pause();

    void clear();

    boolean isPause();

    boolean isRunning();

    boolean isComplete();

    boolean isResourceSet();

    boolean isCanceled();

    boolean isFailed();

    void recycle();
}
