package com.viven.imagezoom;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

/**
 * Created by viventhraarao on 25/11/2016.
 */

public class ImageZoomHelper {
    private View zoomableView = null;
    private ViewGroup parentOfZoomableView;
    private ViewGroup.LayoutParams zoomableViewLP;
    private FrameLayout.LayoutParams zoomableViewFrameLP;
    private Dialog dialog;
    private View placeholderView;
    private int viewIndex;
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

                    // get view's original location relative to the window
                    originalXY = new int[2];
                    view.getLocationInWindow(originalXY);

                    // this FrameLayout will be the zoomableView's temporary parent
                    FrameLayout frameLayout = new FrameLayout(view.getContext());

                    // this view is to gradually darken the backdrop as user zooms
                    darkView = new View(view.getContext());
                    darkView.setBackgroundColor(Color.BLACK);
                    darkView.setAlpha(0f);

                    // adding darkening backdrop to the frameLayout
                    frameLayout.addView(darkView, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));

                    // the Dialog that will hold the FrameLayout
                    dialog = new Dialog(activity,
                            android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                    dialog.addContentView(frameLayout,
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
                    dialog.show();

                    // get the parent of the zoomable view and get it's index and layout param
                    parentOfZoomableView = (ViewGroup) zoomableView.getParent();
                    viewIndex = parentOfZoomableView.indexOfChild(zoomableView);
                    this.zoomableViewLP = zoomableView.getLayoutParams();

                    // this is the new layout param for the zoomableView
                    zoomableViewFrameLP = new FrameLayout.LayoutParams(
                            view.getWidth(), view.getHeight());
                    zoomableViewFrameLP.leftMargin = originalXY[0];
                    zoomableViewFrameLP.topMargin = originalXY[1];

                    // this view will hold the zoomableView's position temporarily
                    placeholderView = new View(activity);

                    // setting placeholderView's background to zoomableView's drawingCache
                    // this avoids flickering when adding/removing views
                    zoomableView.setDrawingCacheEnabled(true);
                    placeholderView.setBackgroundDrawable(
                            new BitmapDrawable(activity.getResources(),
                                    Bitmap.createBitmap(zoomableView.getDrawingCache())));

                    // placeholderView takes the place of zoomableView temporarily
                    parentOfZoomableView.addView(placeholderView, zoomableViewLP.width,
                            zoomableViewLP.height);

                    // zoomableView has to be removed from parent view before being added to it's
                    // new parent
                    parentOfZoomableView.removeView(zoomableView);
                    frameLayout.addView(zoomableView, zoomableViewFrameLP);

                    // using a post to remove placeholder's drawing cache
                    zoomableView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null) {
                                placeholderView.setBackgroundDrawable(null);
                                zoomableView.setDrawingCacheEnabled(false);
                            }
                        }
                    });

                    // Pointer variables to store the original touch positions
                    MotionEvent.PointerCoords pointerCoords1 = new MotionEvent.PointerCoords();
                    ev.getPointerCoords(0, pointerCoords1);

                    MotionEvent.PointerCoords pointerCoords2 = new MotionEvent.PointerCoords();
                    ev.getPointerCoords(1, pointerCoords2);

                    // storing distance between the two positions to be compared later on for
                    // zooming
                    originalDistance = (int) getDistance(pointerCoords1.x, pointerCoords2.x,
                            pointerCoords1.y, pointerCoords2.y);

                    // storing center point of the two pointers to move the view according to the
                    // touch position
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

                zoomableView.setScaleX((float) (1 + pctIncrease));
                zoomableView.setScaleY((float) (1 + pctIncrease));

                updateZoomableViewMargins(newCenter[0] - twoPointCenter[0] + originalXY[0],
                        newCenter[1] - twoPointCenter[1] + originalXY[1]);

                darkView.setAlpha((float) (pctIncrease / 8));

                return true;
            }
        } else {
            if (zoomableView != null && !isAnimatingDismiss) {
                isAnimatingDismiss = true;
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
                valueAnimator.setDuration(activity.getResources()
                        .getInteger(android.R.integer.config_shortAnimTime));
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    float scaleYStart = zoomableView.getScaleY();
                    float scaleXStart = zoomableView.getScaleX();
                    int leftMarginStart = zoomableViewFrameLP.leftMargin;
                    int topMarginStart = zoomableViewFrameLP.topMargin;
                    float alphaStart = darkView.getAlpha();

                    float scaleYEnd = 1f;
                    float scaleXEnd = 1f;
                    int leftMarginEnd = originalXY[0];
                    int topMarginEnd = originalXY[1];
                    float alphaEnd = 0f;
                    
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedFraction = valueAnimator.getAnimatedFraction();
                        if (animatedFraction < 1) {
                            zoomableView.setScaleX(((scaleXEnd - scaleXStart) * animatedFraction) +
                                    scaleXStart);
                            zoomableView.setScaleY(((scaleYEnd - scaleYStart) * animatedFraction) +
                                    scaleYStart);

                            updateZoomableViewMargins(
                                    ((leftMarginEnd - leftMarginStart) * animatedFraction) +
                                            leftMarginStart,
                                    ((topMarginEnd - topMarginStart) * animatedFraction) +
                                            topMarginStart);

                            darkView.setAlpha(((alphaEnd - alphaStart) * animatedFraction) +
                                    alphaStart);
                        } else {
                            dismissDialogAndViews();
                        }
                    }
                });
                valueAnimator.start();

                return true;
            }
        }

        return false;
    }

    void updateZoomableViewMargins(float left, float top) {
        if (zoomableView != null && zoomableViewFrameLP != null) {
            zoomableViewFrameLP.leftMargin = (int) left;
            zoomableViewFrameLP.topMargin = (int) top;
            zoomableView.setLayoutParams(zoomableViewFrameLP);
        }
    }

    /**
     * Dismiss dialog and set views to null for garbage collection
     */
    private void dismissDialogAndViews() {
        if (zoomableView != null) {
            zoomableView.setVisibility(View.VISIBLE);

            ViewGroup parent = (ViewGroup) zoomableView.getParent();
            parent.removeView(zoomableView);
            this.parentOfZoomableView.addView(zoomableView, viewIndex, zoomableViewLP);
            this.parentOfZoomableView.removeView(placeholderView);

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

                if (child.getTag(R.id.unzoomable) == null) {
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
        }

        return null;
    }

    /**
     * Set view to be zoomable
     *
     * @param view
     */
    public static void setViewZoomable(View view) {
        view.setTag(R.id.zoomable, new Object());
    }

    /**
     * Enable or disable zoom for view and it's children
     *
     * @param view
     * @param enabled
     */
    public static void setZoom(View view, boolean enabled) {
        view.setTag(R.id.unzoomable, enabled ? null : new Object());
    }
}
