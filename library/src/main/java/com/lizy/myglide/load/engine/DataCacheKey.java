package com.lizy.myglide.load.engine;

import com.lizy.myglide.load.Key;

import java.security.MessageDigest;

/**
 * Created by lizy on 16-5-5.
 */
final class DataCacheKey implements Key {
    private final Key sourceKey;
    private final Key signature;

    public DataCacheKey(Key sourceKey, Key signature) {
        this.sourceKey = sourceKey;
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataCacheKey that = (DataCacheKey) o;

        if (sourceKey != null ? !sourceKey.equals(that.sourceKey) : that.sourceKey != null)
            return false;
        return signature != null ? signature.equals(that.signature) : that.signature == null;

    }

    @Override
    public int hashCode() {
        int result = sourceKey != null ? sourceKey.hashCode() : 0;
        result = 31 * result + (signature != null ? signature.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DataCacheKey{" +
                "sourceKey=" + sourceKey +
                ", signature=" + signature +
                '}';
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        sourceKey.updateDiskCacheKey(messageDigest);
        signature.updateDiskCacheKey(messageDigest);
    }
}
