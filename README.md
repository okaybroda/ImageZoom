# ImageZoom
An Android library that makes any view to be zoomable.
![View Preview](https://github.com/okaybroda/ImageZoom/blob/master/preview.gif?raw=true)

It was created to mimick the Instagram Zoom feature.

## Installation
Add Jitpack
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then add VRCategoryView
```gradle
dependencies {
  compile 'com.github.okaybroda:ImageZoom:1.0.0'
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
One last thing, set the R.id.zoomable tag to the Views that you would like to be zoomable.
```java
findViewById(R.id.imgLogo).setTag(R.id.zoomable, new Object());
```
