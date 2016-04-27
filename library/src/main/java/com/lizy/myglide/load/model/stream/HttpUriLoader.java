package com.lizy.myglide.load.model.stream;

import android.content.Context;
import android.net.Uri;

import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.model.GlideUrl;
import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.load.model.ModelLoaderFactory;
import com.lizy.myglide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lizy on 16-4-27.
 */
public class HttpUriLoader implements ModelLoader<Uri, InputStream> {
    private final static Set<String> SCHEMES =
            Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("http", "https")));
    private final ModelLoader<GlideUrl, InputStream> glideLoader;

    public HttpUriLoader(ModelLoader<GlideUrl, InputStream> glideLoader) {
        this.glideLoader = glideLoader;
    }

    @Override
    public LoadData<InputStream> buildLoadData(Uri model, int width, int height, Options options) {
        return glideLoader.buildLoadData(new GlideUrl(model.toString()), width, height, options);
    }

    @Override
    public boolean handles(Uri model) {
        return SCHEMES.contains(model.getScheme());
    }

    public static class Factory implements ModelLoaderFactory<Uri, InputStream> {

        @Override
        public ModelLoader<Uri, InputStream> build(Context context,
                           MultiModelLoaderFactory multiFactory) {
            return new HttpUriLoader(multiFactory.build(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {

        }
    }
}
