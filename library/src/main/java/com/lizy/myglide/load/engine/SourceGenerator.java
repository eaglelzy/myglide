package com.lizy.myglide.load.engine;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.load.model.ModelLoader;

/**
 * Created by lizy on 16-5-5.
 */
public class SourceGenerator implements DataFetcherGenerator,
        DataFetcher.DataCallback<Object>,
        DataFetcherGenerator.FetcherReadyCallback {

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
        //TODO:
    }

    @Override
    public void onDataReady(@Nullable Object data) {
        DiskCacheStrategy diskCacheStrategy = helper.getDiskCacheStrategy();
        if (data != null && diskCacheStrategy.isDataCacheable(loadData.fetcher.getDataSource())) {
            //TODO:
        } else {
            cb.onDataFetcherReady(loadData.sourceKey, data, loadData.fetcher,
                    loadData.fetcher.getDataSource(), originalKey);
        }
    }

    @Override
    public void onLoadFailed(Exception e) {

    }

    @Override
    public void reschedule() {

    }

    @Override
    public void onDataFetcherReady(Key sourceKey, @Nullable Object data,
                                   DataFetcher<?> fetcher,
                                   DataSource dataSource, Key attemptedKey) {
        cb.onDataFetcherReady(sourceKey, data, fetcher, dataSource, attemptedKey);
    }

    @Override
    public void onDataFetcherFailed(Key attemptedKey, Exception e,
                                    DataFetcher<?> fetcher, DataSource dataSource) {

    }
}
