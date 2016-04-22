package com.lizy.myglide.load.engine.executor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;


/**
 * Created by lizy on 16-4-22.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class GlideExecutorTest {

    @Test
    public void testLoadInOrder() throws InterruptedException {
        final List<Integer> resultPriorities = Collections.synchronizedList(new ArrayList<Integer>());

        GlideExecutor executor = GlideExecutor.newSourceExecutor();
        for (int i = 5; i > 0; i--) {
            executor.execute(new MockRunnable(i, new MockRunnable.OnRun() {
                @Override
                public void OnRun(int priority) {
                    resultPriorities.add(priority);
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(400, TimeUnit.MILLISECONDS);
        assertThat(resultPriorities).containsExactly(5, 1, 2, 3, 4);
    }

    private static class MockRunnable implements Runnable,
            Comparable<MockRunnable> {

        private int priority;
        private OnRun onRun;

        interface OnRun {
            void OnRun(int priority);
        }
        MockRunnable(int priority, OnRun onRun) {
            this.priority = priority;
            this.onRun = onRun;
        }

        @Override
        public void run() {
            onRun.OnRun(priority);
        }

        @Override
        public int compareTo(MockRunnable another) {
            return priority - another.priority;
        }
    }
}
