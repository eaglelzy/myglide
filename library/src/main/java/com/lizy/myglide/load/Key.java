package com.lizy.myglide.load;

import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Created by lizy on 16-4-19.
 */
public interface Key {
  String STRING_CHARSET_NAME = "UTF-8";
  Charset CHARSET = Charset.forName(STRING_CHARSET_NAME);

  void updateDiskCacheKey(MessageDigest messageDigest);

  @Override
  boolean equals(Object o);

  @Override
  int hashCode();
}
