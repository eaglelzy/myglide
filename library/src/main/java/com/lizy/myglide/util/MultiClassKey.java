package com.lizy.myglide.util;

/**
 * Created by lizy on 16-4-29.
 */
public class MultiClassKey {

    private Class<?> first;
    private Class<?> second;
    private Class<?> third;

    public MultiClassKey() {}

    public MultiClassKey(Class<?> first, Class<?> second) {
        this.first = first;
        this.second = second;
    }

    public MultiClassKey(Class<?> first, Class<?> second, Class<?> third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public void set(Class<?> first, Class<?> second) {
        this.first = first;
        this.second = second;
    }

    public void set(Class<?> first, Class<?> second, Class<?> third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public String toString() {
        return "MultiClassKey{" + "first=" + first + ", second=" + second +
                (third == null ? '}' : ", third=" + third + '}');

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MultiClassKey that = (MultiClassKey) o;

        if (!first.equals(that.first)) {
            return false;
        }
        if (!second.equals(that.second)) {
            return false;
        }
        if (!Util.bothNullOrEqual(third, that.third)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }
}
