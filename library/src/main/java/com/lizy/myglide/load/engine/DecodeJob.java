package com.lizy.myglide.load.engine;

import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.util.Log;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.Priority;
import com.lizy.myglide.Registry;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.EncodeStrategy;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.ResourceEncoder;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.load.engine.cache.DiskCache;
import com.lizy.myglide.util.LogTime;
import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lizy on 16-5-4.
 */
public class DecodeJob<R> implements Runnable,
        DataFetcherGenerator.FetcherReadyCallback,
        Comparable<DecodeJob<?>>,
        FactoryPools.Poolable {
    private final static String TAG = "DecodeJob";
    private final DecodeHelper<R> decodeHelper = new DecodeHelper<>();
    private final List<Exception> exceptions = new ArrayList<>();
    private final StateVerifier stateVerifier = StateVerifier.newInstance();
    private final DiskCacheProvider diskCacheProvider;
    private final Pools.Pool<DecodeJob<?>> pool;
    private final ReleaseManager releaseManager = new ReleaseManager();

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
    private DataFetcherGenerator currentGenerator;

    private volatile boolean isCallbackNotified;
    private volatile boolean isCancelled;

    private DefferedEncodeManager<?> defferedEncodeManager = new DefferedEncodeManager<>();

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
        try {
            if (isCancelled) {
                notifyFailed();
                return;
            }
            runWrapped();
        }catch (RuntimeException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "DecodeJob threw unexpectedly"
                        + ", isCancelled: " + isCancelled
                        + ", stage: " + stage, e);
            }
            // When we're encoding we've already notified our callback and it isn't safe to do so again.
            if (stage != Stage.ENCODE) {
                notifyFailed();
            }
            if (!isCancelled) {
                throw e;
            }
        }
    }

    private void runWrapped() {
        switch (runReason) {
            case INITIALIZE:
                stage = getNextStage(Stage.INITIALIZE);
                currentGenerator = getNextGenerator();
                runGenerators();
                break;
            case SWITCH_TO_SOURCE_SERVICE:
                runGenerators();
                break;
            case DECODE_DATA:
                decodeFromRetrievedData();
                break;
            default:
                throw new IllegalStateException("Unrecognized run resean: " + runReason);
        }
    }

    private void decodeFromRetrievedData() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            logWithTimeAndKey("Retrieved data", startFetchTime,
                    "data: " + currentData
                            + ", cache key: " + currentSourceKey
                            + ", datasource: " + currentDataSource
                            + ", fetcher: " + currentFetcher);
        }

        Resource<R> resource = null;
        try {
            resource = decodeFromData(currentFetcher, currentData, currentDataSource);
        } catch (GlideException e) {
            e.setLoggingDetails(currentAttemptingKey, currentDataSource);
            exceptions.add(e);
        }

        if (resource != null) {
            notifyEncodeAndRelease(resource, currentDataSource);
        } else {
            //TODO:why call this
//            runGenerators();
        }
    }

    private void notifyEncodeAndRelease(Resource<R> resource, DataSource dataSource) {
        Resource<R> result = resource;
        LockedResource<R> lockedResource = null;
        if (defferedEncodeManager.hasResourceEncoder()) {
            lockedResource = LockedResource.obtain(resource);
            result = lockedResource;
        }
        notifyComplete(result, dataSource);

        stage = Stage.ENCODE;
        try {
            if (defferedEncodeManager.hasResourceEncoder()) {
                defferedEncodeManager.encode(diskCacheProvider, options);
            }
        } finally {
            if (lockedResource != null) {
                lockedResource.unlock();
            }
        }
        onEncodeComplete();
    }

    private void notifyComplete(Resource<R> resource, DataSource currentDataSource) {
        setNotifiedOrThrow();
        callback.onResourceReady(resource, currentDataSource);
    }

    private void setNotifiedOrThrow() {
        stateVerifier.throwIfRecycled();
        if (isCallbackNotified) {
            throw new IllegalStateException("Already notified");
        }
        isCallbackNotified = true;
    }

    private Resource<R> decodeFromData(DataFetcher<?> fetcher,
                           Object data,
                           DataSource dataSource) throws GlideException {
        try {
            if (data == null) {
                return null;
            }
            long startTime = LogTime.getLogTime();
            Resource<R> result = decodeFromFetcher(data, dataSource);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Decoded result " + result, startTime);
            }
            return result;
        } finally {
            fetcher.cleanup();
        }
    }

    private <Data> Resource<R> decodeFromFetcher(Data data, DataSource dataSource)
            throws GlideException {
        LoadPath<Data, ?, R> path = decodeHelper.getLoadPath((Class<Data>)data.getClass());
        return runLoadPath(data, dataSource, path);
    }

    private <Data, ResourceType> Resource<R> runLoadPath(Data data,
                           DataSource dataSource,
                           LoadPath<Data, ResourceType, R> path) throws GlideException {
        try {
            return path.load(data, options, width, height,
                    new DecodeCallback<ResourceType>(dataSource));
        } finally {

        }
    }

    public void release(boolean isRemoveFromQueue) {
        if (releaseManager.release(isRemoveFromQueue)) {
            releaseInternal();
        }
    }

    private void onEncodeComplete() {
        if (releaseManager.onEncodeComplete()) {
            releaseInternal();
        }
    }

    private void releaseInternal() {
        releaseManager.reset();
//    deferredEncodeManager.clear();
        decodeHelper.clear();
        isCallbackNotified = false;
        glideContext = null;
        signature = null;
        options = null;
        priority = null;
        loadKey = null;
        callback = null;
        stage = null;
        currentGenerator = null;
        currentThread = null;
        currentSourceKey = null;
        currentData = null;
        currentDataSource = null;
        currentFetcher = null;
        startFetchTime = 0L;
        isCancelled = false;
        exceptions.clear();
        pool.release(this);
    }

    @Override
    public int compareTo(DecodeJob<?> another) {
        return 0;
    }

    class DecodeCallback<Z> implements DecodePath.DecodeCallback<Z> {

        private final DataSource dataSource;

        public DecodeCallback(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Resource<Z> onResourceDecoded(Resource<Z> decoded) {
            Class<Z> resourceSubClass = getResourceClass(decoded);
            Transformation<Z> appliedTransformation = null;

            Resource<Z> transformed = decoded;
            if (dataSource != DataSource.RESOURCE_DISK_CACHE) {
                appliedTransformation = decodeHelper.getTransformation(resourceSubClass);
                transformed = appliedTransformation.transform(decoded, width, height);
            }

            if (!decoded.equals(transformed)) {
                decoded.recycle();
            }

            final EncodeStrategy encodeStrategy;
            final ResourceEncoder<Z> encoder;
            if (decodeHelper.isResourceEncoderAvailable(transformed)) {
                encoder = decodeHelper.getResultEncoder(transformed);
                encodeStrategy = encoder.getEncodeStrategy(options);
            } else {
                encoder = null;
                encodeStrategy = EncodeStrategy.NONE;
            }

            Resource<Z> result = transformed;
            boolean isFromAlternateCacheKey = !decodeHelper.isSourceKey(currentSourceKey);
            if (diskCacheStrategy.isResourceCacheable(isFromAlternateCacheKey, dataSource, encodeStrategy)) {
                if (encoder == null) {
                    throw new Registry.NoResultEncoderAvailableException(transformed.getResourceClass());
                }

                final Key key;
                if (encodeStrategy == EncodeStrategy.SOURCE) {
                    key = new DataCacheKey(currentSourceKey, signature);
                } else if (encodeStrategy == EncodeStrategy.TRANSFORMED) {
                    key = new ResourceCacheKey(currentSourceKey, signature, width, height,
                            appliedTransformation, resourceSubClass, options);
                } else {
                    throw new IllegalArgumentException("Unkown EncodeStrategy:" + encodeStrategy);
                }
                LockedResource<Z> lockedResource = LockedResource.obtain(transformed);
                defferedEncodeManager.init(key, encoder, lockedResource);
                result = lockedResource;
            }

            return result;
        }

        private Class<Z> getResourceClass(Resource<Z> decoded) {
            return (Class<Z>) decoded.get().getClass();
        }
    }

    private static class DefferedEncodeManager<Z> {

        private Key key;
        private ResourceEncoder<Z> resourceEncoder;
        private LockedResource<Z> lockedResource;

        <X> void init(Key key, ResourceEncoder<X> resourceEncoder, LockedResource<X> lockedResource) {
            this.key = key;
            this.resourceEncoder = (ResourceEncoder<Z>) resourceEncoder;
            this.lockedResource = (LockedResource<Z>) lockedResource;
        }

        void encode(DiskCacheProvider diskCacheProvider, Options options) {
            try {
                diskCacheProvider.getDiskCache().put(key,
                        new DataCacheWriter<>(resourceEncoder, lockedResource, options));
            } finally {
                lockedResource.unlock();
            }
        }

        boolean hasResourceEncoder() {
            return resourceEncoder != null;
        }

        void clear() {
            key = null;
            resourceEncoder = null;
            lockedResource = null;
        }
    }

    private void runGenerators() {
        currentThread = Thread.currentThread();
        startFetchTime = LogTime.getLogTime();
        boolean isStarted = false;
        while (!isStarted && currentGenerator != null
                && !(isStarted = currentGenerator.startNext())) {
            stage = getNextStage(stage);
            currentGenerator = getNextGenerator();

            if (stage == Stage.SOURCE) {
                reschedule();
                return;
            }
        }

        if ((stage == Stage.FINISHED || isCancelled) || !isStarted) {
            notifyFailed();
        }
    }

    private void notifyFailed() {
        setNotifiedOrThrow();
        GlideException e = new GlideException("Failed to load resource", new ArrayList<>(exceptions));
        callback.onLoadFailed(e);
        onLoadFailed();
    }
    private void onLoadFailed() {
        if (releaseManager.onFailed()) {
            releaseInternal();
        }
    }

    private DataFetcherGenerator getNextGenerator() {
        switch (stage) {
            case RESOURCE_CACHE:
                return new ResourceCacheGenerator(decodeHelper, this);
            case DATA_CACHE:
                return new DataCacheGenerator(decodeHelper, this);
            case SOURCE:
                return new SourceGenerator(decodeHelper, this);
            case FINISHED:
                return null;
            default:
                throw new IllegalStateException("Unrecognized stage:" + stage);
        }
    }

    private Stage getNextStage(Stage current) {
        switch (current) {
            case INITIALIZE:
                return diskCacheStrategy.decodeCachedResource()
                        ? Stage.RESOURCE_CACHE : getNextStage(Stage.RESOURCE_CACHE);
            case RESOURCE_CACHE:
                return diskCacheStrategy.decodeCachedData()
                        ? Stage.DATA_CACHE : getNextStage(Stage.DATA_CACHE);
            case DATA_CACHE:
                return Stage.SOURCE;
            case SOURCE:
            case FINISHED:
                return Stage.FINISHED;
            default:
                throw new IllegalArgumentException("Unrecognized stage: " + current);
        }
    }

    @Override
    public StateVerifier getVerifier() {
        return stateVerifier;
    }

    boolean willDecodeFromCache() {
        return false;
    }

    @Override
    public void reschedule() {
        runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
        callback.reschedule(this);
    }

    @Override
    public void onDataFetcherReady(Key sourceKey, @Nullable Object data, DataFetcher<?> fetcher, DataSource dataSource, Key attemptedKey) {
        this.currentSourceKey = sourceKey;
        this.currentData = data;
        this.currentFetcher = fetcher;
        this.currentDataSource = dataSource;
        this.currentAttemptingKey = attemptedKey;
        if (Thread.currentThread() != currentThread) {
            runReason = RunReason.DECODE_DATA;
            callback.reschedule(this);
        } else {
            decodeFromRetrievedData();
        }
    }

    @Override
    public void onDataFetcherFailed(Key attemptedKey, Exception e, DataFetcher<?> fetcher, DataSource dataSource) {
        GlideException exception = new GlideException("Fetching data failed", e);
        exception.setLoggingDetails(attemptedKey, dataSource, fetcher.getDataClass());
        exceptions.add(exception);
        if (Thread.currentThread() != currentThread) {
            runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
            callback.reschedule(this);
        } else {
            runGenerators();
        }
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

    private void logWithTimeAndKey(String message, long startTime) {
        logWithTimeAndKey(message, startTime, null /*extraArgs*/);
    }

    private void logWithTimeAndKey(String message, long startTime, String extraArgs) {
        Log.v(TAG, message + " in " + LogTime.getElapsedMillis(startTime) + ", load key: " + loadKey
                + (extraArgs != null ? ", " + extraArgs : "") + ", thread: "
                + Thread.currentThread().getName());
    }

    private static class ReleaseManager {
        private boolean isReleased;
        private boolean isEncodeComplete;
        private boolean isFailed;

        synchronized boolean release(boolean isRemovedFromQueue) {
            isReleased = true;
            return isComplete(isRemovedFromQueue);
        }

        synchronized boolean onEncodeComplete() {
            isEncodeComplete = true;
            return isComplete(false /*isRemovedFromQueue*/);
        }

        synchronized boolean onFailed() {
            isFailed = true;
            return isComplete(false /*isRemovedFromQueue*/);
        }

        synchronized void reset() {
            isEncodeComplete = false;
            isReleased = false;
            isFailed = false;
        }

        private boolean isComplete(boolean isRemovedFromQueue) {
            return (isFailed || isRemovedFromQueue || isEncodeComplete) && isReleased;
        }
    }

}
