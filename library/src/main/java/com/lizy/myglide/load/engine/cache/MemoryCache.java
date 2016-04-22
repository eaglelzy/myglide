package com.lizy.myglide.load.engine.cache;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.engine.Resource;

/**
 * Created by lizy on 16-4-22.
 */
public interface MemoryCache {

    interface ResourceRemovedListener {
        void onResourceRemoved(Resource<?> removed);
    }

    int getCurrentSize();

    int getMaxSize();

    void setSizeMultiplier(float multiplier);

    @Nullable
    Resource<?> remove(Key key);

    @Nullable
    Resource<?> put(Key key, Resource<?> resource);

    void setResourceRemovedListener(ResourceRemovedListener listener);

    void clearMemory();

    void trimMemory(int level);
}
