package com.lizy.myglide.load.engine;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.Priority;
import com.lizy.myglide.Registry;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.load.model.ModelLoader.LoadData;
import com.lizy.myglide.load.resource.UnitTransformation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lizy on 16-4-28.
 */
public class DecodeHelper<Transcode> {

    private final List<LoadData<?>> loadData = new ArrayList<>();
    private final List<Key> cacheKeys = new ArrayList<>();

    private GlideContext glideContext;
    private Object model;
    private int width;
    private int height;
    private Class<?> resourceClass;
    private DecodeJob.DiskCacheProvider diskCacheProvider;
    private Options options;
    private Map<Class<?>, Transformation<?>> transformations;
    private Class<Transcode> transcodeClass;
    private boolean isLoadDataSet;
    private boolean isCacheKeysSet;
    private Key signature;
    private Priority priority;
    private DiskCacheStrategy diskCacheStrategy;
    private boolean isTransformationRequired;

    @SuppressWarnings("unchecked")
    <R> DecodeHelper<R> init(
            GlideContext glideContext,
            Object model,
            Key signature,
            int width,
            int height,
            DiskCacheStrategy diskCacheStrategy,
            Class<?> resourceClass,
            Class<R> transcodeClass,
            Priority priority,
            Options options,
            Map<Class<?>, Transformation<?>> transformations,
            boolean isTransformationRequired,
            DecodeJob.DiskCacheProvider diskCacheProvider) {
        this.glideContext = glideContext;
        this.model = model;
        this.signature = signature;
        this.width = width;
        this.height = height;
        this.diskCacheStrategy = diskCacheStrategy;
        this.resourceClass = resourceClass;
        this.diskCacheProvider = diskCacheProvider;
        this.transcodeClass = (Class<Transcode>) transcodeClass;
        this.priority = priority;
        this.options = options;
        this.transformations = transformations;
        this.isTransformationRequired = isTransformationRequired;

        return (DecodeHelper<R>) this;
    }

    void clear() {
        glideContext = null;
        model = null;
        signature = null;
        resourceClass = null;
        transcodeClass = null;
        options = null;
        priority = null;
        transformations = null;
        diskCacheStrategy = null;

        loadData.clear();
        isLoadDataSet = false;
        cacheKeys.clear();
        isCacheKeysSet = false;
    }

    <Data> LoadPath<Data, ?, Transcode> getLoadPath(Class<Data> dataClass) {
        return glideContext.getRegistry().getLoadPath(dataClass, resourceClass, transcodeClass);
    }

    @SuppressWarnings("unchecked")
    <Z> Transformation<Z> getTransformation(Class<Z> resourceClass) {
        Transformation<Z> result = (Transformation<Z>) transformations.get(resourceClass);
        if (result == null) {
            if (transformations.isEmpty() && isTransformationRequired) {
                throw new IllegalArgumentException(
                        "Missing transformation for " + resourceClass + ". If you wish to"
                                + " ignore unknown resource types, use the optional transformation methods.");
            } else {
                return UnitTransformation.get();
            }
        }
        return result;
    }

    boolean isSourceKey(Key key) {
        List<LoadData<?>> loadData = getLoadData();
        int size = loadData.size();
        for (int i = 0; i < size; i++) {
            LoadData<?> current = loadData.get(i);
            if (current.sourceKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    List<Key> getCacheKeys() {
        if (!isCacheKeysSet) {
            isCacheKeysSet = true;
            cacheKeys.clear();
            List<LoadData<?>> loadData = getLoadData();
            int size = loadData.size();
            for (int i = 0; i < size; i++) {
                LoadData<?> data = loadData.get(i);
                cacheKeys.add(data.sourceKey);
                cacheKeys.addAll(data.alternateKeys);
            }
        }
        return cacheKeys;
    }

    List<ModelLoader<File, ?>> getModelLoaders(File file)
            throws Registry.NoModelLoaderAvailableException {
        return glideContext.getRegistry().getModelLoaders(file);
    }

    List<LoadData<?>> getLoadData() {
        if (!isLoadDataSet) {
            isLoadDataSet = true;
            loadData.clear();
            List<ModelLoader<Object, ?>> modelLoaders = glideContext.getRegistry().getModelLoaders(model);
            int size = modelLoaders.size();
            for (int i = 0; i < size; i++) {
                ModelLoader<Object, ?> modelLoader = modelLoaders.get(i);
                LoadData<?> current =
                        modelLoader.buildLoadData(model, width, height, options);
                if (current != null) {
                    loadData.add(current);
                }
            }
        }
        return loadData;
    }

}
