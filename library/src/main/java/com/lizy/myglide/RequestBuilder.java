package com.lizy.myglide;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.lizy.myglide.load.resource.drawable.DrawableTransitionOptions;
import com.lizy.myglide.request.BaseRequestOptions;
import com.lizy.myglide.request.Request;
import com.lizy.myglide.request.RequestListener;
import com.lizy.myglide.request.SingleRequest;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.util.Preconditions;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-5-3.
 */
public class RequestBuilder<TranscodeType> implements Cloneable {

    private static final TransitionOptions<?, ?> DEFAULT_ANIMATION_OPTIONS =
            new DrawableTransitionOptions();

    private final RequestManager requestmanager;
    private final GlideContext context;
    private final Class<TranscodeType> transcodeClass;

    private TransitionOptions<?, ? super TranscodeType> transitionOptions;

    private boolean isModelSet;

    private Object model;

    private RequestListener<TranscodeType> requestListener;

    private BaseRequestOptions<?> defaultRequestOptions;
    @NonNull private BaseRequestOptions<?> requestOptions;

    public RequestBuilder(GlideContext glideContext,
                          RequestManager requestManager,
                          Class<TranscodeType> resourceTypeClass) {
        this.context = glideContext;
        this.requestmanager = requestManager;
        this.transcodeClass = resourceTypeClass;

        defaultRequestOptions = requestManager.getDefaultRequestOptions();
        requestOptions = defaultRequestOptions;
    }

    public RequestBuilder<TranscodeType> transition(
      @NonNull TransitionOptions<?, ? super TranscodeType> transitionOptions) {
    this.transitionOptions = Preconditions.checkNotNull(transitionOptions);
    return this;
  }

    public RequestBuilder<TranscodeType> load(Object model) {
        return loadGeneric(model);
    }

    public RequestBuilder<TranscodeType> load(String model) {
        return loadGeneric(model);
    }

    private RequestBuilder<TranscodeType> loadGeneric(Object model) {
        this.model = model;
        isModelSet = true;
        return this;
    }

    public <Y extends Target<TranscodeType>> Y into(@NonNull Y target) {
        Util.assertMainThread();
        Preconditions.checkNotNull(target);
        if (!isModelSet) {
            throw new IllegalArgumentException("You must call #load() before calling #into()");
        }

        Request previous = target.getRequest();
        if (previous != null) {
            requestmanager.clear(target);
        }

        requestOptions.lock();
        Request request = buildRequest(target);
        target.setRequest(request);
        requestmanager.track(target, request);

        return target;
    }

    public RequestBuilder<TranscodeType> apply(@NonNull BaseRequestOptions<?> requestOptions) {
        Preconditions.checkNotNull(requestOptions);
        BaseRequestOptions<?> toMutate = defaultRequestOptions == this.requestOptions
                ? this.requestOptions.clone() : this.requestOptions;
        this.requestOptions = toMutate.apply(requestOptions);
        return this;
    }

    public Target<TranscodeType> into(ImageView view) {
        Util.assertMainThread();
        Preconditions.checkNotNull(view);
        if (!requestOptions.isTransformationSet()
                && requestOptions.isTransformationAllowed()
                && view.getScaleType() != null) {
            if (requestOptions.isLocked()) {
                requestOptions = requestOptions.clone();
            }
            switch (view.getScaleType()) {
                case CENTER_CROP:
//TODO:                    requestOptions.optionalCenterCrop(context);
                    break;
                case CENTER_INSIDE:
//TODO:                    requestOptions.optionalCenterInside(context);
                    break;
                case FIT_CENTER:
                case FIT_START:
                case FIT_END:
//TODO:                    requestOptions.optionalFitCenter(context);
                    break;
                //$CASES-OMITTED$
                default:
                    // Do nothing.
            }
        }

        return into(context.buildImageViewTarget(view, transcodeClass));
    }

    private Request buildRequest(Target<TranscodeType> target) {
        return SingleRequest.obtain(
                context,
                model,
                transcodeClass,
                requestOptions,
                0,
                0,
                requestOptions.getPriority(),
                target,
                requestListener,
                context.getEngine(),
                transitionOptions.getTransitionFactory());
    }
}

