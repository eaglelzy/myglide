package com.lizy.myglide.load.engine.cache;

import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by lizy on 16-4-19.
 */
public class DiskLruCacheFactory implements DiskCache.Factory {

    private final int diskCacheSize;
    private final CacheDirectoryGetter cacheDirectoryGetter;

    public interface CacheDirectoryGetter {
        File getCacheDirectory();
    }

    public DiskLruCacheFactory(final String diskCacheDirectory, int diskCacheSize) {
        this.cacheDirectoryGetter = new CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                return new File(diskCacheDirectory);
            }
        };
        this.diskCacheSize = diskCacheSize;
    }

    public DiskLruCacheFactory(final String diskCacheDirectory, final String diskCacheName,
                               int diskCacheSize) {
        this.cacheDirectoryGetter = new CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                return new File(diskCacheDirectory, diskCacheName);
            }
        };
        this.diskCacheSize = diskCacheSize;
    }

    public DiskLruCacheFactory(CacheDirectoryGetter cacheDirectoryGetter, int diskCacheSize) {
        this.diskCacheSize = diskCacheSize;
        this.cacheDirectoryGetter = cacheDirectoryGetter;
    }

    @Nullable
    @Override
    public DiskCache build() {
        File cacheDir = cacheDirectoryGetter.getCacheDirectory();
        if (cacheDir == null) {
            return null;
        }
        if (!cacheDir.mkdirs() && (!cacheDir.exists() || !cacheDir.isDirectory())) {
            return null;
        }

        return DiskLruCacheWrapper.get(cacheDir, diskCacheSize);
    }
}
