package com.lizy.myglide.load.engine;

import android.support.v4.util.Pools;
import android.util.Log;

import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.ResourceDecoder;
import com.lizy.myglide.load.resource.transcode.ResourceTranscoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-4-28.
 */
public class DecodePath<DataType, ResourceType, Transcode> {
    private static final String TAG = "DecodePath";
    private final Class<DataType> dataClass;
    private final List<? extends ResourceDecoder<DataType, ResourceType>> decoders;
    private final ResourceTranscoder<ResourceType, Transcode> transcoder;
    private final Pools.Pool<List<Exception>> listPool;
    private final String failureMessage;

    public DecodePath(Class<DataType> dataClass,
                      Class<ResourceType> resourceClass,
                      Class<Transcode> transcodeClass,
                      List<? extends ResourceDecoder<DataType, ResourceType>> decoders,
                      ResourceTranscoder<ResourceType, Transcode> transcoder,
                      Pools.Pool<List<Exception>> listPool) {
        this.dataClass = dataClass;
        this.decoders = decoders;
        this.transcoder = transcoder;
        this.listPool = listPool;
        failureMessage = "Failed DecodePath{" + dataClass.getSimpleName() + "->"
                + resourceClass.getSimpleName() + "->" + transcodeClass.getSimpleName() + "}";
    }


    public Resource<Transcode> decode(DataType dataType, int width, int height,
                                      Options options, DecodeCallback<ResourceType> callback) throws GlideException {
        Resource<ResourceType> decoded = decodeResource(dataType, width, height, options);
        Resource<ResourceType> transformed = callback.onResourceDecoded(decoded);
        return transcoder.transcode(transformed);
    }

    private Resource<ResourceType> decodeResource(DataType data, int width,
                                  int height, Options options) throws GlideException {
        List<Exception> exceptions = listPool.acquire();
        try {
            return decodeResourceWithList(data, width, height, options, exceptions);
        } finally {
            listPool.release(exceptions);
        }
    }

    private Resource<ResourceType> decodeResourceWithList(DataType data, int width,
                                      int height, Options options, List<Exception> exceptions) throws GlideException {
        Resource<ResourceType> result = null;
        for (int i = 0, size = decoders.size(); i < size; i++) {
            ResourceDecoder<DataType, ResourceType> decoder = decoders.get(i);
            try {
                if (decoder.handles(data, options)) {
                    result = decoder.decode(data, width, height, options);
                }
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Failed to decode data for " + decoder, e);
                }
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

    @Override
    public String toString() {
        return "DecodePath{" + " dataClass=" + dataClass + ", decoders=" + decoders + ", transcoder="
                + transcoder + '}';
    }

    public interface DecodeCallback<ResourceType> {
        Resource<ResourceType> onResourceDecoded(Resource<ResourceType> resource);
    }
}
