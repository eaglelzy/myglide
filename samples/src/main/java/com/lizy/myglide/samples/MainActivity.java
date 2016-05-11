package com.lizy.myglide.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.lizy.myglide.Glide;
import com.lizy.myglide.load.engine.DiskCacheStrategy;
import com.lizy.myglide.request.RequestOptions;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView1;
    private ImageView imageView2;

    private static final String TEST_URL = "http://img5.duitang.com/uploads/item/201509/25/20150925155647_rhAxE.jpeg";

    private static final String LOCAL_FILE = "/sdcard/Pictures/sky.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView)findViewById(R.id.img_test1);
        imageView2 = (ImageView)findViewById(R.id.img_test2);

        Glide.with(this).load(TEST_URL)
                .apply(RequestOptions.circleCropTransform(this)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(imageView1);
    }

    public void test(View v) {

    }
}
