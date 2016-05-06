package com.lizy.myglide.request;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.lizy.myglide.Priority;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.DiskCacheStrategy;
import com.lizy.myglide.load.engine.Transformation;
import com.lizy.myglide.signature.EmptySignature;
import com.lizy.myglide.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class BaseRequestOptions<CHILD extends BaseRequestOptions<CHILD>>
        implements Cloneable {
    private static final int UNSET = -1;

    private int fields;

    private float sizeMultiplier = 1f;
    private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.NONE;
    private Priority priority = Priority.NORMAL;
    private Drawable errorPlaceholder;
    private int errorId;
    private Drawable placeholderDrawable;
    private int placeholderId;
    private boolean isCacheable = true;
    private int overrideHeight = UNSET;
    private int overrideWidth = UNSET;
    private Key signature = EmptySignature.obtain();
    private boolean isTransformationRequired;
    private boolean isTransformationAllowed = true;
    private Drawable fallbackDrawable;
    private int fallbackId;

    private Options options = new Options();
    private Map<Class<?>, Transformation<?>> transformations = new HashMap<>();
    private Class<?> resourceClass = Object.class;
    private boolean isLocked;
    private Resources.Theme theme;
    private boolean isAutoCloneEnabled;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void lock() {

    }

    public final Map<Class<?>, Transformation<?>> getTransformations() {
        return transformations;
    }

    public final boolean isTransformationRequired() {
        return isTransformationRequired;
    }

    public final Options getOptions() {
        return options;
    }

    public final Class<?> getResourceClass() {
        return resourceClass;
    }

    public final DiskCacheStrategy getDiskCacheStrategy() {
        return diskCacheStrategy;
    }

    public final Drawable getErrorPlaceholder() {
        return errorPlaceholder;
    }

    public final int getErrorId() {
        return errorId;
    }

    public final int getPlaceholderId() {
        return placeholderId;
    }

    public final Drawable getPlaceholderDrawable() {
        return placeholderDrawable;
    }

    public final int getFallbackId() {
        return fallbackId;
    }

    public final Drawable getFallbackDrawable() {
        return fallbackDrawable;
    }

    public final Resources.Theme getTheme() {
        return theme;
    }

    public final boolean isMemoryCacheable() {
        return isCacheable;
    }

    public final Key getSignature() {
        return signature;
    }

/*    public final boolean isPrioritySet() {
        return isSet(PRIORITY);
    }*/

    public final Priority getPriority() {
        return priority;
    }

    public final int getOverrideWidth() {
        return overrideWidth;
    }

    public final boolean isValidOverride() {
        return Util.isValidDimensions(overrideWidth, overrideHeight);
    }

    public final int getOverrideHeight() {
        return overrideHeight;
    }

    public final float getSizeMultiplier() {
        return sizeMultiplier;
    }

    private boolean isSet(int flag) {
        return isSet(fields, flag);
    }

    private static boolean isSet(int fields, int flag) {
        return (fields & flag) != 0;
    }
}
