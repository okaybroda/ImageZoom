package com.viven.imagezoom;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.lang.ref.WeakReference;

/**
 * Created by viventhraarao on 25/11/2016.
 */

public class ImageZoomHelper {
    private View zoomableView = null;
    private Dialog dialog;
    private ImageView imageView;
    private View darkView;
    private double originalDistance;
    private int[] twoPointCenter;
    private int[] originalXY;

    private WeakReference<Activity> activityWeakReference;

    private boolean isAnimatingDismiss = false;

    public ImageZoomHelper(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    public boolean onDispatchTouchEvent(MotionEvent ev) {
        Activity activity;
        if ((activity = activityWeakReference.get()) == null)
            return false;

        if (ev.getPointerCount() == 2) {
            if (zoomableView == null) {
                View view = findZoomableView(ev,
                        activity.findViewById(android.R.id.content));
                if (view != null) {
                    zoomableView = view;

                    FrameLayout frameLayout = new FrameLayout(view.getContext());
                    darkView = new View(view.getContext());
                    darkView.setBackgroundColor(Color.BLACK);
                    darkView.setAlpha(0f);
                    frameLayout.addView(darkView, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));

                    imageView = new ImageView(view.getContext());
                    view.setDrawingCacheEnabled(true);
                    view.buildDrawingCache();
                    imageView.setImageBitmap(view.getDrawingCache());
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            view.getWidth(), view.getHeight());
                    frameLayout.addView(imageView, layoutParams);

                    originalXY = new int[2];
                    view.getLocationInWindow(originalXY);

                    imageView.setX(originalXY[0]);
                    imageView.setY(originalXY[1]);

                    imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View view, int i, int i1, int i2, int i3,
                                                   int i4, int i5, int i6, int i7) {
                            zoomableView.setVisibility(View.INVISIBLE);
                            imageView.removeOnLayoutChangeListener(this);
                        }
                    });

                    dialog = new Dialog(activity,
                            android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                    dialog.addContentView(frameLayout,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
                    dialog.show();

                    MotionEvent.PointerCoords pointerCoords1 = new MotionEvent.PointerCoords();
                    ev.getPointerCoords(0, pointerCoords1);

                    MotionEvent.PointerCoords pointerCoords2 = new MotionEvent.PointerCoords();
                    ev.getPointerCoords(1, pointerCoords2);

                    originalDistance = (int) getDistance(pointerCoords1.x, pointerCoords2.x,
                            pointerCoords1.y, pointerCoords2.y);
                    twoPointCenter = new int[] {
                            (int) ((pointerCoords2.x + pointerCoords1.x) / 2),
                            (int) ((pointerCoords2.y + pointerCoords1.y) / 2)
                    };

                    return true;
                }
            } else {
                MotionEvent.PointerCoords pointerCoords1 = new MotionEvent.PointerCoords();
                ev.getPointerCoords(0, pointerCoords1);

                MotionEvent.PointerCoords pointerCoords2 = new MotionEvent.PointerCoords();
                ev.getPointerCoords(1, pointerCoords2);

                int[] newCenter = new int[] {
                        (int) ((pointerCoords2.x + pointerCoords1.x) / 2),
                        (int) ((pointerCoords2.y + pointerCoords1.y) / 2)
                };

                int currentDistance = (int) getDistance(pointerCoords1.x, pointerCoords2.x,
                        pointerCoords1.y, pointerCoords2.y);
                double pctIncrease = (currentDistance - originalDistance) / originalDistance;

                imageView.setScaleX((float) (1 + pctIncrease));
                imageView.setScaleY((float) (1 + pctIncrease));

                imageView.setX(newCenter[0] - twoPointCenter[0] + originalXY[0]);//+ originalXY[0]);
                imageView.setY(newCenter[1] - twoPointCenter[1] + originalXY[1]);//+ originalXY[1]);

                darkView.setAlpha((float) (pctIncrease / 8));

                return true;
            }
        } else {
            if (zoomableView != null && !isAnimatingDismiss) {
                isAnimatingDismiss = true;
                imageView.animate().scaleY(1).scaleX(1).x(originalXY[0]).y(originalXY[1])
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                dismissDialogAndViews();
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {
                                dismissDialogAndViews();
                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        }).start();
                darkView.animate().alpha(0).start();

                return true;
            }
        }

        return false;
    }

    /**
     * Dismiss dialog and set views to null for garbage collection
     */
    private void dismissDialogAndViews() {
        if (zoomableView != null) {
            zoomableView.setVisibility(View.VISIBLE);
            zoomableView.post(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                }
            });
            zoomableView = null;
        } else {
            dismissDialog();
        }

        isAnimatingDismiss = false;
    }

    private void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        zoomableView = null;
        imageView = null;
        darkView = null;
    }

    /**
     * Get distance between two points
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return distance
     */
    private double getDistance(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Finds the view that has the R.id.zoomable tag and also contains the x and y coordinations
     * of two pointers
     *
     * @param event MotionEvent that contains two pointers
     * @param view View to find in
     * @return zoomable View
     */
    private View findZoomableView(MotionEvent event, View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();

            MotionEvent.PointerCoords pointerCoords1 = new MotionEvent.PointerCoords();
            event.getPointerCoords(0, pointerCoords1);

            MotionEvent.PointerCoords pointerCoords2 = new MotionEvent.PointerCoords();
            event.getPointerCoords(1, pointerCoords2);

            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                Rect visibleRect = new Rect();
                int location[] = new int[2];
                child.getLocationOnScreen(location);
                visibleRect.left = location[0];
                visibleRect.top = location[1];
                visibleRect.right = visibleRect.left + child.getWidth();
                visibleRect.bottom = visibleRect.top + child.getHeight();

                if (visibleRect.contains((int) pointerCoords1.x, (int) pointerCoords1.y) &&
                        visibleRect.contains((int) pointerCoords2.x, (int) pointerCoords2.y)) {
                    if (child.getTag(R.id.zoomable) != null)
                        return child;
                    else
                        return findZoomableView(event, child);
                }
            }
        }

        return null;
    }

    public static void setViewZoomable(View view) {
        view.setTag(R.id.zoomable, new Object());
    }
}
