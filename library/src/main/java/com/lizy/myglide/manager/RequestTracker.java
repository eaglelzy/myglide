package com.lizy.myglide.manager;

import com.lizy.myglide.request.Request;
import com.lizy.myglide.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by lizy on 16-5-1.
 */
public class RequestTracker {
    private final Set<Request> requests =
            Collections.newSetFromMap(new WeakHashMap<Request, Boolean>());

    private List<Request> pendingRequests = new ArrayList<>();

    private boolean isPause;

    public void runRequest(Request request) {
        requests.add(request);
        if (!isPause) {
            request.begin();
        } else {
            pendingRequests.add(request);
        }
    }

    public boolean isPaused() {
        return isPause;
    }

    public void resumeRequests() {
        isPause = false;
        for (Request request : Util.getSnapshot(requests)) {
            if (!request.isComplete() && !request.isCanceled() && !request.isRunning()) {
                request.begin();
            }
        }
        pendingRequests.clear();
    }

    public void pauseRequests() {
        isPause = true;
        for (Request request : Util.getSnapshot(requests)) {
            if (request.isRunning()) {
                request.pause();
                pendingRequests.add(request);
            }
        }
    }

    public void restartRequests() {
        for (Request request : Util.getSnapshot(requests)) {
            if (!request.isComplete() && !request.isCanceled()) {
                request.pause();
                if (!isPause) {
                    request.begin();
                } else {
                    pendingRequests.add(request);
                }
            }
        }
    }

    public void cleanRequests() {
        for (Request request : Util.getSnapshot(requests)) {
            clearRemoveAndRecycle(request);
        }
        pendingRequests.clear();
    }

    public boolean clearRemoveAndRecycle(Request request) {
        boolean isOwnByUs =
                request != null && (requests.remove(request) || pendingRequests.remove(request));
        if (isOwnByUs) {
            request.clear();
            request.recycle();
        }
        return isOwnByUs;
    }
}
