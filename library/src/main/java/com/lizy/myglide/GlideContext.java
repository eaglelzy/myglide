package com.lizy.myglide;

import android.annotation.TargetApi;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.lizy.myglide.load.engine.Engine;
import com.lizy.myglide.request.RequestOptions;
import com.lizy.myglide.request.target.ImageViewTargetFactory;
import com.lizy.myglide.request.target.Target;

/**
 * Created by lizy on 16-5-3.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class GlideContext extends ContextWrapper implements ComponentCallbacks2 {

    private final Registry registry;
    private final Handler mainHander;
    private final ComponentCallbacks2 componentCallbacks2;
    private final ImageViewTargetFactory imageViewTargetFactory;
    private final RequestOptions defaultRequestOptions;
    private final Engine engine;
    private final int logLevel;

    public GlideContext(Context base,
                        Registry registry,
                        ComponentCallbacks2 componentCallbacks2,
                        ImageViewTargetFactory imageViewTargetFactory,
                        RequestOptions defaultRequestOptions,
                        Engine engine,
                        int logLevel) {
        super(base);
        this.registry = registry;
        this.componentCallbacks2 = componentCallbacks2;
        this.imageViewTargetFactory = imageViewTargetFactory;
        this.defaultRequestOptions = defaultRequestOptions;
        this.engine = engine;
        this.logLevel = logLevel;

        this.mainHander = new Handler(Looper.getMainLooper());
    }

    public Registry getRegistry() {
        return registry;
    }

    public Handler getMainHander() {
        return mainHander;
    }

    public <X> Target<X> buildImageViewTarget(ImageView imageView, Class<X> clazz) {
        return imageViewTargetFactory.buildTarget(imageView, clazz);
    }

    public RequestOptions getDefaultRequestOptions() {
        return defaultRequestOptions;
    }

    public Engine getEngine() {
        return engine;
    }

    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void onTrimMemory(int level) {
        componentCallbacks2.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        componentCallbacks2.onConfigurationChanged(configuration);
    }

    @Override
    public void onLowMemory() {
        componentCallbacks2.onLowMemory();
    }
}
