package com.lizy.myglide.util;

import android.graphics.Bitmap;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

import static junit.framework.Assert.assertEquals;

/**
 * Created by lizy on 16-4-19.
 */
public class Util {

  private static final char[] HEX_CHAR_ARRAY = "0123456789abcdef".toCharArray();
  // 32 bytes from sha-256 -> 64 hex chars.
  private static final char[] SHA_256_CHARS = new char[64];

  public static String sha256BytesToHex(byte[] bytes) {
    synchronized (SHA_256_CHARS) {
      return bytesToHex(bytes, SHA_256_CHARS);
    }
  }

  // Taken from:
  // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
  // /9655275#9655275
  @SuppressWarnings("PMD.UseVarargs")
  private static String bytesToHex(byte[] bytes, char[] hexChars) {
    int v;
    for (int j = 0; j < bytes.length; j++) {
      v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_CHAR_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_CHAR_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static void writeFile(File file, byte[] data) throws IOException {
    OutputStream out = new FileOutputStream(file);
    try {
      out.write(data);
      out.flush();
      out.close();
    } finally {
      try {
        out.close();
      } catch (IOException ex) {
        // Do nothing.
      }
    }
  }

  public static byte[] readFile(File file, int expectedLength) throws IOException {
    InputStream is = new FileInputStream(file);
    byte[] result = new byte[expectedLength];
    try {
      assertEquals(expectedLength, is.read(result));
      assertEquals(-1, is.read());
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        // Do nothing.
      }
    }
    return result;
  }

  public static <T> Queue<T> createQueue(int size) {
    return new ArrayDeque<>(size);
  }

  public static int getBitmapByteSize(Bitmap bitmap) {
    if (bitmap.isRecycled()) {
      throw new IllegalStateException("Cannot obtain size for recycled Bitmap: " + bitmap
              + "[" + bitmap.getWidth() + "x" + bitmap.getHeight() + "] " + bitmap.getConfig());
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1. See issue #148.
      try {
        return bitmap.getAllocationByteCount();
      } catch (NullPointerException e) {
        // Do nothing.
      }
    }
    return bitmap.getHeight() * bitmap.getRowBytes();
  }

  /**
   * Returns the in memory size of {@link android.graphics.Bitmap} with the given width, height, and
   * {@link android.graphics.Bitmap.Config}.
   */
  public static int getBitmapByteSize(int width, int height, Bitmap.Config config) {
    return width * height * getBytesPerPixel(config);
  }

  private static int getBytesPerPixel(Bitmap.Config config) {
    // A bitmap by decoding a gif has null "config" in certain environments.
    if (config == null) {
      config = Bitmap.Config.ARGB_8888;
    }

    int bytesPerPixel;
    switch (config) {
      case ALPHA_8:
        bytesPerPixel = 1;
        break;
      case RGB_565:
      case ARGB_4444:
        bytesPerPixel = 2;
        break;
      case ARGB_8888:
      default:
        bytesPerPixel = 4;
        break;
    }
    return bytesPerPixel;
  }

  public static boolean bothNullOrEqual(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }
}
