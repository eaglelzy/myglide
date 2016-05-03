package com.lizy.myglide.manager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import com.lizy.myglide.manager.ConnectivityMonitor.ConnectivityListener;

/**
 * Created by lizy on 16-5-3.
 */
public class DefaultConnectivityMonitorFactory implements ConnectivityMonitorFactory {
    @Nullable
    @Override
    public ConnectivityMonitor build(@Nullable Context context,
                                     @Nullable ConnectivityListener connectivityListener) {
        final int res = context.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE");
        final boolean hasPermission = res == PackageManager.PERMISSION_GRANTED;
        if (hasPermission) {
            return new DefaultConnectivityMonitor(context, connectivityListener);
        } else {
            return new NullConnectivityMonitor();
        }
    }
}
