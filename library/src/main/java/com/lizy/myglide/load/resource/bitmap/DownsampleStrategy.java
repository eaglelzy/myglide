package com.lizy.myglide.load.resource.bitmap;

/**
 * Created by lizy on 16-5-9.
 */
public abstract class DownsampleStrategy {
    public abstract float getScaleFactor(int sourceWidth, int sourceHeight, int requestWidth,
                                         int requestHeight);

    public abstract SampleSizeRounding getSampleSizeRounding(int sourceWidth,
                                                             int sourceHeight, int requestWidth, int requestHeight);

    public static final DownsampleStrategy AT_LEAST = new AtLeast();

    private static class AtLeast extends DownsampleStrategy {

        @Override
        public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
                                    int requestedHeight) {
            int minIntegerFactor = Math.min(sourceHeight / requestedHeight, sourceWidth / requestedWidth);
            return minIntegerFactor == 0 ? 1f : 1f / Integer.highestOneBit(minIntegerFactor);
        }

        @Override
        public SampleSizeRounding getSampleSizeRounding(int sourceWidth, int sourceHeight,
                                                        int requestedWidth, int requestedHeight) {
            return SampleSizeRounding.QUALITY;
        }
    }

    public enum SampleSizeRounding {

        MEMORY,

        QUALITY
    }
}
