package com.lizy.myglide.manager;

/**
 * Created by lizy on 16-5-1.
 */
public interface Lifecycle {

    void addListener(LifecycleListener listener);

    void removeListener(LifecycleListener listener);
}
