package com.lizy.myglide.manager;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;

import com.lizy.myglide.RequestManager;

/**
 * Created by lizy on 16-5-1.
 */
public class SupportRequestManagerFragment extends Fragment {

    private final ActivityFragmentLifecycle lifecycle;

    private RequestManager requestManager;

    public void setParentFragmentHint(Fragment parentHint) {

    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    public SupportRequestManagerFragment() {
        this(new ActivityFragmentLifecycle());
    }

    // for test only
    @SuppressLint("ValidFragment")
    public SupportRequestManagerFragment(ActivityFragmentLifecycle activityFragmentLifecycle) {
        lifecycle = activityFragmentLifecycle;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    Lifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void onStart() {
        super.onStart();
        lifecycle.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lifecycle.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycle.onDestroy();
    }
}
