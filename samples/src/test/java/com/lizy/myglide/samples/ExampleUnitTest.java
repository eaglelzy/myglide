package com.lizy.myglide.samples;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class ExampleUnitTest {
    private int count = 0;

    private AtomicInteger atomicInteger = new AtomicInteger();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testAtomic() throws Exception {
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        atomicInteger.getAndIncrement();
                    }
                }
            }).start();
        }

        Thread.sleep(1000);

        System.out.print("count=" + atomicInteger.get());
    }
}