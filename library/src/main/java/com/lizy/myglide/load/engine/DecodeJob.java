package com.lizy.myglide.load.engine;

import android.support.v4.util.Pools;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.Priority;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.load.engine.cache.DiskCache;
import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lizy on 16-5-4.
 */
public class DecodeJob<R> implements Runnable, FactoryPools.Poolable {
    private final DecodeHelper<R> decodeHelper = new DecodeHelper<>();
    private final List<Exception> exceptions = new ArrayList<>();
    private final StateVerifier stateVerifier = StateVerifier.newInstance();
    private final DiskCacheProvider diskCacheProvider;
    private final Pools.Pool<DecodeJob<?>> pool;

    private GlideContext glideContext;
    private Key signature;
    private Priority priority;
    private EngineKey loadKey;
    private int width;
    private int height;
    private DiskCacheStrategy diskCacheStrategy;
    private Options options;
    private Callback<R> callback;
    private int order;
    private Stage stage;
    private RunReason runReason;
    private long startFetchTime;

    private Thread currentThread;
    private Key currentSourceKey;
    private Key currentAttemptingKey;
    private Object currentData;
    private DataSource currentDataSource;
    private DataFetcher<?> currentFetcher;

    private volatile boolean isCallbackNotified;
    private volatile boolean isCancelled;

    DecodeJob(DiskCacheProvider diskCacheProvider, Pools.Pool<DecodeJob<?>> pool) {
        this.diskCacheProvider = diskCacheProvider;
        this.pool = pool;
    }

    DecodeJob<R> init(
            GlideContext glideContext,
            Object model,
            EngineKey loadKey,
            Key signature,
            int width,
            int height,
            Class<?> resourceClass,
            Class<R> transcodeClass,
            Priority priority,
            DiskCacheStrategy diskCacheStrategy,
            Map<Class<?>, Transformation<?>> transformations,
            boolean isTransformationRequired,
            Options options,
            Callback<R> callback,
            int order) {
        decodeHelper.init(
                glideContext,
                model,
                signature,
                width,
                height,
                diskCacheStrategy,
                resourceClass,
                transcodeClass,
                priority,
                options,
                transformations,
                isTransformationRequired,
                diskCacheProvider);
        this.glideContext = glideContext;
        this.signature = signature;
        this.priority = priority;
        this.loadKey = loadKey;
        this.width = width;
        this.height = height;
        this.diskCacheStrategy = diskCacheStrategy;
        this.options = options;
        this.callback = callback;
        this.order = order;
        this.runReason = RunReason.INITIALIZE;
        return this;
    }

    @Override
    public void run() {

    }

    @Override
    public StateVerifier getVerifier() {
        return null;
    }

    interface DiskCacheProvider {
        DiskCache getDiskCache();
    }

    interface Callback<R> {

        void onResourceReady(Resource<R> resource, DataSource dataSource);

        void onLoadFailed(GlideException e);

        void reschedule(DecodeJob<?> job);
    }
    /**
     * Why we're being executed again.
     */
    private enum RunReason {
        /** The first time we've been submitted. */
        INITIALIZE,
        /**
         * We want to switch from the disk cache service to the source executor.
         */
        SWITCH_TO_SOURCE_SERVICE,
        /**
         * We retrieved some data on a thread we don't own and want to switch back to our thread to
         * process the data.
         */
        DECODE_DATA,
    }

    /**
     * Where we're trying to decode data from.
     */
    private enum Stage {
        /** The initial stage. */
        INITIALIZE,
        /** Decode from a cached resource. */
        RESOURCE_CACHE,
        /** Decode from cached source data. */
        DATA_CACHE,
        /** Decode from retrieved source. */
        SOURCE,
        /** Encoding transformed resources after a successful load. */
        ENCODE,
        /** No more viable stages. */
        FINISHED,
    }
}
