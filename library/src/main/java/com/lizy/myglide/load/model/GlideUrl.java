package com.lizy.myglide.load.model;

import android.net.Uri;
import android.text.TextUtils;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.util.Preconditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Created by lizy on 16-4-27.
 */
public class GlideUrl implements Key {

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

    private final URL url;
    private final String stringUrl;

    private final Headers headers;

    private String safeStringUrl;
    private URL safeUrl;

    private volatile byte[] cacheKeyBytes;

    public GlideUrl(URL url) {
        this(url, Headers.DEFAULT);
    }
    public GlideUrl(String url) {
        this(url, Headers.DEFAULT);
    }
    public GlideUrl(String url, Headers headers) {
        this.url = null;
        this.stringUrl = Preconditions.checkNotEmpty(url);
        this.headers = Preconditions.checkNotNull(headers);
    }
    public GlideUrl(URL url, Headers headers) {
        this.url = Preconditions.checkNotNull(url);
        this.stringUrl = null;
        this.headers = Preconditions.checkNotNull(headers);
    }

    public URL toURL() throws MalformedURLException {
        return getSafeUrl();
    }

    private URL getSafeUrl() throws MalformedURLException {
        if (safeUrl == null) {
            safeUrl = new URL(getSafeStringUrl());
        }
        return safeUrl;
    }

    private String getSafeStringUrl() {
        if (TextUtils.isEmpty(safeStringUrl)) {
            String unsafeStringUrl = stringUrl;
            if (TextUtils.isEmpty(unsafeStringUrl)) {
                unsafeStringUrl = url.toString();
            }
            safeStringUrl = Uri.encode(unsafeStringUrl, ALLOWED_URI_CHARS);
        }

        return safeStringUrl;
    }

    public Map<String, String> getHeaders() {
        return headers.getHeaders();
    }

    public String getCacheKey() {
        return stringUrl != null ? stringUrl : url.toString();
    }

    private byte[] getCacheKeyBytes() {
        if (cacheKeyBytes == null) {
            cacheKeyBytes = getCacheKey().getBytes(CHARSET);
        }
        return cacheKeyBytes;
    }

    @Override
    public String toString() {
        return getSafeStringUrl();
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(getCacheKeyBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlideUrl glideUrl = (GlideUrl) o;

        return getCacheKey().equals(glideUrl.getCacheKey())
                && headers.equals(glideUrl.headers);

    }

    @Override
    public int hashCode() {
        int hashCode = getCacheKey().hashCode();
        hashCode += 31 * hashCode + headers.hashCode();
        return hashCode;
    }
}
