package com.lizy.myglide.request.transtion;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.lizy.myglide.load.DataSource;

/**
 * Created by lizy on 16-5-3.
 */
public class ViewAnimationFactory<R> implements TransitionFactory<R> {

    private ViewTransition.ViewTransitionAnimationFactory viewTransitionAnimationFactory;
    private Transition<R> transition;

    public ViewAnimationFactory(Animation animation) {
        this(new ConcreteViewTransitionAnimationFactory(animation));
    }

    public ViewAnimationFactory(int animationId) {
        this(new ResourceViewTransitionAnimationFactory(animationId));
    }

    public ViewAnimationFactory(ViewTransition.ViewTransitionAnimationFactory
                                        viewTransitionAnimationFactory) {
        this.viewTransitionAnimationFactory = viewTransitionAnimationFactory;
    }

    @Override
    public Transition<R> build(DataSource dataSource, boolean isFirstResource) {
        if (dataSource == DataSource.MEMORY_CACHE || !isFirstResource) {
            return NoTransition.get();
        }

        if (transition == null) {
            transition = new ViewTransition<>(viewTransitionAnimationFactory);
        }
        return transition;
    }

    private static class ConcreteViewTransitionAnimationFactory implements
            ViewTransition.ViewTransitionAnimationFactory {

        private final Animation animation;

        public ConcreteViewTransitionAnimationFactory(Animation animation) {
            this.animation = animation;
        }

        @Override
        public Animation build(Context context) {
            return animation;
        }
    }

    private static class ResourceViewTransitionAnimationFactory implements
            ViewTransition.ViewTransitionAnimationFactory {
        private final int animationId;

        public ResourceViewTransitionAnimationFactory(int animationId) {
            this.animationId = animationId;
        }

        @Override
        public Animation build(Context context) {
            return AnimationUtils.loadAnimation(context, animationId);
        }
    }
}
