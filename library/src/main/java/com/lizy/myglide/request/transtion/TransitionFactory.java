package com.lizy.myglide.request.transtion;

import com.lizy.myglide.load.DataSource;

/**
 * Created by lizy on 16-5-3.
 */
public interface TransitionFactory<R> {
    Transition<R> build(DataSource dataSource, boolean isFirstResource);
}
