package com.practice.minesweeper_20;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CoreView extends View implements View.OnTouchListener {

    private int mWidthNum = 10;      //每行格子数量，预设50
    private int mHeightNum = 15;     //每列格子数量，预设50
    private int mSize = 80;          //每个格子大小，预设100dp
    private int mMimes = 25;
    private int mScaneNum;
    private Point mResumePoint;
    private boolean mResume = false;

    private boolean mMoveable;
    private float mPointx = 0;
    private float mPointy = 0;
    private float mFPointx = 0;
    private float mFPointy = 0;

    private int mState;
    private int mMode;
    private boolean mExplodeTime;

    public final int STATE_INIT = 0;
    public final int STATE_START = 1;
    public final int STATE_RUNNING = 2;
    public final int STATE_OVER_WIN = 3;
    public final int STATE_OVER_LOST = 4;

    public final int MODE_CLICK = 0;
    public final int MODE_FLAG = 1;

    public final int MESSAGE_START = 0;
    public final int MESSAGE_LOST = 1;
    public final int MESSAGE_WIN = 2;

    private long mFirstClickTime;   //记录刚触摸屏幕时系统时钟，与_x,_y一起作为判断是否为点击动作的依据。

    private int mWindowWidth;
    private int mWindowHeight;
    private GifView mGifView;
    private int[] bitmapResources = {
            R.drawable.blank,
            R.drawable.num1,
            R.drawable.num2,
            R.drawable.num3,
            R.drawable.num4,
            R.drawable.num5,
            R.drawable.num6,
            R.drawable.num7,
            R.drawable.num8,
            R.drawable.mime,
            R.drawable.button,
            R.drawable.set_tag,
            R.drawable.set_tag_x,
            R.drawable.mime_x,
            R.drawable.unwere
    };
    public Bitmap[] bitmaps;

    private final static String TAG = "debug";

    public List<UniteView> mUniteViews;

//    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            Message message = msg;
//            switch (message.what) {
//                case SINGLE_CLICK:
//                    Log.d(TAG, "handleMessage: " + message.what + " no.=" + message.arg1);
//                    turnOver(mUniteViews.get(message.arg1 - 1));
//                    break;
//                case DOUBLE_CLICK:
//                    Log.d(TAG, "handleMessage: " + message.what + " no.=" + message.arg1);
//                    mUniteViews.get(message.arg1 - 1).dbTurn();
//                    break;
//                case MULTI_CLICK:
//                    Log.d(TAG, "handleMessage: " + message.what + " no.=" + message.arg1);
//                    if (message.arg1 != 0) turnOver(mUniteViews.get(message.arg1 - 1));
//                    if (message.arg2 != 0) turnOver(mUniteViews.get(message.arg2 - 1));
//                    break;
//            }
//        }
//    };

    public CoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSource();
        initGame();
    }

    private void initSource() {
        setOnTouchListener(this);
        initBItmap();
        mUniteViews = new ArrayList<>();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        mWindowWidth = display.getWidth();
        mWindowHeight = display.getHeight();
    }


    public void initGame() {
        mState = STATE_INIT;
        mMode = MODE_CLICK;
        mScaneNum = 0;
        mExplodeTime = false;
        mUniteViews = new ArrayList<>();
        for (int i = 1; i <= mHeightNum; i++) {
            for (int j = 1; j <= mWidthNum; j++) {
                UniteView uv = new UniteView(this, j, i);
                mUniteViews.add(uv);
            }
        }

        for (UniteView uv : mUniteViews) {
            uv.findNeighbours();
        }
        invalidate();
        ;
    }

    private void initBItmap() {
        Bitmap bitmap;
        int length = bitmapResources.length;
        bitmaps = new Bitmap[length];
        for (int i = 0; i < length; i++) {
            bitmap = BitmapFactory.decodeResource(getResources(), bitmapResources[i]);
            bitmaps[i] = Bitmap.createScaledBitmap(bitmap, mSize, mSize, false);
        }
    }

    public int getWidthNum() {
        return mWidthNum;
    }

    public int getState() {
        return mState;
    }

    public void setWidthNum(int mWidthNum) {
        this.mWidthNum = mWidthNum;
    }

    public void setHeightNum(int mHeightNum) {
        this.mHeightNum = mHeightNum;
    }

    public void setMimes(int mimes) {
        this.mMimes = mimes;
    }

    public int getHeightNum() {
        return mHeightNum;
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public int getMode() {
        return mMode;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();

        drawLines(canvas, paint);
        drawPieces(canvas, paint);

        if (mState == STATE_INIT) {
            if (mWindowWidth > mWidthNum * mSize) {
                int left = (mWindowWidth - mWidthNum * mSize) / 2;
                layout(left, getTop(), left + mWidthNum * mSize, getTop() + mHeightNum * mSize);
            } else {
                layout(0, getTop(), mWidthNum * mSize, getTop() + mHeightNum * mSize);
            }
            if (mWindowHeight > mHeightNum * mSize) {
                int top = (mWindowHeight - mHeightNum * mSize) / 2;
                layout(getLeft(), top, getLeft() + mWidthNum * mSize, top + mHeightNum * mSize);
            } else {
                layout(getLeft(), 0, getLeft() + mWidthNum * mSize, 0 + mHeightNum * mSize);
            }
        }else if (mResume) {

            layout(mResumePoint.x, mResumePoint.y, mResumePoint.x + mWidthNum * mSize, mResumePoint.y + mHeightNum * mSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidthNum * mSize + 1, mHeightNum * mSize + 1);
    }

    private void drawPieces(Canvas canvas, Paint paint) {
        float x = getX();
        float y = getY();
        for (int i = 0; i < mHeightNum; i++) {
            float y_ = i * mSize + y;
            if (y_ + mSize >= 0 && y_ <= mWindowHeight)
            for (int j = 0; j < mWidthNum; j++) {
                float x_ = j * mSize + x;
                if (x_ + mSize >= 0 & x_ <= mWindowWidth)
                canvas.drawBitmap(mUniteViews.get(i * mWidthNum + j).getBitmap(), j * mSize, i * mSize, paint);
            }
        }
    }

    public void getGif(GifView gif) {
        mGifView = gif;
    }

    public void move(MotionEvent event) {

        float tempx = event.getRawX() - mPointx;
        float tempy = event.getRawY() - mPointy;

        int l = (int) (getLeft() + tempx);
        int t = (int) (getTop() + tempy);
        int r = mWidthNum * mSize + l + 1;
        int b = mHeightNum * mSize + t + 1;
        this.layout(l, t, r, b);
        invalidate();
        mPointx = event.getRawX();
        mPointy = event.getRawY();

    }

    private void drawLines(Canvas canvas, Paint paint) {
        float x = getX();
        float y = getY();
        for (int i = 0; i <= mHeightNum; i++) {
            float y_ = i * mSize + y;
            if (y_  + mSize>= 0 && y_ <= mWindowHeight)
            canvas.drawLine(0, i * mSize, mWidthNum * mSize, i * mSize, paint);
        }
        for (int i = 0; i <= mWidthNum; i++) {
            float x_ = i * mSize + x;
            if (x_ + mSize >= 0 & x_ <= mWindowWidth)
            canvas.drawLine(i * mSize, 0, i * mSize, mHeightNum * mSize, paint);
        }
    }

    public int getClickNum(float x, float y) {
        float clickX = x;
        float clickY = y;
        float l = getLeft();
        float t = getTop();
        float r = l + mWidthNum * mSize;
        float b = t + mHeightNum * mSize;
        if (x < l || x > r || y < t || y > b) return -1;
        else {
            int w = (int) ((clickX - l) / mSize + 1);
            int h = (int) ((clickY - t) / mSize + 1);
            return (h - 1) * mWidthNum + w;
        }
    }

    private void turnOver(UniteView uv) {
        if (mState == STATE_START) startGame(uv);
        if (mState == STATE_RUNNING) {
            int p = uv.click();
            if (p > 0) lost(p);
            else isWin();
            MineSeeperActivity.playClickMusic();
            invalidate();
        }
    }

    private void startGame(UniteView uv) {
        mState = STATE_RUNNING;
        sendMessage(MESSAGE_START);
        int[] mimes = getRamdomArray(mMimes, mWidthNum * mHeightNum, uv.mId);
        for (int i : mimes) {
            mUniteViews.get(i - 1).setMime(true);
        }
    }

    public Boolean isWin() {
        if (mScaneNum == mWidthNum * mHeightNum - mMimes) {
            win();

            return true;
        } else return false;
    }

    public void lost(int explodeLocation) {
        mState = STATE_OVER_LOST;
        sendMessage(MESSAGE_LOST);

        float x = getX() + (explodeLocation - 1) % mWidthNum * mSize;
        float y = getY() + (explodeLocation - 1) / mWidthNum * mSize;
        mGifView.show((int) x, (int) y);

        MineSeeperActivity.playLostMusic();

        for (UniteView uv : mUniteViews) {
            if (uv.mId == explodeLocation)
                uv.setBitmap(bitmaps[13]);
            else if (!uv.isMime() && uv.mState == uv.STATE_FLAGED)
                uv.setBitmap(bitmaps[12]);
            else if (uv.isMime() && (uv.mState == uv.STATE_INIT || uv.mState == uv.STATE_UNWERE))
                uv.setBitmap(bitmaps[9]);
        }
    }

    public void win() {
        mState = STATE_OVER_WIN;
        sendMessage(MESSAGE_WIN);
        MineSeeperActivity.playWinMusic();
        for (UniteView uv : mUniteViews) {
            if (uv.isMime()) uv.setBitmap(bitmaps[11]);
        }
    }

    private void sendMessage(int what) {
        Message message = Message.obtain();
        message.what = 2;
        if (what == MESSAGE_START) {
            message.arg1 = MESSAGE_START;
        } else if (what == MESSAGE_WIN) {
            message.arg1 = MESSAGE_WIN;
        } else {
            message.arg1 = MESSAGE_LOST;
        }
        MineSeeperActivity.mHandler.sendMessage(message);
    }

    public void setExplodeTime(Boolean isExplode) {
        this.mExplodeTime = isExplode;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mExplodeTime) return false;
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mResume = false;
                if (!mMoveable) mMoveable = true;
                if (mState == STATE_INIT) mState = STATE_START;
                mPointx = event.getRawX();
                mPointy = event.getRawY();
                mFPointx = mPointx;
                mFPointy = mPointy;
                mFirstClickTime = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mMoveable = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMoveable) move(event);
                break;

            case MotionEvent.ACTION_UP:
                if (mState == STATE_OVER_LOST || mState == STATE_OVER_WIN) break;
                long t = System.currentTimeMillis();
                if (t - mFirstClickTime > 200) break;

                float x = event.getRawX();
                float y = event.getRawY();
                if (x > mFPointx + 20 || x < mFPointx - 20 || y > mFPointy + 20 || y < mFPointy - 20)
                    break;
                int p = getClickNum(x, y);
                if (p == -1) break;
                UniteView uv = mUniteViews.get(p - 1);
                turnOver(uv);
                break;
        }
        return true;
    }

    public void increaseScaneNum() {
        mScaneNum++;
    }

    /**
     * 产生一组不重复的随机数，在totalNum范围内，并指定其中不能有exception这个数字
     *
     * @param arrayNum  随机数的个数
     * @param totalNum  取值范围
     * @param exception 例外的数字
     * @return
     */
    private int[] getRamdomArray(int arrayNum, int totalNum, int exception) {

        if (arrayNum > totalNum || exception > totalNum) return null;

        int[] array = new int[arrayNum];
        ArrayList<Integer> arrayLists = new ArrayList();
        for (int i = 1; i <= totalNum; i++) {
            if (i == exception) continue;
            arrayLists.add(i);
        }
        Random r = new Random();
        for (int i = 0; i < arrayNum; i++) {
            int temp = r.nextInt(arrayLists.size());
            array[i] = arrayLists.get(temp);
            arrayLists.remove(temp);
        }
        return array;
    }

    public void resume(Point p) {
        mResumePoint = p;
        mResume = true;
    }
}
