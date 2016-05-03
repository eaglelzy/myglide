package com.lizy.myglide.request.transtion;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

/**
 * Created by lizy on 16-5-3.
 */
public class DrawableCrossFadeTransition implements Transition<Drawable>{

    private final Transition<Drawable> transition;
    private final int duration;

    public DrawableCrossFadeTransition(Transition<Drawable> transition, int duration) {
        this.transition = transition;
        this.duration = duration;
    }

    @Override
    public boolean transition(Drawable current, ViewAdapter adapter) {
        Drawable previous = adapter.getCurrentDrawable();
        if (previous != null) {
            TransitionDrawable transitionDrawable =
                    new TransitionDrawable(new Drawable[]{previous, current});
            transitionDrawable.setCrossFadeEnabled(true);
            transitionDrawable.startTransition(duration);
            adapter.setDrawable(current);
            return true;
        } else {
            transition.transition(current, adapter);
            return false;
        }
    }
}
