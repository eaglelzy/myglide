package com.lizy.myglide;


import com.lizy.myglide.request.transtion.NoTransition;
import com.lizy.myglide.request.transtion.TransitionFactory;
import com.lizy.myglide.request.transtion.ViewAnimationFactory;
import com.lizy.myglide.util.Preconditions;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class TransitionOptions<CHILD extends TransitionOptions<CHILD, TranscodeType>, TranscodeType>
        implements Cloneable {

    private TransitionFactory<? super TranscodeType> transitionFactory = NoTransition.getFactory();

    public final CHILD dontTransition() {
        return transition(NoTransition.getFactory());
    }

    public final CHILD transition(int viewAnimationId) {
        return transition(new ViewAnimationFactory<TranscodeType>(viewAnimationId));
    }

    public final CHILD transition(TransitionFactory<? super TranscodeType> transitionFactory) {
        this.transitionFactory = Preconditions.checkNotNull(transitionFactory);
        return self();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final CHILD clone() {
        try {
            return (CHILD) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    final TransitionFactory<? super TranscodeType> getTransitionFactory() {
        return transitionFactory;
    }

    @SuppressWarnings("unchecked")
    private CHILD self() {
        return (CHILD) this;
    }

}
