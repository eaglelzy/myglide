package com.lizy.myglide.load.model;

import android.content.Context;

/**
 * Created by lizy on 16-4-27.
 */
public interface ModelLoaderFactory<Model, Data> {

    ModelLoader<Model, Data> build(Context context, MultiModelLoaderFactory multiFactory);

    void teardown();
}
