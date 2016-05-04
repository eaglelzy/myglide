package com.lizy.myglide;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.lizy.myglide.load.MemoryCategory;
import com.lizy.myglide.load.engine.Engine;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.load.engine.cache.MemoryCache;
import com.lizy.myglide.load.model.GlideUrl;
import com.lizy.myglide.load.model.StringLoader;
import com.lizy.myglide.load.model.stream.HttpGlideUrlLoader;
import com.lizy.myglide.load.model.stream.HttpUriLoader;
import com.lizy.myglide.load.resource.bitmap.BitmapDrawableDecoder;
import com.lizy.myglide.load.resource.bitmap.Downsampler;
import com.lizy.myglide.load.resource.bitmap.StreamBitmapDecoder;
import com.lizy.myglide.load.resource.transcode.BitmapDrawableTanscoder;
import com.lizy.myglide.manager.ConnectivityMonitorFactory;
import com.lizy.myglide.manager.RequestManagerRetriever;
import com.lizy.myglide.module.GlideModule;
import com.lizy.myglide.module.ManifestParser;
import com.lizy.myglide.request.RequestOptions;
import com.lizy.myglide.request.target.ImageViewTargetFactory;
import com.lizy.myglide.util.Util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-4-27.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Glide implements ComponentCallbacks2 {
    private final Engine engine;
    private final BitmapPool bitmapPool;
    private final MemoryCache memoryCache;
//    private final BitmapPreFiller bitmapPreFiller;
    private final GlideContext glideContext;
    private final Registry registry;
    private final ArrayPool arrayPool;
    private final ConnectivityMonitorFactory connectivityMonitorFactory;
    private final List<RequestManager> managers = new ArrayList<>();

    private static Glide glide;

    public static Glide get(Context context) {
        if (glide == null) {
            synchronized (Glide.class) {
                Context applicationContext = context.getApplicationContext();
                List<GlideModule> modules = new ManifestParser(applicationContext).parse();

                GlideBuilder builder = new GlideBuilder(applicationContext);
                for (GlideModule module : modules) {
                    module.applyOptions(applicationContext, builder);
                }
                glide = builder.createGlide();
                for (GlideModule module : modules) {
                    module.registerComponents(applicationContext, glide.registry);
                }
            }
        }
        return glide;
    }

    public Glide(
            Context context,
            Engine engine,
            BitmapPool bitmapPool,
            MemoryCache memoryCache,
            ArrayPool arrayPool,
            ConnectivityMonitorFactory connectivityMonitorFactory,
            int logLevel,
            RequestOptions requestOptions) {
        this.engine = engine;
        this.bitmapPool = bitmapPool;
        this.memoryCache = memoryCache;
        this.arrayPool = arrayPool;
        this.connectivityMonitorFactory = connectivityMonitorFactory;

        Resources resources = context.getResources();

        Downsampler downsampler =
                new Downsampler(resources.getDisplayMetrics(), bitmapPool, arrayPool);

        registry = new Registry(context);

        registry.append(String.class, InputStream.class, new StringLoader.StreamFactory());
        registry.append(Uri.class, InputStream.class, new HttpUriLoader.Factory());
        registry.append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory());

        registry.append(InputStream.class, Bitmap.class, new StreamBitmapDecoder(downsampler));
        registry.append(InputStream.class, BitmapDrawable.class,
                new BitmapDrawableDecoder<>(resources, bitmapPool,
                        new StreamBitmapDecoder(downsampler)));

        registry.register(Bitmap.class, BitmapDrawable.class,
                new BitmapDrawableTanscoder(resources, bitmapPool));

        ImageViewTargetFactory viewTargetFactory = new ImageViewTargetFactory();

        this.glideContext = new GlideContext(context,
                registry, this, viewTargetFactory, requestOptions, engine, logLevel);
    }

    public static RequestManager with(Activity activity) {
        RequestManagerRetriever retriever = new RequestManagerRetriever();
        return retriever.get(activity);
    }

    public static RequestManager with(FragmentActivity activity) {
        RequestManagerRetriever retriever = new RequestManagerRetriever();
        return retriever.get(activity);
    }

    public static RequestManager with(Fragment fragment) {
        RequestManagerRetriever retriever = new RequestManagerRetriever();
        return retriever.get(fragment);
    }

    public static RequestManager with(android.app.Fragment fragment) {
        RequestManagerRetriever retriever = new RequestManagerRetriever();
        return retriever.get(fragment);
    }

    public static RequestManager with(Context context) {
        RequestManagerRetriever retriever = new RequestManagerRetriever();
        return retriever.get(context);
    }

    public GlideContext getGlideContext() {
        return glideContext;
    }

    public BitmapPool getBitmapPool() {
        return bitmapPool;
    }

    public ArrayPool getArrayPool() {
        return arrayPool;
    }

    public ConnectivityMonitorFactory getConnectivityMonitorFactory() {
        return connectivityMonitorFactory;
    }

    public void setMemoryCache(MemoryCategory memoryCategory) {
        Util.assertMainThread();
        float multiplier = memoryCategory.getMultiplier();
        bitmapPool.setSizeMultiplier(multiplier);
        memoryCache.setSizeMultiplier(multiplier);
    }

    //just for testing
    static void tearDown() {
        glide = null;
    }

    private void trimMemory(int level) {
        Util.assertMainThread();
        memoryCache.trimMemory(level);
        bitmapPool.trimMemory(level);
        arrayPool.trimMemory(level);
    }

    private void clearMemory() {
        Util.assertMainThread();
        memoryCache.clearMemory();
        bitmapPool.clearMemory();
        arrayPool.clearMemory();
    }
    @Override
    public void onTrimMemory(int level) {
        trimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public void onLowMemory() {
        clearMemory();
    }
}
