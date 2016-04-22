package com.lizy.myglide.load.engine.cache;

import android.app.ActivityManager;
import android.content.Context;

import com.google.common.collect.Range;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by lizy on 16-4-22.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 19)
public class MemorySizeCalculatorTest {

    private MemoryHarness memoryHarness;

    @Before
    public void setUp() throws Exception {
        memoryHarness = new MemoryHarness();
        ShadowLog.stream = System.out;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testDefaultMemoryCacheSize() throws Exception {
        Shadows.shadowOf(memoryHarness.activityManager).setMemoryClass(getLargeEnoughMemoryClass());
        float memoryCacheSize = memoryHarness.getCalculator().getMemoryCacheSize();
        assertThat(memoryCacheSize).isEqualTo(memoryHarness.getScreenSize() * memoryHarness.memoryCacheScreens);
    }

    @Test
    public void testDefaultMemoryCacheSizeIsLimitedByMemoryClass() {
        final int memoryClassBytes = Math.round(memoryHarness.getScreenSize()
                * memoryHarness.memoryCacheScreens * memoryHarness.sizeMultiplier);

        Shadows.shadowOf(memoryHarness.activityManager).setMemoryClass(memoryClassBytes / (1024 * 1024));

        float memoryCacheSize = memoryHarness.getCalculator().getMemoryCacheSize();

        assertThat((float) memoryCacheSize)
                .isIn(Range.atMost(memoryClassBytes * memoryHarness.sizeMultiplier));
    }

    public int getLargeEnoughMemoryClass() {
        float totalScreenBytes =
                memoryHarness.getScreenSize() * (memoryHarness.bitmapPoolScreens + memoryHarness.memoryCacheScreens);
        float totalBytes = totalScreenBytes + memoryHarness.byteArrayPoolSizeBytes;
        // Memory class is in mb, not bytes!
        float totalMb = totalBytes / (1024 * 1024);
        float memoryClassMb = totalMb / memoryHarness.sizeMultiplier;
        return (int) Math.ceil(memoryClassMb);
    }

    private final static class MemoryHarness {
        int pixelSize = 500;
        int bytesPerPixel = MemorySizeCalculator.BYTES_PER_ARGB_8888_PIXEL;
        float memoryCacheScreens = MemorySizeCalculator.Builder.MEMORY_CACHE_TARGET_SCREENS;
        float bitmapPoolScreens = MemorySizeCalculator.Builder.BITMAP_POOL_TARGET_SCREENS;
        float sizeMultiplier = MemorySizeCalculator.Builder.MAX_SIZE_MULTIPLIER;
        int byteArrayPoolSizeBytes = MemorySizeCalculator.Builder.ARRAY_POOL_SIZE_BYTES;

        ActivityManager activityManager =
                (ActivityManager)RuntimeEnvironment.application.getSystemService(Context.ACTIVITY_SERVICE);
        MemorySizeCalculator.ScreenDimensions screenDimensions
                = mock(MemorySizeCalculator.ScreenDimensions.class);

        public MemorySizeCalculator getCalculator() {
            when(screenDimensions.getHeightPixels()).thenReturn(pixelSize);
            when(screenDimensions.getWidthPixels()).thenReturn(pixelSize);
            return new MemorySizeCalculator.Builder(RuntimeEnvironment.application)
                    .setArrayPoolSize(byteArrayPoolSizeBytes)
                    .setBitmapPoolScreens(bitmapPoolScreens)
                    .setMemoryCacheScreens(memoryCacheScreens)
                    .setScreenDimensions(screenDimensions)
                    .setMaxSizeMultiplier(sizeMultiplier)
                    .build();
        }

        public int getScreenSize() {
            return pixelSize * pixelSize * bytesPerPixel;
        }
    }
}
