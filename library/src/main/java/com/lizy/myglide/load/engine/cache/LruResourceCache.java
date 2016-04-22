package com.lizy.myglide.load.engine.cache;

import android.content.ComponentCallbacks2;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.util.LruCache;

/**
 * Created by lizy on 16-4-22.
 */
public class LruResourceCache extends LruCache<Key, Resource<?>> implements MemoryCache {

    private ResourceRemovedListener removedListener;

    public LruResourceCache(int size) {
        super(size);
    }

    @Override
    public void setResourceRemovedListener(ResourceRemovedListener listener) {
        this.removedListener = listener;
    }

    @Override
    protected void onItemEvicted(Key key, Resource<?> item) {
        if (removedListener != null) {
            removedListener.onResourceRemoved(item);
        }
    }

    @Override
    public int getSize(Resource<?> item) {
        return item.getSize();
    }

    @Override
    public void trimMemory(int level) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            clearMemory();
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            trimMemory(getCurrentSize() / 2);
        }
    }
}
