package com.lizy.myglide.load.engine.cache;

import android.support.annotation.Nullable;
import android.util.Log;

import com.lizy.myglide.disklrucache.DiskLruCache;
import com.lizy.myglide.load.Key;

import java.io.File;
import java.io.IOException;

/**
 * Created by lizy on 16-4-19.
 */
public class DiskLruCacheWrapper implements DiskCache {

    private static final String TAG = "DiskLruCacheWrapper";

    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    private DiskLruCache diskLruCache;
    private static DiskLruCacheWrapper wrapper;

    private DiskLruCacheWriteLocker writeLocker = new DiskLruCacheWriteLocker();

    private final File directory;
    private final int maxSize;
    private final SafeKeyGenerator safeKeyGenerator;

    public DiskLruCacheWrapper(File directory, int maxSize) {
        this.directory = directory;
        this.maxSize = maxSize;
        this.safeKeyGenerator = new SafeKeyGenerator();
    }

    public synchronized static DiskLruCacheWrapper get(File directory, int size) {
        if (wrapper == null) {
            wrapper = new DiskLruCacheWrapper(directory, size);
        }
        return wrapper;
    }

    private synchronized DiskLruCache getDiskCache() throws IOException {
        if (diskLruCache == null) {
            diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize);
        }
        return diskLruCache;
    }

    @Nullable
    @Override
    public File get(Key key) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Get: Obtain:" + safeKey + " for key:" + key);
        }
        File result = null;
        try {
            DiskLruCache.Value value = getDiskCache().get(safeKey);
            if (value != null) {
                result = value.getFile(0);
            }
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Get: Unable to get from disk cache:" + e);
            }
        }
        return result;
    }

    @Override
    public void put(Key key, Writer writer) {
        // 不使用synchronized,而使用重入锁，在多线程访问不同key时提高了效率
        writeLocker.acquire(key);
        try {
            String safeKey = safeKeyGenerator.getSafeKey(key);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Put: Obtained: " + safeKey + " for Key: " + key);
            }
            try {
                DiskLruCache diskCache = getDiskCache();
                DiskLruCache.Value value = diskCache.get(safeKey);
                if (value != null) {
//                    return;
                }

                DiskLruCache.Editor editor = diskCache.edit(safeKey);
                if (editor == null) {
                    throw new IllegalStateException("Had two simultaneous puts for: " + safeKey);
                }

                try {
                    File file = editor.getFile(0);
                    if (writer.writer(file)) {
                        editor.commit();
                    }
                } finally {
                    editor.abortUnlessCommitted();
                }
            } catch (IOException e) {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unable to put to disk cache", e);
                }
            }

        }finally {
            writeLocker.release(key);
        }
    }

    @Override
    public void delete(Key key) {
        String removeKey = safeKeyGenerator.getSafeKey(key);
        try {
            getDiskCache().remove(removeKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void clear() {
        try {
            getDiskCache().delete();
            resetDiskCache();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetDiskCache() {
        diskLruCache = null;
    }
}
