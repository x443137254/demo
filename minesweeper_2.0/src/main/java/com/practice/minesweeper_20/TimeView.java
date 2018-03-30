package com.practice.minesweeper_20;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class TimeView extends View implements View.OnTouchListener {


    private Bitmap[] mNumPart1;
    private Bitmap[] mNumPart2;
    private Bitmap mSeparator;

    private String TAG = "debug";

    private int mWidth = 200;
    private int mHeight = 70;
    private int mSeparatorWidth = (int) (mWidth / 30);
    private int mSeparatorHeight = (int) (mHeight / 2);
    private int mNumWidth = (int) ((mWidth - mSeparatorWidth) / 4);
    private int mNumHeight = (int) (mHeight / 2);
    private int mMargin = (int) (mWidth * 0.3);

    private Matrix mMatrix;
    private Paint mPaint;
    private int mCount = 0;     //用于显示动画帧数的计数器
    private int mSecond = 0;      //用于统计时间的计数器
    private int mTime = 0;
    private long mStartTime;
    private long mStopTime;
    private Boolean mState;
    private Boolean mReset;
    public final Boolean STATE_START = true;
    public final Boolean STATE_STOP = false;

    public TimeView(Context context) {
        super(context);
        init();
    }

    public TimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public int getmWidth() {
        return mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    private void init() {
        int[] bitmapNums1 = {
                R.drawable.timenum0part1,
                R.drawable.timenum1part1,
                R.drawable.timenum2part1,
                R.drawable.timenum3part1,
                R.drawable.timenum4part1,
                R.drawable.timenum5part1,
                R.drawable.timenum6part1,
                R.drawable.timenum7part1,
                R.drawable.timenum8part1,
                R.drawable.timenum9part1
        };
        int[] bitmapNums2 = {
                R.drawable.timenum0part2,
                R.drawable.timenum1part2,
                R.drawable.timenum2part2,
                R.drawable.timenum3part2,
                R.drawable.timenum4part2,
                R.drawable.timenum5part2,
                R.drawable.timenum6part2,
                R.drawable.timenum7part2,
                R.drawable.timenum8part2,
                R.drawable.timenum9part2
        };
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.separator);
        mSeparator = Bitmap.createScaledBitmap(bitmap, mSeparatorWidth, mSeparatorHeight, false);
        mNumPart1 = new Bitmap[10];
        mNumPart2 = new Bitmap[10];
        for (int i = 0; i < 10; i++) {
            bitmap = BitmapFactory.decodeResource(getResources(), bitmapNums1[i]);
            mNumPart1[i] = Bitmap.createScaledBitmap(bitmap, mNumWidth, mNumHeight, false);
            bitmap = BitmapFactory.decodeResource(getResources(), bitmapNums2[i]);
            mNumPart2[i] = Bitmap.createScaledBitmap(bitmap, mNumWidth, mNumHeight, false);
        }
        setOnTouchListener(this);
        mMatrix = new Matrix();
        mPaint = new Paint();
        mState = STATE_STOP;
        mReset = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth + mMargin * 2, mHeight + mMargin);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(mSeparator, mNumWidth * 2 + mMargin, mHeight / 4, mPaint);
        if (mReset) {
            drawNum(canvas, mPaint, 0);
            mReset = false;
            return;
        }
        mTime = mSecond % 60 + mSecond / 60 * 100;
        drawNum(canvas, mPaint, mTime);
        if (mState == STATE_START || mCount != 0) {
            mCount++;
            if (mCount > 20) {
                mCount = 0;
                return;
            }
            skipNextNum(canvas, mPaint, mTime % 10, 4);
            if (mTime % 10 == 0) skipNextNum(canvas, mPaint, mTime % 100 / 10, 3);
            if (mTime % 100 == 0) skipNextNum(canvas, mPaint, mTime / 100 % 10, 2);
            if (mTime % 600 == 0) skipNextNum(canvas, mPaint, mTime % 10000, 1);
            postInvalidateDelayed(20);
        }
    }

    private void skipNextNum(Canvas canvas, Paint paint, int newNum, int position) {

        int oldNum;
        if (position == 3) oldNum = newNum == 0 ? 5 : newNum - 1;
        else oldNum = newNum == 0 ? 9 : newNum - 1;

        canvas.drawBitmap(mNumPart1[newNum], getPositionX(position), 0, paint);

        if (mCount < 20)
            canvas.drawBitmap(mNumPart2[oldNum], getPositionX(position), mNumHeight, paint);
        else canvas.drawBitmap(mNumPart2[newNum], getPositionX(position), mNumHeight, paint);

        if (mCount < 10) {
            mMatrix.setSkew(-0.03f * mCount, 0, 0, mNumHeight);
            mMatrix.postScale(1, 1 - mCount / 10f, 0, mNumHeight);
            mMatrix.postTranslate(getPositionX(position), 0);
            canvas.drawBitmap(mNumPart1[oldNum], mMatrix, paint);

        } else if (mCount <= 20) {
            int c = mCount - 10;
            mMatrix.setScale(1, c / 10f, 0, 0);
            mMatrix.postTranslate(getPositionX(position), mNumHeight);
            mMatrix.postSkew(0.03f * (10 - c), 0, 0, mNumHeight);
            canvas.drawBitmap(mNumPart2[newNum], mMatrix, paint);
        }

    }

    public int getSeconds() {
        return mSecond;
    }

    public int getText() {
        return mTime;
    }

    public long getTime(){
        return mStopTime - mStartTime;
    }

    private void drawNum(Canvas canvas, Paint paint, int num) {

        int a1 = num / 1000;
        int a2 = (num - a1 * 1000) / 100;
        int a3 = (num - a1 * 1000 - a2 * 100) / 10;
        int a4 = num - a1 * 1000 - a2 * 100 - a3 * 10;
        int[] a = {a1, a2, a3, a4};

        for (int i = 1; i <= 4; i++) {
            canvas.drawBitmap(mNumPart1[a[i - 1]], getPositionX(i), 0, paint);
            canvas.drawBitmap(mNumPart2[a[i - 1]], getPositionX(i), mNumHeight, paint);
        }

    }

    private int getPositionX(int a) {
        if (a == 1) return mMargin;
        else if (a == 2) return mNumWidth + mMargin;
        else if (a == 3) return mNumWidth * 2 + mSeparatorWidth + mMargin;
        else return mNumWidth * 3 + mSeparatorWidth + mMargin;
    }

    public void start() {
        mState = STATE_START;
        mStartTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(1000);
                    if(mState == STATE_STOP) break;
                    MineSeeperActivity.mHandler.sendEmptyMessage(1);
                    mSecond++;
                }
            }
        }).start();
    }

    public void resst(){
        mSecond = 0;
        mCount = 0;
        mTime = 0;
        mReset = true;
        mState = STATE_STOP;
        invalidate();
    }

    public void stop() {
        mState = STATE_STOP;
        mStopTime = System.currentTimeMillis();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

}
