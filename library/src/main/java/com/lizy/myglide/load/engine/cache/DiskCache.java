package com.lizy.myglide.load.engine.cache;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.Key;

import java.io.File;

/**
 * Created by lizy on 16-4-19.
 */
public interface DiskCache {

    interface Factory {
      int DEFAULT_DISK_CACHE_SIZE = 250 * 1024 * 1024;
      String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";

      @Nullable
      DiskCache build();
    }

    interface Writer {
        boolean writer(File file);
    }

    @Nullable
    File get(Key k);

    void put(Key k, Writer writer);

    void delete(Key k);

    void clear();
}
