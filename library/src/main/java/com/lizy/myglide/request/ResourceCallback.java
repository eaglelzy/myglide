package com.lizy.myglide.request;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.engine.GlideException;
import com.lizy.myglide.load.engine.Resource;

/**
 * Created by lizy on 16-5-4.
 */
public interface ResourceCallback {
    void onResourceReady(Resource<?> resource, DataSource dataSource);

    void onLoadFailed(GlideException e);
}
