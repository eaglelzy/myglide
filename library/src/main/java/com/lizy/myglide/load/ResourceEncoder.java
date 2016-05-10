package com.lizy.myglide.load;

import com.lizy.myglide.load.engine.Resource;

/**
 * Created by lizy on 16-5-10.
 */
public interface ResourceEncoder<T> extends Encoder<Resource<T>> {
    EncodeStrategy getEncodeStrategy(Options option);
}
