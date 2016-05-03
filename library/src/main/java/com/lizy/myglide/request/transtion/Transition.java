package com.lizy.myglide.request.transtion;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by lizy on 16-5-3.
 */
public interface Transition<R> {

    interface ViewAdapter {

        View getView();

        @Nullable
        Drawable getCurrentDrawable();

        void setDrawable(@Nullable Drawable drawable);
    }

    boolean transition(R current, ViewAdapter adapter);
}
