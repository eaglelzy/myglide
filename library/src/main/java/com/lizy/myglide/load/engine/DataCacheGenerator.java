package com.lizy.myglide.load.engine;

import android.support.annotation.Nullable;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.load.model.ModelLoader;

import java.io.File;
import java.util.List;

/**
 * Created by lizy on 16-5-5.
 */
public class DataCacheGenerator implements DataFetcherGenerator,
        DataFetcher.DataCallback<Object> {
    private final List<Key> keys;
    private final DecodeHelper<?> helper;
    private final FetcherReadyCallback cb;

    private List<ModelLoader<File, ?>> modelLoaders;
    private int souceIdIndex = -1;
    private int modelLoaderIndex = -1;
    private File cacheFile;
    private Key sourceKey;
    private ModelLoader.LoadData<?> loadData;

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
        ModelLoader.LoadData local = loadData;
        if (local != null) {
            local.fetcher.cancel();
        }
    }

    @Override
    public boolean startNetxt() {
        while (modelLoaders == null || !hasNextLoaders()) {
            souceIdIndex++;
            if (souceIdIndex >= keys.size()) {
                return false;
            }

            Key sourceId = keys.get(souceIdIndex);
            Key orginalKey = new DataCacheKey(sourceId, helper.getSignature());
            cacheFile = helper.getDiskCache().get(orginalKey);
            if (cacheFile != null) {
                this.sourceKey = sourceId;
                modelLoaders = helper.getModelLoaders(cacheFile);
                modelLoaderIndex = 0;
            }
        }

        loadData = null;
        boolean started = false;
        while(!started && hasNextLoaders()) {
            ModelLoader<File, ?> modelLoader = modelLoaders.get(modelLoaderIndex);
            loadData = modelLoader.buildLoadData(cacheFile,
                    helper.getWidth(), helper.getHeight(), helper.getOptions());
            if (loadData != null && helper.hasLoadPath(loadData.fetcher.getDataClass())) {
                started = true;
                loadData.fetcher.loadData(helper.getPriority(), this);
            }
        }

        return started;
    }

    private boolean hasNextLoaders() {
        return modelLoaderIndex < modelLoaders.size();
    }

    @Override
    public void onDataReady(@Nullable Object data) {
        cb.onDataFetcherReady(sourceKey, data, loadData.fetcher, DataSource.DATA_DISK_CACHE, sourceKey);
    }

    @Override
    public void onLoadFailed(Exception e) {
        cb.onDataFetcherFailed(sourceKey, e, loadData.fetcher, DataSource.DATA_DISK_CACHE);
    }
}
