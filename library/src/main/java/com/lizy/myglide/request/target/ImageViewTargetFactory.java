package com.lizy.myglide.request.target;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by lizy on 16-5-3.
 */
public class ImageViewTargetFactory {
    public static <Z> Target<Z> buildTarget(ImageView imageView, Class<Z> clazz) {
        if (Bitmap.class.equals(clazz)) {
            return (Target<Z>) new BitmapImageViewTarget(imageView);
        } else if (Drawable.class.isAssignableFrom(clazz)) {
            return (Target<Z>) new DrawableImageViewTarget(imageView);
        } else {
            throw new IllegalArgumentException("Unkown class: " + clazz);
        }
    }
}
