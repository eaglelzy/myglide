package com.lizy.myglide.load;

/**
 * Created by lizy on 16-5-4.
 */
public enum MemoryCategory {

    LOW(0.5f),

    NORMAL(1.0f),

    HIGHT(1.5f);

    private float multiplier;

    MemoryCategory(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getMultiplier() {
        return multiplier;
    }
}
