package com.lizy.myglide.request.transtion;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by lizy on 16-5-3.
 */
public class ViewTransition<R> implements Transition<R>{

    private final ViewTransitionAnimationFactory viewTransitionAnimationFactory;

    public ViewTransition(ViewTransitionAnimationFactory viewTransitionAnimationFactory) {
        this.viewTransitionAnimationFactory = viewTransitionAnimationFactory;
    }

    @Override
    public boolean transition(R current, ViewAdapter adapter) {
        View view = adapter.getView();
        if (view != null) {
            view.clearAnimation();
            Animation animation = viewTransitionAnimationFactory.build(view.getContext());
            view.startAnimation(animation);
        }
        return false;
    }

    interface ViewTransitionAnimationFactory {
        Animation build(Context context);
    }
}
