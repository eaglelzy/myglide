package com.lizy.myglide.load.engine;

/**
 * Created by lizy on 16-4-22.
 */
public interface Resource<Z> {

    Class<Z> getResourceClass();

    Z get();

    int getSize();

    void recycle();
}
