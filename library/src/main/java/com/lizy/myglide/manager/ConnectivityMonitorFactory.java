package com.lizy.myglide.manager;

import android.content.Context;
import android.support.annotation.Nullable;

import com.lizy.myglide.manager.ConnectivityMonitor.ConnectivityListener;

/**
 * Created by lizy on 16-5-3.
 */
public interface ConnectivityMonitorFactory {
    @Nullable
    ConnectivityMonitor build(@Nullable Context context,
                              @Nullable ConnectivityListener connectivityListener);
}
