package com.viven.imagezoom.sample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.viven.imagezoom.ImageZoomHelper;

public class MainActivity extends AppCompatActivity {

    ImageZoomHelper imageZoomHelper;
    ImageView imgLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgLogo = (ImageView) findViewById(R.id.imgLogo);

        imageZoomHelper = new ImageZoomHelper(this);

        // set zoomable tag on views that is to be zoomed
        ImageZoomHelper.setViewZoomable(imgLogo);

        Glide.with(this)
                .load(R.drawable.bigimage)
                .into(imgLogo);

        imageZoomHelper.addOnZoomListener(new ImageZoomHelper.OnZoomListener() {
            @Override
            public void onImageZoomStarted(final View view) {

            }

            @Override
            public void onImageZoomEnded(View view) {

            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }
}
