package com.lizy.myglide.load.resource.bitmap;

import android.graphics.Bitmap;

import com.lizy.myglide.load.Options;
import com.lizy.myglide.load.ResourceDecoder;
import com.lizy.myglide.load.engine.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lizy on 16-4-29.
 */
public class StreamBitmapDecoder implements ResourceDecoder<InputStream, Bitmap> {

    private final Downsampler downsampler;

    public StreamBitmapDecoder(Downsampler downsampler) {
        this.downsampler = downsampler;
    }

    @Override
    public boolean handles(InputStream source, Options options) throws IOException {
        return downsampler.handles(source);
    }

    // TODO:wrapInputStream
    @Override
    public Resource<Bitmap> decode(InputStream source, int width, int height, Options options) throws IOException {
        return downsampler.decode(source, width, height, options);
    }
}
