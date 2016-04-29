package com.lizy.myglide.load.model;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.lizy.myglide.load.Options;

import java.io.File;
import java.io.InputStream;

/**
 * Created by lizy on 16-4-27.
 */
public class StringLoader<Data> implements ModelLoader<String, Data> {
    private final ModelLoader<Uri, Data> uriLoader;

    public StringLoader(ModelLoader<Uri, Data> uriLoader) {
        this.uriLoader = uriLoader;
    }

    @Override
    public LoadData<Data> buildLoadData(String model, int width, int height, Options Options) {
        Uri uri = parseUri(model);
        return uri == null ? null : uriLoader.buildLoadData(uri, width, height, Options);
    }

    @Nullable
    private static Uri parseUri(String model) {
        Uri uri;
        if (TextUtils.isEmpty(model)) {
            return null;
        } else if (model.startsWith("/")) {
            uri = toFile(model);
        } else {
            uri = Uri.parse(model);
            String scheme = uri.getScheme();
            if (scheme == null) {
                uri = toFile(model);
            }
        }

        return uri;
    }

    private static Uri toFile(String path) {
        return Uri.fromFile(new File(path));
    }

    @Override
    public boolean handles(String s) {
        return true;
    }

    public static class StreamFactory implements ModelLoaderFactory<String, InputStream> {

        @Override
        public ModelLoader<String, InputStream> build(Context context, MultiModelLoaderFactory multiFactory) {
            return new StringLoader(multiFactory.build(Uri.class, InputStream.class));
        }

        @Override
        public void teardown() {

        }
    }
}
