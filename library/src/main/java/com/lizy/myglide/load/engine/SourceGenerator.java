package com.lizy.myglide.load.engine;

import android.support.annotation.Nullable;
import android.util.Log;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Encoder;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.util.LogTime;

import java.util.Collections;

/**
 * Created by lizy on 16-5-5.
 */
public class SourceGenerator implements DataFetcherGenerator,
        DataFetcher.DataCallback<Object>,
        DataFetcherGenerator.FetcherReadyCallback {

    private final static String TAG = "SourceGenerator";

    private DataFetcherGenerator sourceGenerator;

    private Object dataToCache;

    private final DecodeHelper<?> helper;
    private final DataFetcherGenerator.FetcherReadyCallback cb;

    private ModelLoader.LoadData<?> loadData;

    private DataCacheKey originalKey;
    private int loadDataListIndex;

    public SourceGenerator(DecodeHelper<?> helper, FetcherReadyCallback cb) {
        this.helper = helper;
        this.cb = cb;
    }

    @Override
    public void cancel() {
        ModelLoader.LoadData<?> local = loadData;
        if (local != null) {
            local.fetcher.cancel();
        }
    }

    @Override
    public boolean startNetxt() {
        if (dataToCache != null) {
            Object data = dataToCache;
            dataToCache = null;
            cacheData(data);
        }

        if (sourceGenerator != null && sourceGenerator.startNetxt()) {
            return true;
        }

        sourceGenerator = null;
        loadData = null;

        boolean started = false;
        while(!started && hasNextModelLoader()) {
            loadData = helper.getLoadData().get(loadDataListIndex++);
            if (loadData != null
                    && (helper.getDiskCacheStrategy().isDataCacheable(loadData.fetcher.getDataSource())
                        || helper.hasLoadPath(loadData.fetcher.getDataClass()))) {
                started = true;
                loadData.fetcher.loadData(helper.getPriority(), this);
            }
        }
        return started;
    }

    private boolean hasNextModelLoader() {
        return loadDataListIndex < helper.getLoadData().size();
    }

    private void cacheData(Object data) {
        long startTime = LogTime.getLogTime();

        try {
            Encoder<Object> encoder = helper.getSourceEncoder(data);
            DataCacheWriter<Object> write = new DataCacheWriter<>(encoder, data, helper.getOptions());
            originalKey = new DataCacheKey(loadData.sourceKey, helper.getSignature());
            helper.getDiskCache().put(originalKey, write);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Finished encoding source to cache"
                        + ", key: " + originalKey
                        + ", data: " + dataToCache
                        + ", encoder: " + encoder
                        + ", duration: " + LogTime.getElapsedMillis(startTime));
            }
        }finally {
            loadData.fetcher.cleanup();
        }

        sourceGenerator = new DataCacheGenerator(Collections.singletonList(loadData.sourceKey),
                helper, this);
    }

    @Override
    public void onDataReady(@Nullable Object data) {
        DiskCacheStrategy diskCacheStrategy = helper.getDiskCacheStrategy();
        if (data != null && diskCacheStrategy.isDataCacheable(loadData.fetcher.getDataSource())) {
            dataToCache = data;
            cb.reschedule();
        } else {
            cb.onDataFetcherReady(loadData.sourceKey, data, loadData.fetcher,
                    loadData.fetcher.getDataSource(), originalKey);
        }
    }

    @Override
    public void onLoadFailed(Exception e) {
        cb.onDataFetcherFailed(originalKey, e, loadData.fetcher, loadData.fetcher.getDataSource());
    }

    @Override
    public void reschedule() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDataFetcherReady(Key sourceKey, @Nullable Object data,
                                   DataFetcher<?> fetcher,
                                   DataSource dataSource, Key attemptedKey) {
        cb.onDataFetcherReady(sourceKey, data, fetcher, loadData.fetcher.getDataSource(), sourceKey);
    }

    @Override
    public void onDataFetcherFailed(Key sourceKey, Exception e,
                                    DataFetcher<?> fetcher, DataSource dataSource) {
        cb.onDataFetcherFailed(sourceKey, e, fetcher, loadData.fetcher.getDataSource());
    }
}
