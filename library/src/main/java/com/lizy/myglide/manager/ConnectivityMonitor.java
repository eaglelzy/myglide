package com.lizy.myglide.manager;

/**
 * Created by lizy on 16-5-3.
 */
public interface ConnectivityMonitor extends LifecycleListener {
    interface ConnectivityListener {
        void onConnectivityChanged(boolean isConnected);
    }
}
