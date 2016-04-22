package com.lizy.myglide.load.engine.bitmap_recycle;

import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBitmap;

import static org.junit.Assert.assertEquals;

/**
 * Created by lizy on 16-4-21.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class SizeConfigStrategyTest {
    private SizeConfigStrategy strategy;

    @Before
    public void setUp() throws Exception {
        strategy = new SizeConfigStrategy();
    }

    @Test
    public void testPutAndGet() throws Exception {
        Bitmap bitmap = ShadowBitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        strategy.put(bitmap);
        Bitmap bitmap1 = strategy.get(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        assertEquals(bitmap, bitmap1);
    }

}
