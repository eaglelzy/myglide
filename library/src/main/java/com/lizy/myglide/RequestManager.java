package com.lizy.myglide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;

import com.lizy.myglide.load.resource.drawable.DrawableTransitionOptions;
import com.lizy.myglide.manager.ConnectivityMonitor;
import com.lizy.myglide.manager.ConnectivityMonitorFactory;
import com.lizy.myglide.manager.Lifecycle;
import com.lizy.myglide.manager.LifecycleListener;
import com.lizy.myglide.manager.RequestTracker;
import com.lizy.myglide.manager.TargetTracker;
import com.lizy.myglide.request.BaseRequestOptions;
import com.lizy.myglide.request.Request;
import com.lizy.myglide.request.RequestOptions;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.request.target.ViewTarget;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-5-1.
 */
public class RequestManager implements LifecycleListener {

    private final Lifecycle lifecycle;
    private final GlideContext glideContext;
    private final ConnectivityMonitorFactory connectivityMonitorFactory;

    private final RequestOptions defaultRequestOptions;
    private final RequestOptions requestOptions;

    private final ConnectivityMonitor connectivityMonitor;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private RequestTracker requestTracker;
    private final TargetTracker targetTracker = new TargetTracker();


    private final Runnable addSeftToLifecycle = new Runnable() {
        @Override
        public void run() {
            lifecycle.addListener(RequestManager.this);
        }
    };

    public RequestManager(Context context, Lifecycle lifecycle) {
        this(context, lifecycle, new RequestTracker(), Glide.get(context).getConnectivityMonitorFactory());
    }

    RequestManager(Context context, Lifecycle lifecycle, RequestTracker requestTracker,
                   ConnectivityMonitorFactory connectivityMonitorFactory) {
        this.glideContext = Glide.get(context).getGlideContext();
        this.requestTracker = requestTracker;
        this.lifecycle = lifecycle;
        this.connectivityMonitorFactory = Glide.get(context).getConnectivityMonitorFactory();

        connectivityMonitor = connectivityMonitorFactory.build(context,
                new RequestManagerConnectivityMonitorListener(requestTracker));

        if (Util.isOnBackgroudThread()) {
            mainHandler.post(addSeftToLifecycle);
        } else {
            lifecycle.addListener(this);
        }
        lifecycle.addListener(connectivityMonitor);

        defaultRequestOptions = this.glideContext.getDefaultRequestOptions();
        requestOptions = defaultRequestOptions;

        //TODO:
//        Glide.get(context).registerRequestManager(this);
    }

    private void pauseRequests() {
        Util.assertMainThread();
        requestTracker.pauseRequests();
    }

    private void resumeRequests() {
        Util.assertMainThread();
        requestTracker.resumeRequests();
    }

    @Override
    public void onStart() {
        resumeRequests();
        targetTracker.onStart();
    }

    @Override
    public void onStop() {
        pauseRequests();
        targetTracker.onStop();
    }

    @Override
    public void onDestroy() {
        targetTracker.onDestroy();
        for (Target<?> target : targetTracker.getAll()) {
            clear(target);
        }
        targetTracker.clear();
        requestTracker.cleanRequests();
        lifecycle.removeListener(this);
        lifecycle.removeListener(connectivityMonitor);
        mainHandler.removeCallbacks(addSeftToLifecycle);
    }

    public void clear(View view) {
        clear(new ClearTargetView(view));
    }

    public void clear(@Nullable final Target<?> target) {
        if (target == null) {
            return;
        }
        if (Util.isOnMainThread()) {
            untrackerOrDelegate(target);
        } else {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    clear(target);
                }
            });
        }
    }

    private void untrackerOrDelegate(Target<?> target) {
        boolean isOwnedUs = untrack(target);
        if (isOwnedUs) {

        }
    }

    boolean untrack(Target<?> target) {
        Request request = target.getRequest();
        if (request == null) {
            return true;
        }

        if (requestTracker.clearRemoveAndRecycle(request)) {
            targetTracker.untrack(target);
            target.setRequest(null);
            return true;
        } else {
            return false;
        }
    }

    void track(Target<?> target, Request request) {
        targetTracker.track(target);
        requestTracker.runRequest(request);
    }

    private static class RequestManagerConnectivityMonitorListener implements
            ConnectivityMonitor.ConnectivityListener {
        private final RequestTracker requestTracker;
        public RequestManagerConnectivityMonitorListener(RequestTracker requestTracker) {
            this.requestTracker = requestTracker;
        }

        @Override
        public void onConnectivityChanged(boolean isConnected) {
            if (isConnected) {
                requestTracker.restartRequests();
            }
        }
    }

    private static class ClearTargetView extends ViewTarget<View, Object> {

        public ClearTargetView(View view) {
            super(view);
        }

        @Override
        public void setRequest(@Nullable Request request) {

        }
    }

    public RequestBuilder<Drawable> load(Object model) {
        return asDrawable().load(model);
    }
    public RequestBuilder<Drawable> asDrawable() {
        return as(Drawable.class).transition(new DrawableTransitionOptions());
    }

    public <ResourceType> RequestBuilder<ResourceType> as(Class<ResourceType> resourceTypeClass) {
        return new RequestBuilder<>(glideContext, this, resourceTypeClass);
    }

    BaseRequestOptions getDefaultRequestOptions() {
        return requestOptions;
    }
}
