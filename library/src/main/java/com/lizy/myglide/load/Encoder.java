package com.lizy.myglide.load;

import java.io.File;

/**
 * Created by lizy on 16-5-5.
 */
public interface Encoder<T> {
    boolean encode(T data, File file, Options options);
}
