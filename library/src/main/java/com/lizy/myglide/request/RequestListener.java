package com.lizy.myglide.request;

import android.support.annotation.Nullable;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.request.target.Target;

/**
 * Created by lizy on 16-5-5.
 */
public interface RequestListener<R> {

    boolean onLoadFailed(@Nullable GlideContext context, Object model, Target<R> target,
                         boolean isFirstResource);

    boolean onResourceReady(R resource, Object model, Target<R> target, DataSource dataSource,
                            boolean isFirstResource);
}
