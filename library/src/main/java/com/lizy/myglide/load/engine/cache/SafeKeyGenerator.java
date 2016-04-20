package com.lizy.myglide.load.engine.cache;

import android.support.v4.util.Pools;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.util.LruCache;
import com.lizy.myglide.util.Util;
import com.lizy.myglide.util.pool.FactoryPools;
import com.lizy.myglide.util.pool.StateVerifier;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lizy on 16-4-19.
 */
public class SafeKeyGenerator {
    private final LruCache<Key, String> loadIdToSafeHash = new LruCache<>(1000);

    private final Pools.Pool<PoolableDigestContainer> digestPool = FactoryPools.threadSafe(10,
            new FactoryPools.Factory<PoolableDigestContainer>() {
                @Override
                public PoolableDigestContainer create() {
                    try {
                        return new PoolableDigestContainer(MessageDigest.getInstance("SHA-256"));
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

    public String getSafeKey(Key key) {
        String safeKey;
        synchronized (loadIdToSafeHash) {
            safeKey = loadIdToSafeHash.get(key);
        }
        if (safeKey == null) {
            safeKey = calculateHexStringDigest(key);
        }

        synchronized (loadIdToSafeHash) {
            loadIdToSafeHash.put(key, safeKey);
        }
        return safeKey;
    }

    private String calculateHexStringDigest(Key key) {
        final PoolableDigestContainer digestContainer = digestPool.acquire();
        try {
            key.updateDiskCacheKey(digestContainer.messageDigest);
            return Util.sha256BytesToHex(digestContainer.messageDigest.digest());
        } finally {
            digestPool.release(digestContainer);
        }
    }

    private static final class PoolableDigestContainer implements FactoryPools.Poolable {

        private final StateVerifier stateVerifier = StateVerifier.newInstance();

        private final MessageDigest messageDigest;

        public PoolableDigestContainer(MessageDigest messageDigest) {
            this.messageDigest = messageDigest;
        }

        @Override
        public StateVerifier getVerifier() {
            return stateVerifier;
        }
    }
}
