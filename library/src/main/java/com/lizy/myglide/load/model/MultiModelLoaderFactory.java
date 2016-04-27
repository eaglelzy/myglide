package com.lizy.myglide.load.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools.Pool;
import android.util.Log;

import com.lizy.myglide.Registry;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.util.Preconditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by lizy on 16-4-27.
 */
public class MultiModelLoaderFactory {
    private final static String TAG_REGISTRY = "Registry";
    private final static Factory DEFAULT_FACTORY = new Factory();
    private static final ModelLoader<Object, Object> EMPTY_MODEL_LOADER = new EmptyModelLoader();
    private final Context context;
    private final List<Entry<?, ?>> entries = new ArrayList<>();
    private final Pool<List<Exception>> exceptionListPool;
    private final Set<Entry<?, ?>> alreadyUsedEntries = new HashSet<>();
    private final Factory factory;

    public MultiModelLoaderFactory(Context context, Pool<List<Exception>> exceptionListPool) {
        this(context, exceptionListPool, DEFAULT_FACTORY);
    }

    synchronized <Model, Data> void append(Class<Model> modelClass, Class<Data> dataClass,
                                           ModelLoaderFactory<Model, Data> factory) {
        add(modelClass, dataClass, factory, true);
    }

    synchronized <Model, Data> void prepend(Class<Model> modelClass, Class<Data> dataClass,
                                           ModelLoaderFactory<Model, Data> factory) {
        add(modelClass, dataClass, factory, false);
    }

    private <Model, Data> void add(Class<Model> modelClass, Class<Data> dataClass,
                           ModelLoaderFactory<Model, Data> factory, boolean append) {
        Entry<Model, Data> entry = new Entry<>(modelClass, dataClass, factory);
        entries.add(append ? entries.size() : 0, entry);
    }

    synchronized <Model, Data> List<ModelLoaderFactory<Model, Data>> replace(Class<Model> modelClass,
                             Class<Data> dataClass, ModelLoaderFactory<Model, Data> factory) {
        List<ModelLoaderFactory<Model, Data>> removed = remove(modelClass, dataClass);
        append(modelClass, dataClass, factory);
        return removed;
    }

    synchronized <Model, Data> List<ModelLoaderFactory<Model, Data>> remove(Class<Model> modelClass,
                           Class<Data> dataClass) {
        List<ModelLoaderFactory<Model, Data>> factories = new ArrayList<>();
        Iterator<Entry<?, ?>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry<?, ?> entry = iterator.next();
            if (entry.handles(modelClass, dataClass)) {
                iterator.remove();
                factories.add(this.<Model, Data>getFactory(entry));
            }
        }
        return factories;
    }

    synchronized List<Class<?>> getDataClasses(Class<?> modelClass) {
        List<Class<?>> results = new ArrayList<>();
        for (Entry<?, ?> entry : entries) {
            if (!results.contains(entry.dataClass) && entry.handles(modelClass)) {
                results.add(entry.dataClass);
            }
        }
        return results;
    }

    synchronized <Model> List<ModelLoader<Model, ?>> build(Class<Model> modelClass) {
        try {
            List<ModelLoader<Model, ?>> loaders = new ArrayList<>();
            for (Entry<?, ?> entry : entries) {
                // Avoid stack overflow recursively creating model loaders by only creating loaders in
                // recursive requests if they haven't been created earlier in the chain. For example:
                // A Uri loader may translate to another model, which in turn may translate back to a Uri.
                // The original Uri loader won't be provided to the intermediate model loader, although
                // other Uri loaders will be.
                if (alreadyUsedEntries.contains(entry)) {
                    continue;
                }
                if (entry.handles(modelClass)) {
                    alreadyUsedEntries.add(entry);
                    loaders.add(this.<Model, Object>build(entry));
                    alreadyUsedEntries.remove(entry);
                }
            }
            return loaders;
        } catch (Throwable t) {
            alreadyUsedEntries.clear();
            throw t;
        }
    }

    public synchronized <Model, Data> ModelLoader<Model, Data> build(Class<Model> modelClass,
                         Class<Data> dataClass) {
        try {
            List<ModelLoader<Model, Data>> loaders = new ArrayList<>();
            boolean ignoredAnyEntries = false;
            for (Entry<?, ?> entry : entries) {
                if (alreadyUsedEntries.contains(entry)) {
                    ignoredAnyEntries = true;
                    continue;
                }
                if (entry.handles(modelClass, dataClass)) {
                    alreadyUsedEntries.add(entry);
                    loaders.add(this.<Model, Data>build(entry));
                    alreadyUsedEntries.remove(entry);
                }
            }
            System.out.print("loaders=" + loaders);
            if (loaders.size() > 1) {
                return factory.build(loaders, exceptionListPool);
            } else if (loaders.size() == 1) {
                return loaders.get(0);
            } else {
                // Avoid crashing if recursion results in no loaders available. The assertion is supposed to
                // catch completely unhandled types, recursion may mean a subtype isn't handled somewhere
                // down the stack, which is often ok.
                if (ignoredAnyEntries) {
                    return emptyModelLoader();
                } else {
                    throw new Registry.NoModelLoaderAvailableException(modelClass, dataClass);
                }
            }
        } catch (Throwable t) {
            alreadyUsedEntries.clear();
            throw t;
        }
    }

    private <Model, Data> ModelLoader<Model, Data> build(Entry<?, ?> entry) {
        return (ModelLoader<Model, Data>) Preconditions
                .checkNotNull(entry.factory.build(context, this));
    }

    private <Model, Data> ModelLoaderFactory<Model,Data> getFactory(Entry<?, ?> entry) {
        return (ModelLoaderFactory<Model, Data>)entry.factory;
    }

    public MultiModelLoaderFactory(Context context, Pool<List<Exception>> exceptionListPool,
                                   Factory factory) {
        this.context = context.getApplicationContext();
        this.exceptionListPool = exceptionListPool;
        this.factory = factory;
    }

    static class Factory {
        public <Model, Data> MultiModelLoader<Model, Data> build(
                List<ModelLoader<Model, Data>> loaders,
                Pool<List<Exception>> exceptionListPool) {
            return new MultiModelLoader<>(loaders, exceptionListPool);
        }
    }

    private static <Model, Data> ModelLoader<Model, Data> emptyModelLoader() {
        return (ModelLoader<Model, Data>) EMPTY_MODEL_LOADER;
    }

    private static class Entry<Model, Data> {
        private final Class<Model> modelClass;
        private final Class<Data> dataClass;
        private final ModelLoaderFactory<Model, Data> factory;

        public Entry(Class<Model> modelClass, Class<Data> dataClass,
                      ModelLoaderFactory<Model, Data> factory) {
            this.modelClass = modelClass;
            this.dataClass = dataClass;
            this.factory = factory;
        }

        public boolean handles(Class<?> modelClass, Class<?> dataClass) {
            return handles(modelClass) && this.dataClass.isAssignableFrom(dataClass);
        }
        public boolean handles(Class<?> modelClass) {
            return this.modelClass.isAssignableFrom(modelClass);
        }

        @Override
        public String toString() {
            return "[" + modelClass.getSimpleName()
                    + "-" + dataClass.getSimpleName()
                    + "-" + factory.getClass().getSimpleName() + "]\n";
        }
    }

    private static class EmptyModelLoader implements ModelLoader<Object, Object> {

        @Nullable
        @Override
        public LoadData<Object> buildLoadData(Object o, int width, int height, Options options) {
            return null;
        }

        @Override
        public boolean handles(Object o) {
            return false;
        }
    }

    public void dump() {
        if (Log.isLoggable(TAG_REGISTRY, Log.DEBUG)) {
            Log.d(TAG_REGISTRY, "modelloaders=" + entries.toString());
        }
    }
}
