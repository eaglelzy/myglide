package com.lizy.myglide.load.engine.cache;

import com.lizy.myglide.load.Key;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Created by lizy on 16-4-19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class SafeKeyGeneratorTest {
    private SafeKeyGenerator safeKeyGenerator;
    private int nextId;

    @Before
    public void setUp() throws Exception {
        safeKeyGenerator = new SafeKeyGenerator();
        nextId = 0;
    }

    @Test
    public void testGetSafeKey() {
        Pattern pattern = Pattern.compile("[a-z0-9]{64}");
        for (int i = 0; i < 1000; i++) {
            String key = safeKeyGenerator.getSafeKey(new MockKey(nextId()));
            Matcher matcher = pattern.matcher(key);
            assertTrue(key, matcher.matches());
        }
    }

    private String nextId() {
        return String.valueOf(nextId++);
    }

    private static class MockKey implements Key {

        private String id;

        public MockKey(String id) {
            this.id = id;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(id.getBytes(CHARSET));
        }
    }
}
