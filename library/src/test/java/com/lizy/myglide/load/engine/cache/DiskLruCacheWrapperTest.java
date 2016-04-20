package com.lizy.myglide.load.engine.cache;

import com.lizy.myglide.load.Key;
import com.lizy.myglide.util.Util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by lizy on 16-4-19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class DiskLruCacheWrapperTest {

    private static final int KEY_NUM = 10;
    private File dir;
    private DiskLruCacheWrapper cache;
    private StringKey[] keys = new StringKey[KEY_NUM];
    private byte[] data;

    @Before
    public void setUp() throws Exception {
        dir = RuntimeEnvironment.application.getCacheDir();
        cache = new DiskLruCacheWrapper(dir, 10 * 1024 * 1024);
        data = new byte[] {1, 2, 3, 4, 5, 6};
        for (int i = 0; i < KEY_NUM; i++) {
            keys[i] = new StringKey(String.valueOf(i));
        }
    }

    @Test
    public void testPut() throws Exception {
        cache.put(keys[0], new DiskCache.Writer() {
            @Override
            public boolean writer(File file) {
                try {
                    Util.writeFile(file, data);
                } catch (IOException e) {
                    fail(e.toString());
                    e.printStackTrace();
                }
                return true;
            }
        });
        byte[] value = Util.readFile(cache.get(keys[0]), data.length);
        assertArrayEquals(value, data);
    }


    @Test
    public void testDelete() throws Exception {
        cache.put(keys[1], new DiskCache.Writer() {
            @Override
            public boolean writer(File file) {
                try {
                    Util.writeFile(file, data);
                } catch (IOException e) {
                    fail(e.toString());
                    e.printStackTrace();
                }
                return true;
            }
        });

        byte[] value = Util.readFile(cache.get(keys[1]), data.length);
        assertArrayEquals(value, data);

        cache.delete(keys[1]);

        assertNull(cache.get(keys[1]));
    }

    @Test
    public void testClear() throws Exception {
        for (int i = 0; i < KEY_NUM; i++) {
            cache.put(keys[i], new DiskCache.Writer() {
                @Override
                public boolean writer(File file) {
                    try {
                        Util.writeFile(file, data);
                    } catch (IOException e) {
                        fail(e.toString());
                        e.printStackTrace();
                    }
                    return true;
                }
            });
        }

        cache.clear();

        for (int i = 0; i < KEY_NUM; i++) {
            assertNull(cache.get(keys[i]));
        }
    }

    private static class StringKey implements Key {

        private String key;
        StringKey(String key) {
            this.key = key;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(key.getBytes(CHARSET));
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof StringKey) {
                StringKey other = (StringKey)o;
                return key.equals(other.key);
            }
            return false;
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
