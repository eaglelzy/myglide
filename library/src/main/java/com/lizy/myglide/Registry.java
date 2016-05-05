package com.lizy.myglide;

import android.content.Context;
import android.support.v4.util.Pools;

import com.lizy.myglide.load.Encoder;
import com.lizy.myglide.load.ResourceDecoder;
import com.lizy.myglide.load.engine.DecodePath;
import com.lizy.myglide.load.engine.LoadPath;
import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.load.model.ModelLoaderFactory;
import com.lizy.myglide.load.model.ModelLoaderRegistry;
import com.lizy.myglide.load.provider.LoadPathCache;
import com.lizy.myglide.load.provider.ResourceDecodeRegistry;
import com.lizy.myglide.load.resource.transcode.ResourceTranscoder;
import com.lizy.myglide.load.resource.transcode.TranscodeRegistry;
import com.lizy.myglide.provider.EncoderRegistry;
import com.lizy.myglide.util.pool.FactoryPools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-4-27.
 */
public class Registry {

    private final ModelLoaderRegistry modelLoaderRegistry;

    private final ResourceDecodeRegistry decoderRegistry;
    private final TranscodeRegistry transcoderRegistry;
    private final EncoderRegistry encoderRegistry;

//    private final LoadPathCache loadPathCache = new LoadPathCache();

    private final Pools.Pool<List<Exception>> exceptionListPool = FactoryPools.threadSafeList();
    private LoadPathCache loadPathCache = new LoadPathCache();

    public Registry(Context context) {
        this.modelLoaderRegistry =
                new ModelLoaderRegistry(context.getApplicationContext(), exceptionListPool);
        decoderRegistry = new ResourceDecodeRegistry();
        transcoderRegistry = new TranscodeRegistry();
        encoderRegistry = new EncoderRegistry();
    }

    public <Data> Registry register(Class<Data> dataClass, Encoder<Data> encoder) {
        encoderRegistry.add(dataClass, encoder);
        return this;
    }

    public <X> Encoder<X> getSourceEncode(X data) throws NoSourceEncodeAvailableException {
        Encoder<X> encoder = encoderRegistry.getEncoder((Class<X>)data.getClass());
        if (encoder != null) {
            return encoder;
        }
        throw new NoSourceEncodeAvailableException(data.getClass());
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

    public <Data, TResource> Registry append(Class<Data> dataClass, Class<TResource> resourceClass,
                                             ResourceDecoder<Data, TResource> decoder) {
        decoderRegistry.append(decoder, dataClass, resourceClass);
        return this;
    }

    public <Data, TResource> Registry prepend(Class<Data> dataClass, Class<TResource> resourceClass,
                                              ResourceDecoder<Data, TResource> decoder) {
        decoderRegistry.prepend(decoder, dataClass, resourceClass);
        return this;
    }

    public <Model> List<ModelLoader<Model, ?>> getModelLoaders(Model model) {
        List<ModelLoader<Model, ?>> result = modelLoaderRegistry.getModelLoaders(model);
        if (result.isEmpty()) {
            throw new NoModelLoaderAvailableException(model);
        }
        return result;
    }

    public <TResource, Transcode> Registry register(Class<TResource> resourceClass,
                        Class<Transcode> transcodeClass, ResourceTranscoder<TResource, Transcode> transcoder) {
        transcoderRegistry.register(resourceClass, transcodeClass, transcoder);
        return this;
    }

    public <Data, TResource, Transcode> LoadPath<Data, TResource, Transcode> getLoadPath(
            Class<Data> dataClass, Class<TResource> resourceClass, Class<Transcode> transcodeClass) {
        LoadPath<Data, TResource, Transcode> result =
                loadPathCache.get(dataClass, resourceClass, transcodeClass);
        if (result == null && !loadPathCache.contains(dataClass, resourceClass, transcodeClass)) {
            List<DecodePath<Data, TResource, Transcode>> decodePaths =
                    getDecodePaths(dataClass, resourceClass, transcodeClass);
            // It's possible there is no way to decode or transcode to the desired types from a given
            // data class.
            if (decodePaths.isEmpty()) {
                result = null;
            } else {
                result = new LoadPath<>(dataClass, resourceClass, transcodeClass, decodePaths,
                        exceptionListPool);
            }
            loadPathCache.put(dataClass, resourceClass, transcodeClass, result);
        }
        return result;
    }

    private <Data, TResource, Transcode> List<DecodePath<Data, TResource, Transcode>> getDecodePaths(
            Class<Data> dataClass, Class<TResource> resourceClass, Class<Transcode> transcodeClass) {

        List<DecodePath<Data, TResource, Transcode>> decodePaths = new ArrayList<>();
        List<Class<TResource>> registeredResourceClasses =
                decoderRegistry.getResourceClasses(dataClass, resourceClass);

        for (Class<TResource> registeredResourceClass : registeredResourceClasses) {
            List<Class<Transcode>> registeredTranscodeClasses =
                    transcoderRegistry.getTranscodeClasses(registeredResourceClass, transcodeClass);

            for (Class<Transcode> registeredTranscodeClass : registeredTranscodeClasses) {

                List<ResourceDecoder<Data, TResource>> decoders =
                        decoderRegistry.getDecoders(dataClass, registeredResourceClass);
                ResourceTranscoder<TResource, Transcode> transcoder =
                        transcoderRegistry.get(registeredResourceClass, registeredTranscodeClass);
                decodePaths.add(new DecodePath<>(dataClass, registeredResourceClass,
                        registeredTranscodeClass, decoders, transcoder, exceptionListPool));
            }
        }
        return decodePaths;
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

    public static class NoSourceEncodeAvailableException extends MissingComponentException {
        public NoSourceEncodeAvailableException(Class<?> dataClass) {
            super("Failed to find source encode for data class: " + dataClass);
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
