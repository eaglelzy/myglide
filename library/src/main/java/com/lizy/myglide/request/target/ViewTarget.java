package com.lizy.myglide.request.target;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.lizy.myglide.request.Request;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizy on 16-5-3.
 */
public abstract class ViewTarget<T extends View, Z> extends BaseTarget<Z> {

    private final SizeDeterminer sizeDeterminer;

    protected final T view;

    private static boolean isTagUsedAtLeastOnce = false;
    private static Integer tagId = null;

    public ViewTarget(T view) {
        this.view = view;
        sizeDeterminer = new SizeDeterminer(view);
    }

    public View getView() {
        return view;
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeHolder) {
        super.onLoadCleared(placeHolder);
        sizeDeterminer.clearCallbacksAndListener();
    }

    @Override
    public void getSize(SizeReadyCallback callback) {
        sizeDeterminer.getSize(callback);
    }

    @Override
    public void setRequest(@Nullable Request request) {
        setTag(request);
    }

    @Nullable
    @Override
    public Request getRequest() {
        Object tag = getTag();
        Request request = null;
        if (tag != null) {
            if (tag instanceof Request) {
                request = (Request)tag;
            } else {
                throw new IllegalArgumentException("you must call setTag() on a view Glide is targeting");
            }
        }

        return request;
    }

    private void setTag(Request request) {
        if (tagId != null) {
            isTagUsedAtLeastOnce = true;
            view.setTag(tagId, request);
        } else {
            view.setTag(request);
        }
    }

    @Nullable
    private Object getTag() {
        if (tagId == null) {
            return view.getTag();
        } else {
            return view.getTag(tagId);
        }
    }

    public static void setTagId(Integer tagId) {
        if (ViewTarget.tagId != null && isTagUsedAtLeastOnce) {
            throw new IllegalArgumentException("you cannot set the tagId mone than once or change" +
                    "the tag id after the first request has been made");
        }
        ViewTarget.tagId = tagId;
    }

    private static class SizeDeterminer {
        private static final int PENDING_SIZE = 0;
        private final View view;
        private final List<SizeReadyCallback> cbs = new ArrayList<>();

        private SizeDeterminerLayoutListener layoutListener;
        private Point displayDimens;

        public SizeDeterminer(View view) {
            this.view = view;
        }

        private void notifyCbs(int width, int height) {
            for (SizeReadyCallback cb : cbs) {
                cb.onSizeReady(width, height);
            }
        }
        private void checkCurrentDimens() {
            if (cbs == null) {
                return;
            }

            int width = getViewWidthOrParams();
            int height = getViewHeightOrParams();

            if (!isSizeValid(width) || !isSizeValid(height)) {
                return;
            }

            notifyCbs(width, height);
            clearCallbacksAndListener();
        }

        private void clearCallbacksAndListener() {
            ViewTreeObserver observer = view.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnPreDrawListener(layoutListener);
            }
            layoutListener = null;
            cbs.clear();
        }

        void getSize(SizeReadyCallback cb) {
            int width = getViewWidthOrParams();
            int height = getViewHeightOrParams();
            if (isSizeValid(width) && isSizeValid(height)) {
                cb.onSizeReady(width, height);
            } else {
                if (!cbs.contains(cb)) {
                    cbs.add(cb);
                }
                if (layoutListener == null) {
                    final ViewTreeObserver observable = view.getViewTreeObserver();
                    layoutListener = new SizeDeterminerLayoutListener(this);
                    observable.addOnPreDrawListener(layoutListener);
                }
            }
        }

        private int getViewHeightOrParams() {
            LayoutParams params = view.getLayoutParams();
            if (isSizeValid(view.getHeight())) {
                return view.getHeight();
            } else if(params != null) {
                return getSizeForParam(params.height, true);
            } else {
                return PENDING_SIZE;
            }
        }

        private int getViewWidthOrParams() {
            LayoutParams params = view.getLayoutParams();
            if (isSizeValid(view.getWidth())) {
                return view.getWidth();
            } else if(params != null) {
                return getSizeForParam(params.width, false);
            } else {
                return PENDING_SIZE;
            }
        }

        private int getSizeForParam(int param, boolean isHeight) {
            if (param == LayoutParams.WRAP_CONTENT) {
                Point displayDimens = getDisplayDimens();
                return isHeight ? displayDimens.y : displayDimens.x;
            } else {
                return param;
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        @SuppressWarnings("deprecation")
        private Point getDisplayDimens() {
            if (displayDimens != null) {
                return displayDimens;
            }
            WindowManager windowManager =
                    (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                displayDimens = new Point();
                display.getSize(displayDimens);
            } else {
                displayDimens = new Point(display.getWidth(), display.getHeight());
            }
            return displayDimens;
        }

        private boolean isSizeValid(int size) {
            return size > 0 || size == LayoutParams.WRAP_CONTENT;
        }

        private static class SizeDeterminerLayoutListener implements
                ViewTreeObserver.OnPreDrawListener {

            private final WeakReference<SizeDeterminer> sizeDeterminerRef;

            public SizeDeterminerLayoutListener(SizeDeterminer sizeDeterminerRef) {
                this.sizeDeterminerRef = new WeakReference<>(sizeDeterminerRef);
            }

            @Override
            public boolean onPreDraw() {
                SizeDeterminer sizeDeterminer = sizeDeterminerRef.get();
                if (sizeDeterminer != null) {
                    sizeDeterminer.checkCurrentDimens();
                }
                return true;
            }
        }
    }
}
