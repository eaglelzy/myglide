package com.lizy.myglide.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;


/**
 * Created by lizy on 16-4-19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class LruCacheTest {

  @Test
  public void putAndGet() throws Exception {
    LruCache<String, String> cache = new LruCache<>(10);
    cache.put("1", "1");
    cache.put("2", "2");

    assertEquals("2", cache.get("2"));
    assertEquals("2", cache.get("2"));
  }

  @Test
  public void putAndGet_evict() throws Exception {
    LruCache<String, String> cache = new LruCache<>(2);
    for (int i = 0; i < 10; i++) {
      cache.put(String.valueOf(i), String.valueOf(i));
    }
    assertEquals(2, cache.getCurrentSize());
  }

  @Test
  public void putAndGet_null() throws Exception {
    LruCache<String, String> cache = new LruCache<>(10);
    cache.put("1", null);
    cache.put(null, "1");

    assertEquals("1", cache.get(null));
    assertEquals(null, cache.get("1"));
    assertEquals(null, cache.get("2"));
  }

  @Test
  public void setMultiplier() throws Exception {
    LruCache<String, String> cache = new LruCache<>(10);
    assertEquals(10, cache.getMaxSize());

    cache.setSizeMultiplier(0.7f);
    assertEquals(7, cache.getMaxSize());
  }

  @Test
  public void remove() throws Exception {
    LruCache<String, String> cache = getCache(10);
    init(cache);

    assertEquals("1", cache.remove("1"));
    assertEquals(9, cache.getCurrentSize());
    assertEquals("2", cache.remove("2"));
    assertEquals(8, cache.getCurrentSize());

  }

  private LruCache<String, String> getCache(int size) {
    return new LruCache<>(size);
  }

  private void init(LruCache cache) {
    for (int i = 0, N = cache.getMaxSize(); i < N; i++) {
      cache.put(String.valueOf(i), String.valueOf(i));
    }
  }
}
