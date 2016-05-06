package com.lizy.myglide.load.engine.cache;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.Key;

import java.io.File;

/**
 * Created by lizy on 16-5-5.
 */
public class DiskCacheAdapter implements DiskCache {
    @Nullable
    @Override
    public File get(Key k) {
        return null;
    }

    @Override
    public void put(Key k, Writer writer) {

    }

    @Override
    public void delete(Key k) {

    }

    @Override
    public void clear() {

    }
}
