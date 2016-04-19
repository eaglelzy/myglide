package com.lizy.myglide.samples;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.util.Pools.Pool;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.lizy.myglide.util.FactoryPools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String PATH = "/sdcard/Pictures/OGQ/qq.jpg";

    private int num;

    ExecutorService executorService = new ThreadPoolExecutor(2, 10, 10L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100));

    private static final Pool<Bitmap> POOL = FactoryPools.threadSafe(10, new FactoryPools.Factory<Bitmap>() {
        @Override
        public Bitmap create() {
            return BitmapFactory.decodeFile(PATH);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void acquire(View view) {
        executorService.execute(new RequestTask(num++));
    }

    private class RequestTask implements Runnable {
        private int num;
        public RequestTask(int num) {
            this.num = num;
        }

        @Override
        public void run() {
            Bitmap bitmap = POOL.acquire();
            Log.d("lizy", "request " + num + " is running!");
            try {
                Thread.sleep(6 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            POOL.release(bitmap);
        }
    }
}
