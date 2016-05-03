package com.lizy.myglide.load.resource.drawable;

import android.graphics.drawable.Drawable;

import com.lizy.myglide.TransitionOptions;
import com.lizy.myglide.request.transtion.DrawableCrossFadeFactory;

/**
 * Created by lizy on 16-5-3.
 */
public class DrawableTransitionOptions extends TransitionOptions<DrawableTransitionOptions, Drawable>{

    public static DrawableTransitionOptions withCrossFade() {
        return new DrawableTransitionOptions().crossFade();
    }

    public static DrawableTransitionOptions withCrossFade(int duration) {
        return new DrawableTransitionOptions().crossFade(duration);
    }

    public static DrawableTransitionOptions withCrossFade(int animationId, int duration) {
        return new DrawableTransitionOptions().crossFade(animationId, duration);
    }

    public DrawableTransitionOptions crossFade() {
        return transition(new DrawableCrossFadeFactory());
    }

    public DrawableTransitionOptions crossFade(int duration) {
        return transition(new DrawableCrossFadeFactory(duration));
    }

    public DrawableTransitionOptions crossFade(int animationId, int duration) {
        return transition(new DrawableCrossFadeFactory(animationId, duration));
    }
}
