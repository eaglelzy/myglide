package com.lizy.myglide.signature;

import com.lizy.myglide.load.Key;

import java.security.MessageDigest;

/**
 * Created by lizy on 16-5-6.
 */
public final class ObjectKey implements Key {
    private final Object object;

    public ObjectKey(Object object) {
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectKey objectKey = (ObjectKey) o;

        return object != null ? object.equals(objectKey.object) : objectKey.object == null;

    }

    @Override
    public int hashCode() {
        return object != null ? object.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ObjectKey{" +
                "object=" + object +
                '}';
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(object.toString().getBytes(CHARSET));
    }
}
