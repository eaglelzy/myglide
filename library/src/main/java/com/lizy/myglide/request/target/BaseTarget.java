package com.lizy.myglide.request.target;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.lizy.myglide.request.Request;
import com.lizy.myglide.request.transtion.Transition;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class BaseTarget<Z> implements Target<Z> {
    private Request request;
    @Override
    public void onLoadStarted(@Nullable Drawable placeHolder) {

    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorHolder) {

    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeHolder) {

    }

    @Override
    public void onResourceReady(Z source, Transition<? super Z> transition) {

    }

    @Override
    public void getSize(SizeReadyCallback callback) {

    }

    @Override
    public void setRequest(@Nullable Request request) {
        this.request = request;
    }

    @Nullable
    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }
}
