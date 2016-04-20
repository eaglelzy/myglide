package com.lizy.myglide.load.engine.cache;

import android.content.Context;

import java.io.File;

/**
 * Created by lizy on 16-4-19.
 */
public class InternalDiskCacheGetterFactory extends DiskLruCacheFactory {
    public InternalDiskCacheGetterFactory(Context context) {
        this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
    }

    public InternalDiskCacheGetterFactory(Context context, int diskCacheSize) {
        this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, diskCacheSize);
    }

    public InternalDiskCacheGetterFactory(final Context context, final String diskCacheName,
                                          int diskCacheSize) {
        super(new CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                File cacheDir = context.getCacheDir();
                if (cacheDir == null) {
                    return null;
                }

                if (diskCacheName != null) {
                    return new File(cacheDir, diskCacheName);
                }
                return cacheDir;
            }
        }, diskCacheSize);
    }
}
