package com.lizy.myglide.manager;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;

import com.lizy.myglide.RequestManager;

/**
 * Created by lizy on 16-5-1.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RequestManagerFragment extends Fragment {
    private final ActivityFragmentLifecycle lifecycle;

    public RequestManagerFragment() {
        lifecycle = new ActivityFragmentLifecycle();
    }

    Lifecycle getLifecycle() {
        return lifecycle;
    }

    public RequestManager getRequestManager() {
        return null;
    }

    public void setParentFragmentHint(Fragment parentHint) {

    }

    public void setRequestManager(RequestManager requestManager) {

    }
}
