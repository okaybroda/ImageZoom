package com.viven.imagezoom.sample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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

        findViewById(R.id.btnDialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View image = findViewById(R.id.imgLogo);

                final FrameLayout frameLayout = new FrameLayout(MainActivity.this);

                int[] originalXY = new int[2];
                image.getLocationInWindow(originalXY);

                final ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                final ViewGroup parent = (ViewGroup) image.getParent();
                parent.removeView(image);
                frameLayout.addView(image);

                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) image.getLayoutParams();
                marginLayoutParams.leftMargin = originalXY[0];
                marginLayoutParams.topMargin = originalXY[1];
//                image.setX(originalXY[0]);
//                image.setY(originalXY[1]);

                Dialog dialog = new Dialog(MainActivity.this,
                        android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                dialog.addContentView(frameLayout,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                dialog.show();

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        frameLayout.removeView(image);
                        parent.addView(image, layoutParams);
                    }
                });
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }
}
