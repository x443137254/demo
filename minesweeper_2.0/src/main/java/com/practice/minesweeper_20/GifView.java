package com.practice.minesweeper_20;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;


public class GifView extends View {

    private final String TAG = "debug";

    private long mStartTime;
    private int mEnd;
    private Movie mMovie;
    private boolean mInit;
    private int mSize;
    private int newSize;

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GifView(Context context) {
        super(context);
        init();
    }

    private void init(){
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        int resourceId = getResources().getIdentifier("explode", "drawable", getContext().getPackageName());
        mMovie = Movie.decodeStream(getResources().openRawResource(resourceId));
        mSize = mMovie.width();
        mInit = true;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mInit) return;
        int time = (int) ((System.currentTimeMillis() - mStartTime) % mMovie.duration());
        if (mEnd > time) {
            MineSeeperActivity.mHandler.sendEmptyMessage(3);
            return;
        }
        mEnd = time;
        mMovie.setTime(time);
        mMovie.draw(canvas, 0, 0);
        invalidate();
    }

    public void show(int x, int y){
        mStartTime = System.currentTimeMillis();
        mEnd = 0;
        int offset = (mSize - newSize) / 2;
        layout(x - offset, y - offset, mSize + x, mSize + y);
        mInit = false;
        invalidate();
    }

    public void setSize(int size){
        newSize = size;
        float scale = size * 1.0f / mSize;
        setScaleX(scale);
        setScaleY(scale);
    }

    public void reset(){
        mInit = true;
    }
    public int getSize() {
        return mSize;
    }

}
