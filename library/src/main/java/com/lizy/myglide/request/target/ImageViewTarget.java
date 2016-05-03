package com.lizy.myglide.request.target;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.lizy.myglide.request.transtion.Transition;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class ImageViewTarget<Z> extends ViewTarget<ImageView, Z>
        implements Transition.ViewAdapter{

    @Nullable
    private Animatable animatable;

    public ImageViewTarget(ImageView view) {
        super(view);
    }

    @Nullable
    @Override
    public Drawable getCurrentDrawable() {
        return view.getDrawable();
    }

    @Override
    public void setDrawable(@Nullable Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeHolder) {
        super.onLoadStarted(placeHolder);
        setResource(null);
        setDrawable(placeHolder);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorHolder) {
        super.onLoadFailed(errorHolder);
        setResource(null);
        setDrawable(errorHolder);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeHolder) {
        super.onLoadCleared(placeHolder);
        setResource(null);
        setDrawable(placeHolder);
    }

    @Override
    public void onResourceReady(Z resource, Transition<? super Z> transition) {
        if (transition != null && !transition.transition(resource, this)) {
            setResource(resource);
        }

        if (resource instanceof Animatable) {
            animatable = (Animatable)resource;
            animatable.start();
        }
    }

    @Override
    public void onStart() {
        if (animatable != null) {
            animatable.start();
        }
    }

    @Override
    public void onStop() {
        if (animatable != null) {
            animatable.stop();
        }
    }

    protected abstract void setResource(@Nullable Z resource);
}
