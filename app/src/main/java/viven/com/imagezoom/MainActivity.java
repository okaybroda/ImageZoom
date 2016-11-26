package viven.com.imagezoom;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import viven.com.imagezoom.library.ImageZoomManager;

public class MainActivity extends AppCompatActivity {

    ImageZoomManager imageZoomManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageZoomManager = new ImageZoomManager(this);

        findViewById(R.id.imgLogo).setTag(R.id.zoomable, new Object());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomManager.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }
}
