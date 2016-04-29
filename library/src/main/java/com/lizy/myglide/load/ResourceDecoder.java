package com.lizy.myglide.load;

import com.lizy.myglide.load.engine.Resource;

import java.io.IOException;

/**
 * Created by lizy on 16-4-28.
 */
public interface ResourceDecoder<T, Z> {

    boolean handles(T source, Options options) throws IOException;

    Resource<Z> decode(T source, int width, int height, Options options) throws IOException;
}
