package com.lizy.myglide.request.transtion;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.lizy.myglide.load.DataSource;

/**
 * Created by lizy on 16-5-3.
 */
public class DrawableCrossFadeFactory implements TransitionFactory<Drawable>{
    private static final int DEFAULT_DURATION = 300;
    private DrawableCrossFadeTransition firstResourceTransition;
    private DrawableCrossFadeTransition secondResourceTransition;

    private final ViewAnimationFactory<Drawable> viewAnimationFactory;
    private final int duration;

    public DrawableCrossFadeFactory() {
        this(DEFAULT_DURATION);
    }

    public DrawableCrossFadeFactory(int duration) {
        this(new ViewAnimationFactory<Drawable>(new DefaultCrossFadeAnimationFactory(duration)), duration);
    }
    public DrawableCrossFadeFactory(int animationId, int duration) {
        this(new ViewAnimationFactory<Drawable>(animationId), duration);
    }

    public DrawableCrossFadeFactory(ViewAnimationFactory<Drawable> viewAnimationFactory, int duration) {
        this.viewAnimationFactory = viewAnimationFactory;
        this.duration = duration;
    }

    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        if (dataSource == DataSource.MEMORY_CACHE) {
            return NoTransition.get();
        } else if (isFirstResource) {
            return getFirstResourceTransition(dataSource);
        } else {
            return getSecondResourceTransition(dataSource);
        }
    }

    private DrawableCrossFadeTransition getFirstResourceTransition(DataSource dataSource) {
        if (firstResourceTransition == null) {
            Transition<Drawable> defautTransition =
                    viewAnimationFactory.build(dataSource, true);
            firstResourceTransition = new DrawableCrossFadeTransition(defautTransition, duration);
        }
        return firstResourceTransition;
    }

    private DrawableCrossFadeTransition getSecondResourceTransition(DataSource dataSource) {
        if (secondResourceTransition == null) {
            Transition<Drawable> defautTransition =
                    viewAnimationFactory.build(dataSource, false);
            secondResourceTransition = new DrawableCrossFadeTransition(defautTransition, duration);
        }
        return secondResourceTransition;
    }

    private static class DefaultCrossFadeAnimationFactory implements
            ViewTransition.ViewTransitionAnimationFactory {

        private final int duration;

        public DefaultCrossFadeAnimationFactory(int duration) {
            this.duration = duration;
        }

        @Override
        public Animation build(Context context) {
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(duration);
            return animation;
        }
    }
}
