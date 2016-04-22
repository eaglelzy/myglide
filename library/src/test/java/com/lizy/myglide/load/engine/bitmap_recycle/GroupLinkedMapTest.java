package com.lizy.myglide.load.engine.bitmap_recycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by lizy on 16-4-21.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class GroupLinkedMapTest {

    private GroupLinkedMap<Key, Object> map;

    @Before
    public void setUp() throws Exception {
        map = new GroupLinkedMap<>();
    }

    @Test
    public void testRetureNullIfNoKey() throws Exception {
        assertNull(map.get(mock(Key.class)));
    }

    @Test
    public void testPutAndGet() throws Exception {
        Key key = new Key("1", 1, 1);
        String value = "1";
        map.put(key, value);

        assertEquals(map.get(key), value);
    }

    @Test
    public void testPutAndGetMoreThanOne() throws Exception {
        Key key = new Key("1", 1, 1);
        int addNum = 10;

        for (int i = 0; i < addNum; i++) {
            map.put(key, new Integer(i));
        }

        printMap();

        for (int i = addNum - 1; i >= 0; i--) {
            assertEquals(map.get(key), new Integer(i));
        }
        printMap();
    }

    @Test
    public void testRemoveLast() throws Exception {
        Key key1 = new Key("1", 1, 1);
        Key key2 = new Key("2", 1, 1);
        Key key3 = new Key("3", 1, 1);
        Integer value1 = new Integer(1);
        Integer value2 = new Integer(2);
        Integer value3 = new Integer(3);

        map.put(key1, value1);
        map.put(key1, new Integer(value1));

        map.put(key2, value2);
        map.put(key3, value3);

        assertEquals(map.removeLast(), value3);
        assertEquals(map.removeLast(), value2);
        assertEquals(map.removeLast(), value1);
        assertEquals(map.removeLast(), value1);
    }

    @Test
    public void testLeastRecentlyRetrievedKeyIsLeastRecentlyUsed() {
        Key firstKey = new Key("key", 1, 1);
        Integer firstValue = 10;
        map.put(firstKey, firstValue);
        map.put(firstKey, new Integer(firstValue));

        Key secondKey = new Key("key", 2, 2);
        Integer secondValue = 20;
        map.put(secondKey, secondValue);

        map.get(firstKey);

        assertEquals(map.removeLast(), secondValue);
    }

    private void printMap() {
        System.out.println(map);
    }

    private class Key implements Poolable {
        private final String key;
        private final int width;
        private final int height;

        public Key(String key, int width, int height) {
            this.key = key;
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key1 = (Key) o;

            if (width != key1.width) return false;
            if (height != key1.height) return false;
            return key != null ? key.equals(key1.key) : key1.key == null;

        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }

        @Override
        public void offer() {

        }

        @Override
        public String toString() {
            return "[" + key + width + height + "]";
        }
    }
}
