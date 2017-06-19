# ImageZoom
An Android library that makes any view to be zoomable.
It was created to mimick the Instagram Zoom feature.

![View Preview](https://github.com/okaybroda/ImageZoom/blob/master/preview.gif?raw=true)

## Installation
Add Jitpack
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then add ImageZoom library
```gradle
dependencies {
  compile 'com.github.okaybroda:ImageZoom:1.1.0'
}
```
## Usage
Create an ImageZoomHelper instance in the OnCreate function of your Activity
```java
ImageZoomHelper imageZoomHelper;

@Override
protected void onCreate(Bundle savedInstanceState) {
    // ... your code ...
    imageZoomHelper = new ImageZoomHelper(this);
}
```
Override dispatchTouchEvent in your Activity and pass all touch events to the ImageZoomHelper instance:
```java
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
    return imageZoomHelper.onDispatchTouchEvent(ev) || super.dispatchTouchEvent(ev);
}
```
Set the R.id.zoomable tag to the Views that you would like to be zoomable.
```java
ImageZoomHelper.setViewZoomable(findViewById(R.id.imgLogo));
```
To enable/disable zoom for certain Views (e.g. Recycler View refreshing)
```java
ImageZoomHelper.setZoom(recyclerView, false)
```
### Advanced Usage
For a smoother zoom transition, set the layout to be fullscreen. This only works on API 16 and above.

Place this code in the OnCreate function of your Activity. Preferably before the setContentView line.
```java
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
    View decorView = getWindow().getDecorView();
    // Hide the status bar.
    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    decorView.setSystemUiVisibility(uiOptions);
}
```

The above code makes your Activity layout go behind the status bar which brings the status bar on top of the layout. To fix that, put this line in your root layout XML.
```xml
android:fitsSystemWindows="true"
```
## Known Issues
### RecyclerView
When using RecyclerView and setting it's child to be zoomable, RecyclerView crashes.
```java
ImageView imageView = new ImageView(RecyclerViewActivity.this);
imageView.setImageResource(R.mipmap.ic_launcher);
ImageZoomHelper.setViewZoomable(imageView);
return new RecyclerView.ViewHolder(frameLayout) {};
```

Workaround is to wrap the zoomable View with a parent ViewGroup.
```java
// Wrap ImageView with FrameLayout to avoid RecyclerView issue
FrameLayout frameLayout = new FrameLayout(parent.getContext());
frameLayout.addView(imageView);
return new RecyclerView.ViewHolder(frameLayout) {};
```
