package com.lizy.myglide.module;

import android.content.Context;

import com.lizy.myglide.GlideBuilder;
import com.lizy.myglide.Registry;

/**
 * Created by lizy on 16-5-3.
 */
public interface GlideModule {
    void applyOptions(Context context, GlideBuilder glideBuilder);

    void registerComponents(Context context, Registry registry);
}
