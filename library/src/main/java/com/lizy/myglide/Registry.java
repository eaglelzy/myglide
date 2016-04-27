package com.lizy.myglide;

import android.content.Context;
import android.support.v4.util.Pools;

import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.load.model.ModelLoaderFactory;
import com.lizy.myglide.load.model.ModelLoaderRegistry;
import com.lizy.myglide.util.pool.FactoryPools;

import java.util.List;

/**
 * Created by lizy on 16-4-27.
 */
public class Registry {

    private final ModelLoaderRegistry modelLoaderRegistry;

    private final Pools.Pool<List<Exception>> exceptionListPool = FactoryPools.threadSafeList();

    public Registry(Context context) {
        this.modelLoaderRegistry =
                new ModelLoaderRegistry(context.getApplicationContext(), exceptionListPool);
    }

    public <Model, Data> Registry append(Class<Model> modelClass, Class<Data> dataClass,
                                         ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.append(modelClass, dataClass, factory);
        return this;
    }

    public <Model, Data> Registry prepend(Class<Model> modelClass, Class<Data> dataClass,
                                          ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.prepend(modelClass, dataClass, factory);
        return this;
    }

    public <Model, Data> Registry replace(Class<Model> modelClass, Class<Data> dataClass,
                                          ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.replace(modelClass, dataClass, factory);
        return this;
    }

    public <Model> List<ModelLoader<Model, ?>> getModelLoaders(Model model) {
        List<ModelLoader<Model, ?>> result = modelLoaderRegistry.getModelLoaders(model);
        if (result.isEmpty()) {
            throw new NoModelLoaderAvailableException(model);
        }
        return result;
    }

    /**
     * Thrown when no {@link com.lizy.myglide.load.model.ModelLoader} is registered for a given
     * model class.
     */
    public static class NoModelLoaderAvailableException extends MissingComponentException {
        public NoModelLoaderAvailableException(Object model) {
            super("Failed to find any ModelLoaders for model: " + model);
        }

        public NoModelLoaderAvailableException(Class modelClass, Class dataClass) {
            super("Failed to find any ModelLoaders for model: " + modelClass + " and data: " + dataClass);
        }
    }

    /**
     * Thrown when some necessary component is missing for a load.
     */
    public static class MissingComponentException extends RuntimeException {
        public MissingComponentException(String message) {
            super(message);
        }
    }
}
