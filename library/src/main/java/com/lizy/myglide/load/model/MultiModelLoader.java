package com.lizy.myglide.load.model;

import android.support.v4.util.Pools;

import com.lizy.myglide.load.Options;

import java.util.List;

/**
 * Created by lizy on 16-4-27.
 */
public class MultiModelLoader<Model, Data> implements ModelLoader<Model, Data> {

    private final List<ModelLoader<Model, Data>> modelLoaders;
    private final Pools.Pool<List<Exception>> exceptionListPool;

    public MultiModelLoader(List<ModelLoader<Model, Data>> loaders, Pools.Pool<List<Exception>> exceptionListPool) {
        this.modelLoaders = loaders;
        this.exceptionListPool = exceptionListPool;
    }

    @Override
    public LoadData<Data> buildLoadData(Model model, int width, int height, Options Options) {
        return null;
    }

    @Override
    public boolean handles(Model model) {
        return true;
    }

    @Override
    public String toString() {
        return "MultiModelLoader{" +
                "modelLoaders=" + modelLoaders + "}\n";
    }
}
