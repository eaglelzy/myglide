package com.lizy.myglide.request;

import android.support.v4.util.Pools;
import android.util.Log;

import com.lizy.myglide.GlideContext;
import com.lizy.myglide.Priority;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.engine.Engine;
import com.lizy.myglide.load.engine.GlideException;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.request.target.SizeReadyCallback;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.request.transtion.Transition;
import com.lizy.myglide.request.transtion.TransitionFactory;
import com.lizy.myglide.util.LogTime;
import com.lizy.myglide.util.Util;
import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

/**
 * Created by lizy on 16-5-4.
 */
public class SingleRequest<R> implements Request,
        SizeReadyCallback,
        ResourceCallback,
        FactoryPools.Poolable {

    private final static String GLIDE_TAG = "Glide";
    private final StateVerifier stateVerifier = StateVerifier.newInstance();

    private GlideContext context;
    private Object model;
    private int overrideWidth;
    private int overrideHeight;
    private RequestListener requestListener;
    private Priority priority;
    private Engine engine;
    private TransitionFactory<R> transitionFactory;
    private Target<R> target;
    private Class<R> transcodeClass;
    private BaseRequestOptions<?> requestOptions;

    private Engine.LoadStatus loadStatus;

    private Resource<R> resource;

    private int width;
    private int height;

    private long startTime;

    private Status status;
    private enum Status {
        /**
         * Created but not yet running.
         */
        PENDING,
        /**
         * In the process of fetching media.
         */
        RUNNING,
        /**
         * Waiting for a callback given to the Target to be called to determine target dimensions.
         */
        WAITING_FOR_SIZE,
        /**
         * Finished loading media successfully.
         */
        COMPLETE,
        /**
         * Failed to load media, may be restarted.
         */
        FAILED,
        /**
         * Cancelled by the user, may not be restarted.
         */
        CANCELLED,
        /**
         * Cleared by the user with a placeholder set, may not be restarted.
         */
        CLEARED,
        /**
         * Temporarily paused by the system, may be restarted.
         */
        PAUSED,
    }

    private static final Pools.Pool<SingleRequest<?>> POOL = FactoryPools.simple(150,
            new FactoryPools.Factory<SingleRequest<?>>() {
                @Override
                public SingleRequest<?> create() {
                    return new SingleRequest<Object>();
                }
            });

    private SingleRequest() {}

    public static <R> SingleRequest<R> obtain(
            GlideContext context,
            Object model,
            Class<R> transcodeClass,
            BaseRequestOptions<?> requestOptions,
            int overrideWidth,
            int overrideHeight,
            Priority priority,
            Target<R> target,
            RequestListener<R> requestListener,
//            RequestCoordinator requestCoordinator,
            Engine engine,
            TransitionFactory<? super R> animationFactory) {
        SingleRequest<R> request = (SingleRequest<R>) POOL.acquire();
        if (request == null) {
            request = new SingleRequest<>();
        }

        request.init(context,
                model,
                transcodeClass,
                requestOptions,
                overrideWidth,
                overrideHeight,
                priority,
                target,
                requestListener,
                engine,
                animationFactory);

        return request;
    }

    private void init(GlideContext context,
                      Object model,
                      Class<R> transcodeClass,
                      BaseRequestOptions<?> requestOptions,
                      int overrideWidth,
                      int overrideHeight,
                      Priority priority,
                      Target<R> target,
                      RequestListener<R> requestListener,
                      Engine engine,
                      TransitionFactory transitionFactory) {
        this.context = context;
        this.model = model;
        this.transcodeClass = transcodeClass;
        this.requestOptions = requestOptions;
        this.target = target;
        this.overrideWidth = overrideWidth;
        this.overrideHeight = overrideHeight;
        this.priority = priority;
        this.requestListener = requestListener;
        this.engine = engine;
        this.transitionFactory = transitionFactory;
        status = Status.PENDING;
    }

    @Override
    public void begin() {
        stateVerifier.throwIfRecycled();
        startTime = LogTime.getLogTime();

        if (model == null) {
            onLoadFailed(new GlideException("model is null"));
            return;
        }

        status = Status.WAITING_FOR_SIZE;
        if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
            onSizeReady(overrideWidth, overrideHeight);
        } else {
            target.getSize(this);
        }
    }

    @Override
    public void pause() {
        clear();
        status = Status.PAUSED;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isPause() {
        return Status.PAUSED == status;
    }

    @Override
    public boolean isRunning() {
        return status == Status.RUNNING || status == Status.WAITING_FOR_SIZE;
    }

    @Override
    public boolean isComplete() {
        return status == Status.COMPLETE;
    }

    @Override
    public boolean isResourceSet() {
        return isComplete();
    }

    @Override
    public boolean isCanceled() {
        return status == Status.CANCELLED || status == Status.CLEARED;
    }

    @Override
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    @Override
    public void recycle() {

    }

    @Override
    public void onSizeReady(int width, int height) {
        float sizeMultiplier = requestOptions.getSizeMultiplier();
        this.width = Math.round(sizeMultiplier * width);
        this.height = Math.round(sizeMultiplier * height);

        loadStatus = engine.load(context,
                model,
                requestOptions.getSignature(),
                this.width,
                this.height,
                transcodeClass,
                requestOptions.getResourceClass(),
                priority,
                requestOptions.getDiskCacheStrategy(),
                requestOptions.getTransformations(),
                requestOptions.isTransformationRequired(),
                requestOptions.getOptions(),
                requestOptions.isMemoryCacheable(),
                this);
    }

    @Override
    public StateVerifier getVerifier() {
        return stateVerifier;
    }

    @Override
    public void onResourceReady(Resource<?> resource, DataSource dataSource) {
        stateVerifier.throwIfRecycled();
        loadStatus = null;
        if (resource == null) {
            GlideException e = new GlideException("expected to receive a Resource<R> with " +
                    "an object of " + transcodeClass + " inside, but instead of got null");
            onLoadFailed(e);
            return;
        }

        Object received = resource.get();
        if (received != null && !transcodeClass.isAssignableFrom(received.getClass())) {
            releaseResource(resource);
            GlideException exception = new GlideException("Expected to receive an object of "
                    + transcodeClass + " but instead" + " got "
                    + (received != null ? received.getClass() : "") + "{" + received + "} inside" + " "
                    + "Resource{" + resource + "}."
                    + (received != null ? "" : " " + "To indicate failure return a null Resource "
                    + "object, rather than a Resource object containing null data."));
            onLoadFailed(exception);
            return;
        }

        onResourceReady((Resource<R>) resource, (R)received, dataSource);
    }

    private void onResourceReady(Resource<R> resource, R result, DataSource dataSource) {
        boolean isFirstResource = isFirstReadyResource();
        status = Status.COMPLETE;
        this.resource = resource;

        if (context.getLogLevel() <= Log.DEBUG) {
            Log.d(GLIDE_TAG, "Finished loading " + result.getClass().getSimpleName() + " from "
                    + dataSource + " for " + model + " with size [" + width + "x" + height + "] in "
                    + LogTime.getElapsedMillis(startTime) + " ms");
        }

        if (requestListener == null || !requestListener.onResourceReady(resource, model, target,
                dataSource, isFirstResource)) {
            Transition<? super R> animation = transitionFactory.build(dataSource, isFirstResource);
            target.onResourceReady(result, animation);
        }

        notifyLoadSuccessed();
    }

    private void releaseResource(Resource resource) {
        engine.release(resource);
        this.resource = null;
    }

    private void notifyLoadSuccessed() {

    }

    @Override
    public void onLoadFailed(GlideException e) {
        onLoadFailed(e, Log.WARN);
    }

    private void onLoadFailed(GlideException e, int maxLogLevel) {
        stateVerifier.throwIfRecycled();
        int logLevel = context.getLogLevel();
        if (logLevel <= maxLogLevel) {
            Log.w(GLIDE_TAG, "Load failed for " + model + " with size [" + width + "x" + height + "]", e);
            if (logLevel <= Log.INFO) {
                e.logRootCauses(GLIDE_TAG);
            }
        }

        if (requestListener == null || requestListener.onLoadFailed(context, model, target,
                isFirstReadyResource())) {
            setErrorPlaceHolder();
        }
    }

    private void setErrorPlaceHolder() {

    }

    private boolean isFirstReadyResource() {
        return true;
    }
}
