package com.lizy.myglide.samples;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.lizy.myglide.Glide;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    private static final String TEST_URL = "http://img5.duitang.com/uploads/item/201509/25/20150925155647_rhAxE.jpeg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.img_test);

        Glide.with(this).load(TEST_URL)
                .into(imageView);
    }

    public void test(View v) {

    }
}
