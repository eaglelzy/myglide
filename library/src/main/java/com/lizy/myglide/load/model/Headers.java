package com.lizy.myglide.load.model;

import java.util.Collections;
import java.util.Map;

/**
 * Created by lizy on 16-4-27.
 */
public interface Headers {

    Headers DEFAULT = new Headers() {
        @Override
        public Map<String, String> getHeaders() {
            return Collections.emptyMap();
        }
    };

    Map<String, String> getHeaders();
}
