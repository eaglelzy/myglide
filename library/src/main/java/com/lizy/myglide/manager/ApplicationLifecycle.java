package com.lizy.myglide.manager;

/**
 * Created by lizy on 16-5-1.
 */
public class ApplicationLifecycle implements Lifecycle {
    @Override
    public void addListener(LifecycleListener listener) {
        listener.onStart();
    }

    @Override
    public void removeListener(LifecycleListener listener) {

    }
}
