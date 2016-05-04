package com.lizy.myglide.signature;

import com.lizy.myglide.load.Key;

import java.security.MessageDigest;

/**
 * Created by lizy on 16-5-4.
 */
public class EmptySignature implements Key {

    private static final EmptySignature EMPTY_SIGNATURE = new EmptySignature();

    public static EmptySignature obtain() {
        return EMPTY_SIGNATURE;
    }

    private EmptySignature() {
    }

    @Override
    public String toString() {
        return "EmptySignature";
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }
}
