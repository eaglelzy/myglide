package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.Encoder;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.cache.DiskCache;

import java.io.File;

/**
 * Created by lizy on 16-5-6.
 */
public class DataCacheWriter<DataType> implements DiskCache.Writer {
    private final Encoder<DataType> encoder;
    private final Options options;
    private final DataType data;

    public DataCacheWriter(Encoder<DataType> encoder, DataType data, Options options) {
        this.encoder = encoder;
        this.data = data;
        this.options = options;
    }

    @Override
    public boolean writer(File file) {
        return encoder.encode(data, file, options);
    }
}
