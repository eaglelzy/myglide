package com.lizy.myglide.request.target;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.lizy.myglide.manager.LifecycleListener;
import com.lizy.myglide.request.transtion.Transition;

/**
 * Created by lizy on 16-5-3.
 */
public interface Target<R> extends LifecycleListener {

    int SIZE_ORIGINAL = Integer.MIN_VALUE;

    void onLoadStarted(@Nullable Drawable placeHolder);

    void onLoadFailed(@Nullable Drawable errorHolder);

    void onLoadCleared(@Nullable Drawable placeHolder);

    void onResourceReady(R source, Transition<? super R> transition);

    void getSize(SizeReadyCallback callback);

    void setRequest(@Nullable Request request);


    @Nullable
    Request getRequest();
}
