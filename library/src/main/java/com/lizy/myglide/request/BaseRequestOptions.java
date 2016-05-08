package com.lizy.myglide.request;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.lizy.myglide.Priority;
import com.lizy.myglide.load.Key;
import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.engine.DiskCacheStrategy;
import com.lizy.myglide.load.engine.Transformation;
import com.lizy.myglide.load.resource.bitmap.DownsampleStrategy;
import com.lizy.myglide.load.resource.bitmap.Downsampler;
import com.lizy.myglide.signature.EmptySignature;
import com.lizy.myglide.util.Preconditions;
import com.lizy.myglide.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class BaseRequestOptions<CHILD extends BaseRequestOptions<CHILD>>
        implements Cloneable {
    private static final int UNSET = -1;
    private static final int SIZE_MULTIPLIER = 1 << 1;
    private static final int DISK_CACHE_STRATEGY = 1 << 2;
    private static final int PRIORITY = 1 << 3;
    private static final int ERROR_PLACEHOLDER = 1 << 4;
    private static final int ERROR_ID = 1 << 5;
    private static final int PLACEHOLDER = 1 << 6;
    private static final int PLACEHOLDER_ID = 1 << 7;
    private static final int IS_CACHEABLE = 1 << 8;
    private static final int OVERRIDE = 1 << 9;
    private static final int SIGNATURE = 1 << 10;
    private static final int TRANSFORMATION = 1 << 11;
    private static final int RESOURCE_CLASS = 1 << 12;
    private static final int FALLBACK = 1 << 13;
    private static final int FALLBACK_ID = 1 << 14;
    private static final int THEME = 1 << 15;
    private static final int TRANSFORMATION_ALLOWED = 1 << 16;
    private static final int TRANSFORMATION_REQUIRED = 1 << 17;

    private int fields;

    private float sizeMultiplier = 1f;
    private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.ALL;
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

    public final CHILD diskCacheStrategy(@NonNull DiskCacheStrategy diskCacheStrategy) {
        if (isAutoCloneEnabled) {
            return clone().diskCacheStrategy(diskCacheStrategy);
        }

        this.diskCacheStrategy = Preconditions.checkNotNull(diskCacheStrategy);
        fields |= DISK_CACHE_STRATEGY;

        return selfOrThrowIfLocked();
    }

    @Override
    public CHILD clone() {
        BaseRequestOptions<CHILD> result = null;
        try {
            result = (BaseRequestOptions<CHILD>) super.clone();
            result.options = new Options();
            result.options.putAll(options);
            result.transformations = new HashMap<>();
            result.transformations.putAll(transformations);
            result.isLocked = false;
            result.isAutoCloneEnabled = false;
            return (CHILD) result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public CHILD lock() {
        isLocked = true;
        return (CHILD)this;
    }

    private CHILD selfOrThrowIfLocked() {
        if (isLocked) {
            throw new IllegalStateException("You cannot modify locked RequestOptions, consider clone()");
        }
        return (CHILD)this;
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

    public CHILD apply(BaseRequestOptions<?> other) {
        if (isAutoCloneEnabled) {
            return clone().apply(other);
        }

        if (isSet(other.fields, DISK_CACHE_STRATEGY)) {
            diskCacheStrategy = other.diskCacheStrategy;
        }

        fields |= other.fields;
        options.putAll(other.options);
        return selfOrThrowIfLocked();
    }
}
