package com.lizy.myglide;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.lizy.myglide.load.engine.Engine;
import com.lizy.myglide.load.engine.bitmap_recycle.ArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPool;
import com.lizy.myglide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.lizy.myglide.load.engine.bitmap_recycle.LruArrayPool;
import com.lizy.myglide.load.engine.bitmap_recycle.LruBitmapPool;
import com.lizy.myglide.load.engine.cache.DiskCache;
import com.lizy.myglide.load.engine.cache.InternalDiskCacheGetterFactory;
import com.lizy.myglide.load.engine.cache.LruResourceCache;
import com.lizy.myglide.load.engine.cache.MemoryCache;
import com.lizy.myglide.load.engine.cache.MemorySizeCalculator;
import com.lizy.myglide.load.engine.executor.GlideExecutor;
import com.lizy.myglide.manager.ConnectivityMonitorFactory;
import com.lizy.myglide.manager.DefaultConnectivityMonitorFactory;
import com.lizy.myglide.request.RequestOptions;

/**
 * Created by lizy on 16-5-3.
 */
public final class GlideBuilder {

    private final Context context;

    private Engine engine;
    private BitmapPool bitmapPool;
    private ArrayPool arrayPool;
    private MemoryCache memoryCache;
    private GlideExecutor sourceExecutor;
    private GlideExecutor diskCacheExecutor;
    private DiskCache.Factory diskCacheFactory;
    private MemorySizeCalculator memorySizeCalculator;
    private ConnectivityMonitorFactory connectivityMonitorFactory;
    private int logLevel = Log.INFO;

    private RequestOptions defaultRequestOptions = new RequestOptions();

    public GlideBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    public Glide createGlide() {
        if (sourceExecutor == null) {
            sourceExecutor = GlideExecutor.newSourceExecutor();
        }

        if (diskCacheExecutor == null) {
            diskCacheExecutor = GlideExecutor.newDiskCacheExecutor();
        }

        if (memorySizeCalculator == null) {
            memorySizeCalculator = new MemorySizeCalculator.Builder(context).build();
        }

        if (connectivityMonitorFactory == null) {
            connectivityMonitorFactory = new DefaultConnectivityMonitorFactory();
        }

        if (bitmapPool == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                bitmapPool = new LruBitmapPool(memorySizeCalculator.getBitmapPoolSize());
            } else {
                bitmapPool = new BitmapPoolAdapter();
            }
        }

        if (arrayPool == null) {
            arrayPool = new LruArrayPool(memorySizeCalculator.getArrayPoolSizeInBytes());
        }

        if (memoryCache == null) {
            memoryCache = new LruResourceCache(memorySizeCalculator.getMemoryCacheSize());
        }

        if (diskCacheFactory == null) {
            diskCacheFactory = new InternalDiskCacheGetterFactory(context);
        }

        if (engine == null) {
            engine = new Engine(memoryCache, diskCacheFactory, diskCacheExecutor, sourceExecutor);
        }
        return new Glide(context,
                engine,
                bitmapPool,
                memoryCache,
                arrayPool,
                connectivityMonitorFactory,
                logLevel,
                defaultRequestOptions);
    }
}
