package com.lizy.myglide;

import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        int data = 255;
        System.out.println(Integer.highestOneBit(data));
        System.out.println(Integer.lowestOneBit(data));
    }
}