package com.lizy.myglide.load.model;

import com.lizy.myglide.load.Key;

import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by lizy on 16-4-27.
 */
public class GlideUrl implements Key {


    public GlideUrl(URL url) {
        this(url, Headers.DEFAULT);
    }
    public GlideUrl(String url) {
        this(url, Headers.DEFAULT);
    }
    public GlideUrl(String url, Headers headers) {

    }
    public GlideUrl(URL url, Headers headers) {

    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
