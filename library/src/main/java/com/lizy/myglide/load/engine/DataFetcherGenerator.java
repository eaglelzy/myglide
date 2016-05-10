package com.lizy.myglide.load.engine;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.data.DataFetcher;

/**
 * Created by lizy on 16-5-5.
 */
public interface DataFetcherGenerator {

    interface FetcherReadyCallback {
        void reschedule();

        void onDataFetcherReady(Key sourceKey, @Nullable Object data, DataFetcher<?> fetcher,
                                DataSource dataSource, Key attemptedKey);

        void onDataFetcherFailed(Key attemptedKey, Exception e, DataFetcher<?> fetcher,
                                 DataSource dataSource);
    }

    void cancel();

    boolean startNext();
}
