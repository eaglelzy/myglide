package com.lizy.myglide.load.engine.executor;

import android.os.Process;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by lizy on 16-4-22.
 */
public class GlideExecutor extends ThreadPoolExecutor {

    private static final String TAG = "GlideExecutor";

    public static final String DEFAULT_DISKCACHE_EXECUTOR_NAME = "disk-cache";
    public static final String DEFAULT_SOURCE_EXECUTOR_NAME = "source";

    public static final int DEFAULT_DISKCACHE_EXCUTOR_THREADS = 1;

    private static final String CPU_NAME_REGEX = "cpu[0-9]+";
    private static final String CPU_LOCATION = "/sys/devices/system/cpu/";
    private static final int MAX_AUTOMATIC_THREAD_COUNT = 4;

    private final boolean executeSynchronously;

    public static GlideExecutor newDiskCacheExecutor() {
        return newDiskCacheExecutor(calculateBestThreadCount(), DEFAULT_DISKCACHE_EXECUTOR_NAME,
                UncaughtThrowableStrategy.DEFAULT);
    }

    public static GlideExecutor newDiskCacheExecutor(int threadCount, String name,
                                                     UncaughtThrowableStrategy strategy) {
        return new GlideExecutor(DEFAULT_DISKCACHE_EXCUTOR_THREADS, name, strategy,
                true /*preventNetworkOperations*/, false);
    }

    public static GlideExecutor newSourceExecutor() {
        return newSourceExecutor(calculateBestThreadCount(), DEFAULT_SOURCE_EXECUTOR_NAME,
                UncaughtThrowableStrategy.DEFAULT);
    }

    public static GlideExecutor newSourceExecutor(int threadCount, String name,
                                                  UncaughtThrowableStrategy strategy) {
        return new GlideExecutor(threadCount, name, strategy,
                false /*preventNetworkOperations*/, false);
    }

    public GlideExecutor(int coreThreadCount, String name, UncaughtThrowableStrategy strategy,
                     boolean preventNetworkOptions, boolean executeSynchronously) {
        super(coreThreadCount,
                coreThreadCount,
                0,
                TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<Runnable>(),
                new DefaultThreadFactory(name, strategy, preventNetworkOptions));
        this.executeSynchronously = executeSynchronously;
    }

    public static int calculateBestThreadCount() {
        File[] cpus = null;
        try {
            final File cpuInfo = new File(CPU_LOCATION);
            final Pattern cpuNamePattern = Pattern.compile(CPU_NAME_REGEX);
            cpus = cpuInfo.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return cpuNamePattern.matcher(filename).matches();
                }
            });
        } catch (Throwable t) {
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.e(TAG, "Failed to calculate accurate cpu count", t);
            }
        }

        int count = cpus != null ? cpus.length : 0;
        int availableProcessors = Math.max(1, Runtime.getRuntime().availableProcessors());
        return Math.min(MAX_AUTOMATIC_THREAD_COUNT, Math.max(count, availableProcessors));
    }

    @Override
    public void execute(Runnable command) {
        if (executeSynchronously) {
            command.run();
        } else {
            super.execute(command);
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        return maybeWait(super.submit(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return maybeWait(super.submit(task, result));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return maybeWait(super.submit(task));
    }

    private <T> Future<T> maybeWait(Future<T> future) {
        if (executeSynchronously) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return future;
    }

    public enum UncaughtThrowableStrategy {

        IGNORE,

        LOG {
            @Override
            protected void handle(Throwable throwable) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, throwable.getMessage());
                }
            }
        },

        THROWABLE {
            @Override
            protected void handle(Throwable throwable) {
                super.handle(throwable);
                if (throwable != null) {
                    throw new RuntimeException("request throw uncaught throwable!" + throwable);
                }
            }
        };

        public static final UncaughtThrowableStrategy DEFAULT = LOG;

        protected void handle(Throwable throwable) {}
    }

    private static final class DefaultThreadFactory implements ThreadFactory {

        private final String name;
        private final UncaughtThrowableStrategy strategy;

        public DefaultThreadFactory(String name, UncaughtThrowableStrategy strategy,
                                    boolean preventNetworkOperations) {
            this.name = name;
            this.strategy = strategy;
            this.preventNetworkOperations = preventNetworkOperations;
        }

        private final boolean preventNetworkOperations;
        private int threadNum;

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            final Thread result = new Thread(runnable, "glide-" + name + "-thread-" + threadNum) {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    if (preventNetworkOperations) {
                        StrictMode.setThreadPolicy(
                                new StrictMode.ThreadPolicy.Builder()
                                        .detectNetwork()
                                        .penaltyDeath()
                                        .build()
                        );
                    }
                    try {
                        super.run();
                    }catch (Throwable t) {
                        strategy.handle(t);
                    }

                }
            };
            threadNum++;
            return result;
        }
    }
}
