package com.lizy.myglide.request;

import android.content.Context;

/**
 * Created by lizy on 16-5-3.
 */
public class RequestOptions extends BaseRequestOptions<RequestOptions> {
    private static RequestOptions circleCropOptions;

    public static RequestOptions circleCropTransform(Context context) {
        if (circleCropOptions  == null) {
            circleCropOptions = new RequestOptions()
                    .circleCrop(context)
                    .autoLock();
        }
        return circleCropOptions;
    }
}
