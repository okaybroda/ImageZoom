package com.viven.imagezoom.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.viven.imagezoom.ImageZoomHelper;

public class MainActivity extends AppCompatActivity {

    ImageZoomHelper imageZoomHelper;

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

        imageZoomHelper = new ImageZoomHelper(this);

        // set zoomable tag on views that is to be zoomed
        ImageZoomHelper.setViewZoomable(findViewById(R.id.imgLogo));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }
}
