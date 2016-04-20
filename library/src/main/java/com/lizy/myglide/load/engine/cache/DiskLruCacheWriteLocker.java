package com.lizy.myglide.load.engine.cache;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.util.Preconditions;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lizy on 16-4-19.
 */
final class DiskLruCacheWriteLocker {
    private final Map<Key, WriteLock> locks = new HashMap<>();
    private final WriteLockPool writeLockPool = new WriteLockPool();

    private static class WriteLock {
        final Lock lock = new ReentrantLock();
        int interestedThreads;
    }

    void acquire(Key key) {
        WriteLock writeLock;
        synchronized (this) {
            writeLock = locks.get(key);
            if (writeLock == null) {
                writeLock = writeLockPool.obtain();
                locks.put(key, writeLock);
            }
            writeLock.interestedThreads++;
        }
        writeLock.lock.lock();
    }

    void release(Key key) {
        WriteLock writeLock;
        synchronized (this) {
            writeLock = Preconditions.checkNotNull(locks.get(key));
            if (writeLock.interestedThreads < 1) {
                throw new IllegalStateException("Cannot release a lock that is not held"
                        + ", key: " + key
                        + ", interestedThreads: " + writeLock.interestedThreads);
            }

            writeLock.interestedThreads--;
            if (writeLock.interestedThreads == 0) {
                WriteLock removed = locks.remove(key);
                if (removed != writeLock) {
                    throw new IllegalStateException("Removed the wrong lock"
                            + ", expected to remove: " + writeLock
                            + ", but actually removed: " + removed
                            + ", key: " + key);
                }
                writeLockPool.offer(removed);
            }
        }
        writeLock.lock.unlock();
    }

    private static class WriteLockPool {
        private static final int MAX_POOL_SIZE = 10;
        private Queue<WriteLock> pool = new ArrayDeque<>();

        WriteLock obtain() {
            WriteLock result;
            synchronized (pool) {
                result = pool.poll();
            }
            if (result == null) {
                result = new WriteLock();
            }
            return result;
        }

        void offer(WriteLock writeLock) {
            synchronized (pool) {
                if (pool.size() < MAX_POOL_SIZE) {
                    pool.offer(writeLock);
                }
            }
        }
    }
}
