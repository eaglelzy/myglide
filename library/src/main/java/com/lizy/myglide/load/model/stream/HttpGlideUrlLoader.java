package com.lizy.myglide.load.model.stream;

import android.content.Context;

import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.data.HttpUrlFetcher;
import com.lizy.myglide.load.model.GlideUrl;
import com.lizy.myglide.load.model.ModelCache;
import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.load.model.ModelLoaderFactory;
import com.lizy.myglide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

/**
 * Created by lizy on 16-4-28.
 */
public class HttpGlideUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private final ModelCache<GlideUrl, GlideUrl> modelCache;

    public HttpGlideUrlLoader() {
        this(null);
    }

    public HttpGlideUrlLoader(ModelCache<GlideUrl, GlideUrl> modelCache) {
        this.modelCache = modelCache;
    }

    @Override
    public LoadData<InputStream> buildLoadData(GlideUrl model, int width, int height, Options Options) {
        GlideUrl url = model;
        if (modelCache != null) {
            url = modelCache.get(model, 0, 0);
            if (url == null) {
                modelCache.put(model, 0, 0, model);
                url = model;
            }
        }
        return new LoadData<>(url, new HttpUrlFetcher(url));
    }

    @Override
    public boolean handles(GlideUrl glideUrl) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {

        @Override
        public ModelLoader<GlideUrl, InputStream> build(Context context,
                                            MultiModelLoaderFactory multiFactory) {
            return new HttpGlideUrlLoader();
        }

        @Override
        public void teardown() {

        }
    }
}
