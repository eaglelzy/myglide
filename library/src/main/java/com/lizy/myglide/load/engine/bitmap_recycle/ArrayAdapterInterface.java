package com.lizy.myglide.load.engine.bitmap_recycle;

/**
 * Created by lizy on 16-4-22.
 */
public interface ArrayAdapterInterface<T> {
  /**
   * TAG for logging.
   */
  String getTag();

  /**
   * Return the length of the given array.
   */
  int getArrayLength(T array);

  /**
   * Reset the array for re-use (e.g. set all values to 0).
   */
  void resetArray(T array);

  /**
   * Allocate and return an array of the specified size.
   */
  T newArray(int length);

  /**
   * Return the size of an element in the array in bytes (e.g. for int return 4).
   */
  int getElementSizeInBytes();
}
