package com.lizy.myglide;

import android.content.Context;
import android.util.Log;

import com.lizy.myglide.manager.Lifecycle;
import com.lizy.myglide.manager.LifecycleListener;
import com.lizy.myglide.manager.RequestTracker;
import com.lizy.myglide.util.Util;

/**
 * Created by lizy on 16-5-1.
 */
public class RequestManager implements LifecycleListener {

    private final Lifecycle lifecycle;

    private RequestTracker requestTracker;

    public RequestManager(Context context, Lifecycle lifecycle) {
        this(context, lifecycle, new RequestTracker());
    }

    RequestManager(Context context, Lifecycle lifecycle, RequestTracker requestTracker) {
        this.requestTracker = requestTracker;
        this.lifecycle = lifecycle;

        lifecycle.addListener(this);
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
    }

    @Override
    public void onStop() {
        pauseRequests();
    }

    @Override
    public void onDestroy() {
        requestTracker.cleanRequests();
        lifecycle.removeListener(this);
    }
}
