package com.practice.minesweeper_20;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Administrator on 2018/3/7.
 */
public class SelectView extends View implements View.OnTouchListener {

//    private int mStatuBarHeight;
    private Bitmap[] mBitmap;

    private Bitmap mBitmapLeft;
    private Bitmap mBitmapRight;
    private Bitmap mBitmapRestart;
    private Bitmap mBitmapSetting;

    private CoreView mCoreView;
    private int mSelection;
    public boolean mGameOver;
    public boolean mExplodeTime = false;
    public final int CLICK = 0;
    public final int FLAG = 1;
    public final int RES_MSG = 0;
    public final int LEF_MSG = 1;
    public final int RIG_MSG = 2;
    public final int SET_MSG = 3;

    private int mButtonWidth = 200;
    private int mButtonheight = 200;
    private int mRButtonWidth = 100;
    private int mRButtonheight = 100;

    private int mWindowWidth;
    private int mWindowHeight;

    private String TAG = "debug";

    private int mBitmap1x = 0;  //左边图片x坐标
    private int mBitmap1y = 0;  //左边图片y坐标
    private int mBitmap2x = 0;  //右边图片x坐标
    private int mBitmap2y = 0;  //右边图片y坐标

    public SelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectView(Context context, CoreView cv) {
        super(context);
        mCoreView = cv;
        init();
    }

    public int getSelection() {
        return mSelection;
    }

    public void setSelection(int selection) {
        this.mSelection = selection;
    }

    private void init() {

        mGameOver = false;
        int[] bitmapResources = {
                R.drawable.click_button,
                R.drawable.flag_button_unselect,
                R.drawable.click_button_unselect,
                R.drawable.flag_button,
                R.drawable.restart,
                R.drawable.setting
        };
        Bitmap bitmap;
        mBitmap = new Bitmap[bitmapResources.length];
        for (int i = 0; i < 4; i++) {
            bitmap = BitmapFactory.decodeResource(getResources(), bitmapResources[i]);
            mBitmap[i] = Bitmap.createScaledBitmap(bitmap, mButtonWidth, mButtonheight, false);
        }

        bitmap = BitmapFactory.decodeResource(getResources(), bitmapResources[4]);
        mBitmapRestart = Bitmap.createScaledBitmap(bitmap, mRButtonWidth, mRButtonheight, false);
        bitmap = BitmapFactory.decodeResource(getResources(), bitmapResources[5]);
        mBitmapSetting = Bitmap.createScaledBitmap(bitmap, mRButtonWidth, mRButtonheight, false);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        mWindowWidth = display.getWidth();
        mWindowHeight = display.getHeight();

        mBitmap2x = mWindowWidth - mButtonWidth;
        mBitmap1y = mWindowHeight - mButtonheight;
        mBitmap2y = mBitmap1y;
        mSelection = CLICK;
        setOnTouchListener(this);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        if (mSelection == CLICK) {
            mBitmapLeft = mBitmap[0];
            mBitmapRight = mBitmap[1];
        } else {
            mBitmapLeft = mBitmap[2];
            mBitmapRight = mBitmap[3];
        }

        canvas.drawBitmap(mBitmapRestart, 10, 10, paint);
        canvas.drawBitmap(mBitmapSetting, mWindowWidth - mRButtonWidth - 10, 10, paint);
        canvas.drawBitmap(mBitmapLeft, mBitmap1x, mBitmap1y, paint);
        canvas.drawBitmap(mBitmapRight, mBitmap2x, mBitmap2y, paint);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN || mExplodeTime) return false;
        float x = event.getRawX();
        float y = event.getRawY();

        if (x < (mRButtonWidth + 10) && y < (mRButtonheight + 10)) {
            sendMessage(RES_MSG);
            return true;
        }
        if (x > (mWindowWidth - mRButtonWidth - 10) && y < mRButtonWidth + 10) {
            sendMessage(SET_MSG);
            return true;
        }

        if (mGameOver) return false;

        if (x < mButtonWidth && y > mBitmap1y) {
            if (mSelection == CLICK) return true;
            mSelection = CLICK;
            sendMessage(LEF_MSG);
            invalidate();
            return true;
        } else if (x > mBitmap2x && y > mBitmap2y) {
            if (mSelection == FLAG) return true;
            mSelection = FLAG;
            sendMessage(RIG_MSG);
            invalidate();
            return true;
        }
        return false;
    }

    public void setExplodeTime(boolean isExplode) {
        this.mExplodeTime = isExplode;
    }

    private void sendMessage(int arg1) {
        Message message = Message.obtain();
        message.what = 5;
        message.arg1 = arg1;
        MineSeeperActivity.mHandler.sendMessage(message);
    }
    public void reset(){
        mSelection = CLICK;
        invalidate();
    }
}
