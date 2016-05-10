package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.EncodeStrategy;

/**
 * Created by lizy on 16-5-4.
 */
public abstract class DiskCacheStrategy {

    public static DiskCacheStrategy ALL = new DiskCacheStrategy() {

        @Override
        public boolean isDataCacheable(DataSource dataSource) {
            return dataSource == DataSource.REMOTE;
        }

        @Override
        public boolean isResourceCacheable(boolean isFromAlternateCacheKey, DataSource dataSource, EncodeStrategy encodeStrategy) {
            return dataSource != DataSource.RESOURCE_DISK_CACHE && dataSource != DataSource.MEMORY_CACHE;
        }

        @Override
        public boolean decodeCachedResource() {
            return true;
        }

        @Override
        public boolean decodeCachedData() {
            return true;
        }
    };

    public static DiskCacheStrategy NONE = new DiskCacheStrategy() {

        @Override
        public boolean isDataCacheable(DataSource dataSource) {
            return false;
        }

        @Override
        public boolean isResourceCacheable(boolean isFromAlternateCacheKey, DataSource dataSource, EncodeStrategy encodeStrategy) {
            return false;
        }

        @Override
        public boolean decodeCachedResource() {
            return false;
        }

        @Override
        public boolean decodeCachedData() {
            return false;
        }
    };

    public static DiskCacheStrategy DATA = new DiskCacheStrategy() {

        @Override
        public boolean isDataCacheable(DataSource dataSource) {
            return dataSource != DataSource.DATA_DISK_CACHE && dataSource != DataSource.MEMORY_CACHE;
        }

        @Override
        public boolean isResourceCacheable(boolean isFromAlternateCacheKey, DataSource dataSource, EncodeStrategy encodeStrategy) {
            return false;
        }

        @Override
        public boolean decodeCachedResource() {
            return false;
        }

        @Override
        public boolean decodeCachedData() {
            return true;
        }
    };

    public abstract boolean isDataCacheable(DataSource dataSource);

    public abstract boolean isResourceCacheable(boolean isFromAlternateCacheKey,
            DataSource dataSource, EncodeStrategy encodeStrategy);

    public abstract boolean decodeCachedResource();

    public abstract boolean decodeCachedData();
}
