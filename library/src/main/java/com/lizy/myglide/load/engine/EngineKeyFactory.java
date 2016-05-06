package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;

import java.util.Map;

/**
 * Created by lizy on 16-5-5.
 */
public class EngineKeyFactory {
    public EngineKey build(Object model, Key signature, int width, int height,
                           Map<Class<?>, Transformation<?>> transformations, Class<?> resourceClass,
                           Class<?> transcodeClass, Options options) {
        return new EngineKey(model, signature, width, height, transformations,
                resourceClass, transcodeClass, options);
    }
}
