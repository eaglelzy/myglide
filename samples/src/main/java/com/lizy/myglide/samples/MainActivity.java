package com.lizy.myglide.samples;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lizy.myglide.Registry;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.data.DataFetcher;
import com.lizy.myglide.load.engine.DecodePath;
import com.lizy.myglide.load.engine.GlideException;
import com.lizy.myglide.load.engine.LoadPath;
import com.lizy.myglide.load.engine.Resource;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.load.engine.bitmap_recycle.LruArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.LruBitmapPool;
import com.lizy.myglide.load.model.GlideUrl;
import com.lizy.myglide.load.model.ModelLoader;
import com.lizy.myglide.load.model.StringLoader;
import com.lizy.myglide.load.model.stream.HttpGlideUrlLoader;
import com.lizy.myglide.load.model.stream.HttpUriLoader;
import com.lizy.myglide.load.resource.bitmap.BitmapDrawableDecoder;
import com.lizy.myglide.load.resource.bitmap.Downsampler;
import com.lizy.myglide.load.resource.bitmap.StreamBitmapDecoder;
import com.lizy.myglide.load.resource.transcode.BitmapDrawableTanscoder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.img_test);
    }

    public void test(View v) {

        Resources resources = getResources();
        BitmapPool bitmapPool = new LruBitmapPool(50 * 1024 * 1024);
        ArrayPool arrayPool = new LruArrayPool(8 * 1024 * 1024);
        Downsampler downsampler = new Downsampler(resources.getDisplayMetrics(), bitmapPool, arrayPool);

        final Registry registry = new Registry(this);
        registry.append(String.class, InputStream.class, new StringLoader.StreamFactory());
        registry.append(Uri.class, InputStream.class, new HttpUriLoader.Factory());
        registry.append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory());

        registry.append(InputStream.class, Bitmap.class, new StreamBitmapDecoder(downsampler));
        registry.append(InputStream.class, BitmapDrawable.class,
                new BitmapDrawableDecoder<>(resources, bitmapPool,
                        new StreamBitmapDecoder(downsampler)));

        registry.register(Bitmap.class, BitmapDrawable.class,
                new BitmapDrawableTanscoder(resources, bitmapPool));

        String url = "http://www.google.com";
        List<ModelLoader<String, ?>> loaders = registry.getModelLoaders(url);

        List<ModelLoader.LoadData<?>> loadDatas = new ArrayList<>();
        for (ModelLoader<String, ?> loader : loaders) {
            if (loader.handles(url)) {
                ModelLoader.LoadData<?> loadData = loader.buildLoadData(url, 100, 100, new Options());
                loadDatas.add(loadData);
            }
        }
        for (final ModelLoader.LoadData<?> loadData : loadDatas) {
            loadData.fetcher.loadData(RenderScript.Priority.LOW, new DataFetcher.DataCallback<Object>() {
                @Override
                public void onDataReady(@Nullable Object o) {
                    LoadPath loadPath = registry.getLoadPath(o.getClass(), Object.class, BitmapDrawable.class);
                    try {
                        Resource<BitmapDrawable> bitmapDrawableResource = loadPath.load(
                                o, new Options(), 100, 100, new DecodePath.DecodeCallback() {
                            @Override
                            public Resource onResourceDecoded(Resource resource) {
                                return resource;
                            }
                        });
                        imageView.setImageDrawable(bitmapDrawableResource.get());
                    } catch (GlideException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                }
            });
        }
    }
}
