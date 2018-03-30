package com.practice.minesweeper_20;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 封装单位格子的所有信息
 */
public class UniteView {

    private final String TAG = "debug";
    private int mX;     //横坐标（第几个）
    private int mY;     //纵坐标
    public int mId;
    private Bitmap mBitmap;
    private boolean mIsMime;
    private int mNeighbourMime;
    public int mState;                      //保存单元格的状态
    public final int STATE_INIT = 0;       //刚开始的状态
    public final int STATE_TURNED = 1;     //已经点击过的状态
    public final int STATE_FLAGED = 2;     //被标记有雷的状态
    public final int STATE_UNWERE = 3;     //标记为不确定的状态

    private CoreView mCoreView = null;
    public List<UniteView> mNeighbours = null;

    public UniteView(CoreView cv, int x, int y) {
        this.mX = x;
        this.mY = y;
        this.mCoreView = cv;
        mId = mCoreView.getWidthNum() * (this.mY - 1) + this.mX;
        init();
    }

    public void init() {
        mState = STATE_INIT;
        mBitmap = mCoreView.bitmaps[10];
        mIsMime = false;
        mNeighbourMime = 0;
    }

    public void setMime(Boolean isMime) {
        this.mIsMime = isMime;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public Bitmap getBitmap() {

        return mBitmap;
    }

    public Boolean isMime() {
        return mIsMime;
    }

    /**
     * 返回一组相邻格子的对象
     */
    public void findNeighbours() {
        mNeighbours = new ArrayList();
        int w, h;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                w = this.mX + i;
                h = this.mY + j;
                if (w == 0 || w > mCoreView.getWidthNum() || h == 0 || h > mCoreView.getHeightNum())
                    continue;
                mNeighbours.add(mCoreView.mUniteViews.get(mCoreView.getWidthNum() * (h - 1) + w - 1));
            }
        }

    }

    public int getNeighboursMimeNum() {
        int neighboursMimeNum = 0;
        for (int i = 0; i < mNeighbours.size(); i++) {
            if (mNeighbours.get(i).mIsMime) {
                neighboursMimeNum++;
            }
        }
        return neighboursMimeNum;
    }

    public int click() {
        if (mCoreView.getMode() == mCoreView.MODE_FLAG && mState != STATE_TURNED) {
            flag();
            return 0;
        } else if (mState == STATE_INIT) {
            return turnOver();
        } else if (mState == STATE_TURNED) {
            int flags = 0;
            for (UniteView uv : mNeighbours) {
                if (uv.mState == STATE_FLAGED) flags++;
            }
            if (mNeighbourMime == flags) {
                for (UniteView uv : mNeighbours) {
                    if (uv.mState == STATE_INIT) {
                        int p = uv.turnOver();
                        if (p > 0) return p;
                    }
                }
            }
            return 0;
        }
        return 0;
    }

    public int turnOver() {
        mState = STATE_TURNED;
        if (isMime()) return mId;
        else {
            mCoreView.increaseScaneNum();
            int mimes = getNeighboursMimeNum();
            mNeighbourMime = mimes;
            if (mimes > 0) {
                mBitmap = mCoreView.bitmaps[mimes];
                return 0;
            } else {
                mBitmap = mCoreView.bitmaps[0];
                mCoreView.invalidate();
                Iterator<UniteView> it = mNeighbours.iterator();
                while (it.hasNext()) {
                    UniteView uv = it.next();
                    if (uv.mState == STATE_INIT) uv.turnOver();
                }
                return 0;
            }
        }
    }

    public void flag() {
        if (mState == STATE_INIT) {
            mState = STATE_FLAGED;
            mBitmap = mCoreView.bitmaps[11];
            mCoreView.invalidate();
        } else if (mState == STATE_FLAGED) {
            mState = STATE_UNWERE;
            mBitmap = mCoreView.bitmaps[14];
            mCoreView.invalidate();
        } else if (mState == STATE_UNWERE) {
            mState = STATE_INIT;
            mBitmap = mCoreView.bitmaps[10];
            mCoreView.invalidate();
        }

    }

}
