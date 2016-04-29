package com.lizy.myglide.load.resource.transcode;

import com.lizy.myglide.load.engine.Resource;

/**
 * Created by lizy on 16-4-28.
 */
public interface ResourceTranscoder<Z, R> {

    Resource<R> transcode(Resource<Z> toTranscode);

}
