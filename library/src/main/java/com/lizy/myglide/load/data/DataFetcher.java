package com.lizy.myglide.load.data;

import android.renderscript.RenderScript.Priority;
import android.support.annotation.Nullable;

import com.lizy.myglide.load.DataSource;

/**
 * Created by lizy on 16-4-27.
 */
public interface DataFetcher<T> {

    interface DataCallback<T> {
        void onDataReady(@Nullable T t);

        void onLoadFailed(Exception e);
    }

    void loadData(Priority priority, DataCallback<? super T> callback);

    void cleanup();

    void cancel();

    Class<T> getDataClass();

    DataSource getDataSource();
}
