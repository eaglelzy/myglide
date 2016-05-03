package com.lizy.myglide.samples;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.request.target.ImageViewTargetFactory;
import com.lizy.myglide.request.target.Target;
import com.lizy.myglide.request.transtion.Transition;
import com.lizy.myglide.request.transtion.ViewAnimationFactory;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.img_test);
        drawables[0] = getResources().getDrawable(R.drawable.test_1_200);
        drawables[1] = getResources().getDrawable(R.drawable.test_2_200);
        drawables[2] = getResources().getDrawable(R.drawable.test_3_200);


        adapter = new Transition.ViewAdapter() {
            private Drawable drawable;
            @Override
            public View getView() {
                return imageView;
            }

            @Nullable
            @Override
            public Drawable getCurrentDrawable() {
                return drawable;
            }

            @Override
            public void setDrawable(@Nullable Drawable drawable) {
                this.drawable = drawable;
            }
        };

        transition= new ViewAnimationFactory<Drawable>(android.R.anim.fade_in)
                .build(DataSource.REMOTE, true);

        target = new ImageViewTargetFactory().buildTarget(imageView, Drawable.class);
    }

    private int currentIndex = 0;

    private Drawable[] drawables = new Drawable[3];

    private Transition.ViewAdapter adapter;

    private Transition<Drawable> transition;

    Target<Drawable> target;

    public void test(View v) {
        target.onResourceReady(drawables[currentIndex], transition);

        currentIndex++;
        currentIndex = currentIndex % 3;

/*        Resources resources = getResources();
        BitmapPool bitmapPool = new LruBitmapPool(50 * 1024 * 1024);
        ArrayPool arrayPool = new LruArrayPool(8 * 1024 * 1024);
        Downsampler downsampler = new Downsampler(resources.getDisplayMetrics(), bitmapPool, arrayPool);

        final Registry registry = new Registry(this);
        registry.append(String.class, InputStream.class, new StringLoader.StreamFactory());
        registry.append(Uri.class, InputStream.class, new HttpUriLoader.Factory());
        registry.append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory());

        registry.append(InputStream.class, Bitmap.class, new StreamBitmapDecoder(downsampler));
        registry.append(InputStream.class, BitmapDrawable.class,
                new BitmapDrawableDecoder<>(resources, bitmapPool,
                        new StreamBitmapDecoder(downsampler)));

        registry.register(Bitmap.class, BitmapDrawable.class,
                new BitmapDrawableTanscoder(resources, bitmapPool));

        String url = "http://www.google.com";
        List<ModelLoader<String, ?>> loaders = registry.getModelLoaders(url);

        List<ModelLoader.LoadData<?>> loadDatas = new ArrayList<>();
        for (ModelLoader<String, ?> loader : loaders) {
            if (loader.handles(url)) {
                ModelLoader.LoadData<?> loadData = loader.buildLoadData(url, 100, 100, new Options());
                loadDatas.add(loadData);
            }
        }
        for (final ModelLoader.LoadData<?> loadData : loadDatas) {
            loadData.fetcher.loadData(RenderScript.Priority.LOW, new DataFetcher.DataCallback<Object>() {
                @Override
                public void onDataReady(@Nullable Object o) {
                    LoadPath loadPath = registry.getLoadPath(o.getClass(), Object.class, BitmapDrawable.class);
                    try {
                        Resource<BitmapDrawable> bitmapDrawableResource = loadPath.load(
                                o, new Options(), 100, 100, new DecodePath.DecodeCallback() {
                            @Override
                            public Resource onResourceDecoded(Resource resource) {
                                return resource;
                            }
                        });
                        imageView.setImageDrawable(bitmapDrawableResource.get());
                    } catch (GlideException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoadFailed(Exception e) {
                }
            });
        }*/
    }
}
