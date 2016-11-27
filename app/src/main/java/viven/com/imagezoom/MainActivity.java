package viven.com.imagezoom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import viven.com.imagezoom.library.ImageZoomHelper;

public class MainActivity extends AppCompatActivity {

    ImageZoomHelper imageZoomHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageZoomHelper = new ImageZoomHelper(this);

        findViewById(R.id.imgLogo).setTag(R.id.zoomable, new Object());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }
}
