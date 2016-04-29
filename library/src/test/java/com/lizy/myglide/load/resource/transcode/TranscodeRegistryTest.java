package com.lizy.myglide.load.resource.transcode;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by lizy on 16-4-28.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class TranscodeRegistryTest {

    private TranscodeRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new TranscodeRegistry();
    }

    @Test
    public void testRegisterAndGet() throws Exception {
        ResourceTranscoder<Bitmap, BitmapDrawable> transcoder = mock(ResourceTranscoder.class);
        registry.register(Bitmap.class, BitmapDrawable.class, transcoder);
        ResourceTranscoder<Bitmap, BitmapDrawable> result = registry.get(Bitmap.class, BitmapDrawable.class);

        assertThat(result).isEqualTo(transcoder);
    }

    @Test
    public void testRegisterAndGetIfResourceAndTranscodeSame() throws Exception {
        ResourceTranscoder<BitmapDrawable, BitmapDrawable> transcoder = mock(ResourceTranscoder.class);
        registry.register(BitmapDrawable.class, BitmapDrawable.class, transcoder);
        ResourceTranscoder<BitmapDrawable, BitmapDrawable> result =
                registry.get(BitmapDrawable.class, BitmapDrawable.class);

        assertThat(result).isEqualTo(UnitTranscoder.get());
    }

    @Test
    public void testTranscodeClasses() throws Exception {
        ResourceTranscoder<Bitmap, BitmapDrawable> transcoder1 = mock(ResourceTranscoder.class);
        ResourceTranscoder<BitmapDrawable, Byte[]> transcoder2 = mock(ResourceTranscoder.class);
        ResourceTranscoder<Bitmap, ColorDrawable> transcoder3 = mock(ResourceTranscoder.class);
        registry.register(Bitmap.class, BitmapDrawable.class, transcoder1);
        registry.register(BitmapDrawable.class, Byte[].class, transcoder2);
        registry.register(Bitmap.class, ColorDrawable.class, transcoder3);

        List<Class<Drawable>> transcodeClasses =
                registry.getTranscodeClasses(Bitmap.class, Drawable.class);
        transcodeClasses.get(0).isInstance(BitmapDrawable.class);
        transcodeClasses.get(1).isInstance(ColorDrawable.class);
    }

}
