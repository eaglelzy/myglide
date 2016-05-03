package com.lizy.myglide.manager;

import android.util.Log;

import com.lizy.myglide.util.Util;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by lizy on 16-5-1.
 */
public class ActivityFragmentLifecycle implements Lifecycle {

    private final Set<LifecycleListener> listeners =
            Collections.newSetFromMap(new WeakHashMap<LifecycleListener, Boolean>());

    private boolean isStart;
    private boolean isDestroy;
    @Override
    public void addListener(LifecycleListener listener) {
        Log.d("lizy", this + " addlistener:" + listener);
        listeners.add(listener);
        if (isDestroy) {
            listener.onDestroy();
        } else if (isStart) {
            listener.onStart();
        } else {
            listener.onStop();
        }
    }

    @Override
    public void removeListener(LifecycleListener listener) {
        listeners.remove(listener);
    }

    public void onStart() {
        Log.d("lizy", "listeners=" + listeners);
        isStart = true;
        for (LifecycleListener listener : Util.getSnapshot(listeners)) {
            listener.onStart();
        }
    }
    public void onStop() {
        Log.d("lizy", "listeners=" + listeners);
        isStart = false;
        for (LifecycleListener listener : Util.getSnapshot(listeners)) {
            listener.onStop();
        }
    }

    public void onDestroy() {
        Log.d("lizy", "listeners=" + listeners);
        isDestroy = true;
        for (LifecycleListener listener : Util.getSnapshot(listeners)) {
            listener.onDestroy();
        }
    }
}
