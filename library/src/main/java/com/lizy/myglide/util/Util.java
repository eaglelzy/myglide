package com.lizy.myglide.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
