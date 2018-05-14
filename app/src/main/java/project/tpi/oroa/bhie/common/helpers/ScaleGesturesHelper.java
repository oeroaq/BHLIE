package project.tpi.oroa.bhie.common.helpers;


import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import project.tpi.oroa.bhie.common.global.GlobalClass;

public class ScaleGesturesHelper implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private View view;
    private ScaleGestureDetector gestureScale;
    private float scaleFactor = 1;
    private boolean inScale;
    private static final String TAG = "MyScaleGestures";

    public ScaleGesturesHelper(Context c) {
        gestureScale = new ScaleGestureDetector(c, this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.view = view;
        gestureScale.onTouchEvent(event);
        return true;
    }

    public boolean SetView(View view){
        this.view = view;
        return true;
    }
    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        scaleFactor *= detector.getScaleFactor();

        scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor);
        scaleFactor = ((float) ((int) (scaleFactor * 100))) / 100;
        GlobalClass.scaleFactor = scaleFactor;
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        inScale = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        inScale = false;
    }
}
