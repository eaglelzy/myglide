package com.lizy.myglide.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.lizy.myglide.RequestManager;
import com.lizy.myglide.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizy on 16-5-1.
 */
public class RequestManagerRetriever implements Handler.Callback {

    private static final String TAG = "RequestManagerRetriever";
    private static final String FRAGMENT_TAG = "com.lizy.myglide.manager";

    private static RequestManagerRetriever INSTANCE = new RequestManagerRetriever();

    private volatile RequestManager applicationRequestManager;

    final Map<android.app.FragmentManager, RequestManagerFragment> pendingRequestManagerFragments =
            new HashMap<>();

    final Map<FragmentManager, SupportRequestManagerFragment>
            pendingSupportRequestManagerFragments = new HashMap<>();

    private static final int ID_REMOVE_FRAGMENT_MANAGER = 1;
    private static final int ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2;

    private final Handler handler;

    public static RequestManagerRetriever get() {
        return INSTANCE;
    }

    public RequestManagerRetriever() {
        handler = new Handler(Looper.getMainLooper(), this);
    }

    private RequestManager getApplicationRequestManager(Context context) {
        if (applicationRequestManager == null) {
            synchronized (this) {
                if (applicationRequestManager == null) {
                    applicationRequestManager = new RequestManager(context.getApplicationContext(),
                            new ApplicationLifecycle());
                }
            }
        }
        return applicationRequestManager;
    }

    public RequestManager get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("you cannot start load on a null context");
        } else if (Util.isOnMainThread() && !(context instanceof Application)) {
            if (context instanceof FragmentActivity) {
                return get((FragmentActivity)context);
            } else if (context instanceof Activity) {
                return get((Activity)context);
            } else if (context instanceof ContextWrapper) {
                return get(((ContextWrapper) context).getBaseContext());
            }
        }

        return getApplicationRequestManager(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public RequestManager get(Activity activity) {
        if (Util.isOnBackgroudThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            android.app.FragmentManager fm = activity.getFragmentManager();
            return fragmentGet(activity, fm, null);
        }
    }

    public RequestManager get(FragmentActivity activity) {
        if (Util.isOnBackgroudThread()) {
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            FragmentManager fm = activity.getSupportFragmentManager();
            return supportFragmentGet(activity, fm , null);
        }
    }

    public RequestManager get(Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException(
                    "you cannot start a load on fragment before it is attached");
        }
        if (Util.isOnBackgroudThread()) {
            return get(fragment.getActivity().getApplicationContext());
        } else {
            FragmentManager fm = fragment.getChildFragmentManager();
            return supportFragmentGet(fragment.getActivity(), fm, fragment);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public RequestManager get(android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException(
                    "you cannot start a load on fragment before it is attached");
        }
        if (Util.isOnBackgroudThread() || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return get(fragment.getActivity().getApplicationContext());
        } else {
            android.app.FragmentManager fm = fragment.getChildFragmentManager();
            return fragmentGet(fragment.getActivity(), fm, fragment);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void assertNotDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private RequestManager fragmentGet(Context context, android.app.FragmentManager fm,
                                       android.app.Fragment parentHint) {
        RequestManagerFragment current = getRequestManagerFragment(fm, parentHint);
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(context, current.getLifecycle());
            current.setRequestManager(requestManager);
        }

        return requestManager;
    }

    private RequestManager supportFragmentGet(Context context,
                                              FragmentManager fm, Fragment parentHint) {
        SupportRequestManagerFragment fragment = getSupportRequestManagerFragment(fm, parentHint);
        Log.d("lizy", "fragment=" +fragment);
        RequestManager requestManager = fragment.getRequestManager();
        if (requestManager == null) {
            requestManager = new RequestManager(context, fragment.getLifecycle());
            fragment.setRequestManager(requestManager);
        }

        return requestManager;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private RequestManagerFragment getRequestManagerFragment(android.app.FragmentManager fm,
                                                             android.app.Fragment parentHint) {
        RequestManagerFragment current = (RequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = pendingRequestManagerFragments.get(fm);
            if (current == null) {
                current = new RequestManagerFragment();
                current.setParentFragmentHint(parentHint);
                pendingRequestManagerFragments.put(fm, current);
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
                handler.obtainMessage(ID_REMOVE_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }

        return current;
    }

    private SupportRequestManagerFragment getSupportRequestManagerFragment(FragmentManager fm,
                                   Fragment parentHint) {
        SupportRequestManagerFragment current =
                (SupportRequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = pendingSupportRequestManagerFragments.get(fm);
            if (current == null) {
                current = new SupportRequestManagerFragment();
                current.setParentFragmentHint(parentHint);
                pendingSupportRequestManagerFragments.put(fm, current);
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
                handler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }
        return current;
    }

    @Override
    public boolean handleMessage(Message message) {
        boolean handled = true;
        Object removed = null;
        Object key = null;
        switch (message.what) {
            case ID_REMOVE_FRAGMENT_MANAGER:
                android.app.FragmentManager fm = (android.app.FragmentManager) message.obj;
                key = fm;
                removed = pendingRequestManagerFragments.remove(fm);
                break;
            case ID_REMOVE_SUPPORT_FRAGMENT_MANAGER:
                FragmentManager supportFm = (FragmentManager) message.obj;
                key = supportFm;
                removed = pendingSupportRequestManagerFragments.remove(supportFm);
                break;
            default:
                handled = false;
                break;
        }
        if (handled && removed == null && Log.isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, "Failed to remove expected request manager fragment, manager: " + key);
        }
        return handled;
    }
}
