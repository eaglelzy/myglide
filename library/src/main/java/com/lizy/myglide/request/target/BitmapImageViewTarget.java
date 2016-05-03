package com.lizy.myglide.request.target;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

/**
 * Created by lizy on 16-5-3.
 */
public class BitmapImageViewTarget extends ImageViewTarget<Bitmap> {
    public BitmapImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable Bitmap resource) {
        view.setImageBitmap(resource);
    }
}

