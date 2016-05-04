package com.lizy.myglide.manager;

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
        isStart = true;
        for (LifecycleListener listener : Util.getSnapshot(listeners)) {
            listener.onStart();
        }
    }
    public void onStop() {
        isStart = false;
        for (LifecycleListener listener : Util.getSnapshot(listeners)) {
            listener.onStop();
        }
    }

    public void onDestroy() {
        isDestroy = true;
        for (LifecycleListener listener : Util.getSnapshot(listeners)) {
            listener.onDestroy();
        }
    }
}
