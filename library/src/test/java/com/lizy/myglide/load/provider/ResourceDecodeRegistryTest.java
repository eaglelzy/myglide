package com.lizy.myglide.load.provider;

import android.graphics.Bitmap;

import com.lizy.myglide.load.ResourceDecoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Created by lizy on 16-4-28.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class ResourceDecodeRegistryTest {

    private ResourceDecodeRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new ResourceDecodeRegistry();
    }

    @Test
    public void testAppend() throws Exception {
        ResourceDecoder<InputStream, Bitmap> firstDecoder = mock(ResourceDecoder.class);
        ResourceDecoder<InputStream, Bitmap> secondDecoder = mock(ResourceDecoder.class);
        registry.append(firstDecoder, InputStream.class, Bitmap.class);
        registry.append(secondDecoder, InputStream.class, Bitmap.class);

        List<ResourceDecoder<InputStream, Bitmap>> decoders =
                registry.getDecoders(InputStream.class, Bitmap.class);
        assertThat(decoders).containsExactly(firstDecoder, secondDecoder).inOrder();
    }

    @Test
    public void testPrepend() throws Exception {
        ResourceDecoder<InputStream, Bitmap> firstDecoder = mock(ResourceDecoder.class);
        ResourceDecoder<InputStream, Bitmap> secondDecoder = mock(ResourceDecoder.class);
        registry.prepend(firstDecoder, InputStream.class, Bitmap.class);
        registry.prepend(secondDecoder, InputStream.class, Bitmap.class);

        List<ResourceDecoder<InputStream, Bitmap>> decoders =
                registry.getDecoders(InputStream.class, Bitmap.class);
        assertThat(decoders).containsExactly(secondDecoder, firstDecoder).inOrder();
    }

    @Test
    public void testGetDecoders() throws Exception {
        ResourceDecoder<InputStream, Bitmap> firstDecoder = mock(ResourceDecoder.class);
        ResourceDecoder<InputStream, Byte[]> secondDecoder = mock(ResourceDecoder.class);
        registry.prepend(firstDecoder, InputStream.class, Bitmap.class);
        registry.prepend(secondDecoder, InputStream.class, Byte[].class);

        List<Class<Object>> classes =
                registry.getResourceClasses(InputStream.class, Object.class);
        classes.get(0).isInstance(Byte[].class);
        classes.get(1).isInstance(Bitmap.class);
    }
}
