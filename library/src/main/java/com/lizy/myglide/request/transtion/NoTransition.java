package com.lizy.myglide.request.transtion;

import com.lizy.myglide.load.DataSource;

/**
 * Created by lizy on 16-5-3.
 */
public class NoTransition<R> implements Transition<R> {
    private static final NoTransition<?> NO_ANIMATION = new NoTransition<Object>();

    private static final NoTransitionFactory<?> NO_ANIMATION_FACTORY = new NoTransitionFactory<Object>();

    private static class NoTransitionFactory<R> implements TransitionFactory<R> {

        @Override
        public Transition<R> build(DataSource dataSource, boolean isFirstResource) {
            return (Transition<R>) NO_ANIMATION;
        }
    }

    public static <R> NoTransitionFactory<R> getFactory() {
        return (NoTransitionFactory<R>) NO_ANIMATION_FACTORY;
    }

    public static <R> NoTransition<R> get() {
        return (NoTransition<R>) NO_ANIMATION;
    }

    @Override
    public boolean transition(R current, ViewAdapter adapter) {
        return false;
    }
}
