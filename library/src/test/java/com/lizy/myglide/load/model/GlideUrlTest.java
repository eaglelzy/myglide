package com.lizy.myglide.load.model;

import com.google.common.testing.EqualsTester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by lizy on 16-4-28.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class GlideUrlTest {

    @Test(expected = NullPointerException.class)
    public void testThrowsIfGivenURLisNull() {
        new GlideUrl((URL)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsIfGivenStringisNull() {
        new GlideUrl((String)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsIfGivenStringisEmpty() {
        new GlideUrl("");
    }
    @Test
    public void testHashCode() throws MalformedURLException {
        String url = "http://wwww.google.com";
        GlideUrl glideUrl = new GlideUrl(url);
        assertEquals(glideUrl.hashCode(), new GlideUrl(url).hashCode());
    }

    @Test
    public void testEquals() throws MalformedURLException {
        Headers headers = mock(Headers.class);
        Headers otherHeaders = mock(Headers.class);
        String url = "http://www.google.com";
        String otherUrl = "http://mail.google.com";
        new EqualsTester()
                .addEqualityGroup(
                        new GlideUrl(url),
                        new GlideUrl(url),
                        new GlideUrl(new URL(url)),
                        new GlideUrl(new URL(url))
                )
                .addEqualityGroup(
                        new GlideUrl(otherUrl),
                        new GlideUrl(new URL(otherUrl))
                )
                .addEqualityGroup(
                        new GlideUrl(url, headers),
                        new GlideUrl(new URL(url), headers)
                )
                .addEqualityGroup(
                        new GlideUrl(url, otherHeaders),
                        new GlideUrl(new URL(url), otherHeaders)
                ).testEquals();
    }
}
