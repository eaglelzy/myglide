package com.lizy.myglide.load.engine;

import android.support.v4.util.Pools;

import com.lizy.myglide.load.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lizy on 16-4-28.
 */
public class LoadPath<Data, ResourceType, Transcode>{
    private final Class<Data> dataClass;
    private final Class<Transcode> transcodeClass;

    private final String failureMessage;

    private final List<DecodePath<Data, ResourceType, Transcode>> decodePaths;
    private final Pools.Pool<List<Exception>> listPool;

    public LoadPath(Class<Data> dataClass,
                    Class<ResourceType> resourceClass,
                    Class<Transcode> transcodeClass,
                    List<DecodePath<Data, ResourceType, Transcode>> decodePaths,
                    Pools.Pool<List<Exception>> exceptionListPool) {
        this.dataClass = dataClass;
        this.transcodeClass = transcodeClass;
        this.decodePaths = decodePaths;
        this.listPool = exceptionListPool;

        failureMessage = "Failed DecodePath{" + dataClass.getSimpleName() + "->"
                + resourceClass.getSimpleName() + "->" + transcodeClass.getSimpleName() + "}";
    }
    public Resource<Transcode> load(Data data, Options options, int width,
                int height, DecodePath.DecodeCallback<ResourceType> decodeCallback) throws GlideException {
        List<Exception> exceptions = listPool.acquire();
        try {
            return loadWithExceptionList(data, options, width, height, decodeCallback, exceptions);
        } finally {
            listPool.release(exceptions);
        }
    }

    private Resource<Transcode> loadWithExceptionList(Data data, Options options,
                                                      int width, int height, DecodePath.DecodeCallback<ResourceType> decodeCallback,
                                                      List<Exception> exceptions) throws GlideException {
        int size = decodePaths.size();
        Resource<Transcode> result = null;
        for (int i = 0; i < size; i++) {
            DecodePath<Data, ResourceType, Transcode> path = decodePaths.get(i);
            try {
                result = path.decode(data, width, height, options, decodeCallback);
            } catch (GlideException e) {
                exceptions.add(e);
            }
            if (result != null) {
                break;
            }
        }

        if (result == null) {
            throw new GlideException(failureMessage, new ArrayList<>(exceptions));
        }

        return result;
    }

    public Class<Data> getDataClass() {
        return dataClass;
    }

    @Override
    public String toString() {
        return "LoadPath{" + "decodePaths="
                + Arrays.toString(decodePaths.toArray(new DecodePath[decodePaths.size()])) + '}';
    }

}
