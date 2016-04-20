package com.lizy.myglide.util.pool;

/**
 * Created by lizy on 16-4-19.
 */
public abstract class StateVerifier {

    private static final boolean DEBUG = false;

    public static StateVerifier newInstance() {
        if (DEBUG) {
            return new DebugStateVerifier();
        } else {
            return new DefaultStateVerifier();
        }
    }

    public abstract void throwIfRecycled();

    abstract void setRecycled(boolean isRecycled);

    private static class DefaultStateVerifier extends StateVerifier {

        private volatile boolean isReleased;

        @Override
        public void throwIfRecycled() {
            if (isReleased) {
                throw new IllegalStateException("Alreadly released!");
            }
        }

        @Override
        void setRecycled(boolean isRecycled) {
            this.isReleased = isRecycled;
        }
    }

    private static class DebugStateVerifier extends StateVerifier {
        private volatile RuntimeException recycledAtStackTraceException;
        @Override
        public void throwIfRecycled() {
            if (recycledAtStackTraceException != null) {
                throw new IllegalStateException("Already released!", recycledAtStackTraceException);
            }
        }

        @Override
        void setRecycled(boolean isRecycled) {
            if (isRecycled) {
                recycledAtStackTraceException = new RuntimeException("Released");
            } else {
                recycledAtStackTraceException = null;
            }
        }
    }
}
