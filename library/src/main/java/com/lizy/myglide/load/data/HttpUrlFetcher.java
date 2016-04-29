package com.lizy.myglide.load.data;

import android.renderscript.RenderScript;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.model.GlideUrl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by lizy on 16-4-28.
 */
public class HttpUrlFetcher implements DataFetcher<InputStream> {
    public HttpUrlFetcher(GlideUrl url) {

    }

    @Override
    public void loadData(RenderScript.Priority priority, DataCallback<? super InputStream> callback) {
        try {
            //just for test
            callback.onDataReady(new FileInputStream("/sdcard/Pictures/1024/1.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
