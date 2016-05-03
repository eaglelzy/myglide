package com.lizy.myglide.request;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class BaseRequestOptions<CHILD extends BaseRequestOptions<CHILD>>
        implements Cloneable {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
