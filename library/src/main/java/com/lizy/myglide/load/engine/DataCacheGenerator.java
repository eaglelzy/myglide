package com.lizy.myglide.load.engine;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.data.DataFetcher;

import java.util.List;

/**
 * Created by lizy on 16-5-5.
 */
public class DataCacheGenerator implements DataFetcherGenerator,
        DataFetcher.DataCallback<Object> {
    private final List<Key> keys;
    private final DecodeHelper<?> helper;
    private final FetcherReadyCallback cb;

    DataCacheGenerator(DecodeHelper<?> helper, FetcherReadyCallback cb) {
        this(helper.getCacheKeys(), helper, cb);
    }

    DataCacheGenerator(List<Key> keys, DecodeHelper<?> helper, FetcherReadyCallback cb) {
        this.keys = keys;
        this.helper = helper;
        this.cb = cb;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean startNetxt() {
        return false;
    }

    @Override
    public void onDataReady(@Nullable Object o) {

    }

    @Override
    public void onLoadFailed(Exception e) {

    }
}
