package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.Key;

/**
 * Created by lizy on 16-5-4.
 */
public interface Transformation<T> extends Key {

    Resource<T> transform(Resource<T> resource, int width, int height);
}
