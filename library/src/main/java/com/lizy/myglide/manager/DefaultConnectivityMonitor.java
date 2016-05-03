package com.lizy.myglide.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by lizy on 16-5-3.
 */
public class DefaultConnectivityMonitor implements ConnectivityMonitor {

    private final Context context;
    private final ConnectivityListener connectivityListener;

    private boolean isConnected;
    private boolean isRegister;

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean wasConnected = isConnected;
            isConnected = isConnected(context);
            if (wasConnected != isConnected) {
                connectivityListener.onConnectivityChanged(isConnected);
            }
        }
    };

    public DefaultConnectivityMonitor(Context context, ConnectivityListener connectivityListener) {
        this.context = context;
        this.connectivityListener = connectivityListener;
    }

    private void register() {
        if (isRegister) {
            return;
        }

        isConnected = isConnected(context);
        context.registerReceiver(connectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        isRegister = true;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void unregister() {
        if (!isRegister) {
            return;
        }
        context.unregisterReceiver(connectivityReceiver);
        isRegister = false;
    }

    @Override
    public void onStart() {
        register();
    }

    @Override
    public void onStop() {
        unregister();
    }

    @Override
    public void onDestroy() {

    }
}
