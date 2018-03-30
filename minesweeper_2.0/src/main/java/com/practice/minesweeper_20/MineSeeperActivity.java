package com.practice.minesweeper_20;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MineSeeperActivity extends AppCompatActivity {

    private CoreView mCoreView;
    private SelectView mSelectView;
    private TimeView mTimeView;
    private GifView mGifView;
    private static final String TAG = "debug";
    private SharedPreferences mSp;
    public static Handler mHandler;
    private static SoundPool soundPool;
    private static int stopMusic;
    private static int music_background;
    private static int music_explode;
    private static int music_click;
    private static int music_win;
    private static float soundEffectVolume;
    private static float soundBackgroundVolume;
    private static boolean soundEffectEnable;
    private static boolean soundBackgroundEnable;

    private int mEasyTotalgame;
    private int mNormalTotalgame;
    private int mHardTotalgame;
    private int mEasyTotalwin;
    private int mNormalTotalwin;
    private int mHardTotalwin;
    private long mEasyRecord;
    private long mNormRecord;
    private long mHardRecord;
    private long mTime;

    private int resumeX;
    private int resumeY;

    private int mLevel;
    public final int EASY = 1;
    public final int NORMAL = 2;
    public final int HARD = 3;

    public final int DIALOG_RES = 0;
    public final int DIALOG_LOST = 1;
    public final int DIALOG_WIN = 2;
    public final int DIALOG_FIN = 3;

    private ProgressDialog mCheckInternetDialog;
    private ProgressDialog mDownloadProgress;
    private long mNewVersionLenght;

    private boolean mAutoUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_layout);
        getSupportActionBar().hide();

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mCoreView = (CoreView) findViewById(R.id.coreVIew);
        mTimeView = (TimeView) findViewById(R.id.timeView);
        mSelectView = new SelectView(this, mCoreView);
        mGifView = new GifView(this);

        mGifView.setSize(mCoreView.getSize());
        mCoreView.getGif(mGifView);
        relativeLayout.addView(mSelectView);
        relativeLayout.addView(mGifView);
        mSp = getSharedPreferences("record", 0);

        getRecord();
        mLevel = NORMAL;
        handleMessage();
        initSoundSources();

        satrtUpdateService();
    }

    private void satrtUpdateService() {
        startService(new Intent(this, DownLoadService.class));
        if (mAutoUpdate) updateCheck();
    }


    public void updateCheck() {
        File installPackage = new File(FieldName.DIR, FieldName.INSTALLPACKAGE + ".apk");
        if (installPackage.exists()) {
            new AlertDialog.Builder(this)
                    .setMessage("本地已有最新版安装包，现在更新？")
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendBroadcast(new Intent(FieldName.INSTALLACTION));
                        }
                    })
                    .setNegativeButton("不更新", null)
                    .create()
                    .show();
        } else {
                OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
                Request request = new Request.Builder().url(FieldName.URL).get().tag(this).build();
                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //TODO
                        // 自己主动取消的错误的 java.net.SocketException: Socket closed
                        // 超时的错误是 java.net.SocketTimeoutException
                        // 网络出错的错误是java.net.ConnectException: Failed to connect to xxxxx
//                        mCheckInternetDialog.cancel();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() == 200) {
                            mNewVersionLenght = response.body().contentLength();
                            mHandler.sendEmptyMessage(4);
                        }
                    }
                });

        }
    }


    private void startDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                String[] permission = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                requestPermissions(permission, 1);
            }else sendDownloadbroadcast();
        }
        else sendDownloadbroadcast();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendDownloadbroadcast();
        }
        else {
            Toast.makeText(MineSeeperActivity.this, "没有权限无法下载，或者您可以手动前往手机设置页面开启", Toast.LENGTH_LONG).show();
        }
    }

    private void sendDownloadbroadcast(){
        Intent intent = new Intent(FieldName.DOWN_LOAD);
        sendBroadcast(intent);
        showDownloadProgress();
    }

    private void showDownloadProgress(){
        mDownloadProgress = new ProgressDialog(MineSeeperActivity.this);
        mDownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadProgress.setMessage("正在下载...");
        mDownloadProgress.setMax((int) (mNewVersionLenght/1024));
        mDownloadProgress.setProgressNumberFormat("%1d/%2d  k");
        mDownloadProgress.setCancelable(false);
        mDownloadProgress.setIndeterminate(false);
        mDownloadProgress.setButton(DialogInterface.BUTTON_POSITIVE, "后台下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDownloadProgress.cancel();
            }
        });
        mDownloadProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "取消更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDownloadProgress.cancel();
                sendBroadcast(new Intent(FieldName.ABANDON));
            }
        });
        mDownloadProgress.show();

        new Thread(new Runnable() {
            File installPackage = new File(FieldName.DIR, FieldName.INSTALLPACKAGE);
            long lenght = installPackage.length();
            @Override
            public void run() {
                while (lenght <= mNewVersionLenght && mDownloadProgress.isShowing()){
                    SystemClock.sleep(100);
                    int progress = (int) (lenght/1024);
                    mDownloadProgress.setProgress(progress);
                    lenght = installPackage.length();
                }
                if (mDownloadProgress.isShowing())
                    mDownloadProgress.dismiss();
            }
        }).start();
    }

    private void handleMessage() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        mTimeView.invalidate();
                        break;
                    case 2:
                        if (msg.arg1 == mCoreView.MESSAGE_START) {
                            mTimeView.start();
                        } else if (msg.arg1 == mCoreView.MESSAGE_WIN) {
                            mTimeView.stop();
                            countGameTimes(true);
                            mTime = mTimeView.getTime();
                            toastDialog(DIALOG_WIN);
                        } else {
                            mTimeView.stop();
                            countGameTimes(false);
                            mCoreView.setExplodeTime(true);
                            mSelectView.setExplodeTime(true);
                        }
                        break;
                    case 3:
                        mCoreView.setExplodeTime(false);
                        mSelectView.setExplodeTime(false);
                        toastDialog(DIALOG_LOST);
                        break;
                    case 4:
//                        mCheckInternetDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(MineSeeperActivity.this);
                        builder.setMessage("发现新版本，立即更新？")
                                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startDownload();
                                    }
                                })
                                .setNegativeButton("不更新", null);
                        builder.create().show();
                        break;
                    case 5:
                        if (msg.arg1 == mSelectView.RES_MSG) {
                            toastDialog(DIALOG_RES);
                        } else if (msg.arg1 == mSelectView.LEF_MSG) {
                            mCoreView.setMode(mCoreView.MODE_CLICK);
                        } else if (msg.arg1 == mSelectView.RIG_MSG) {
                            mCoreView.setMode(mCoreView.MODE_FLAG);
                        } else {
                            gotoAnotherActicity();
                        }
                        break;

                }
            }


        };
    }

    private void initSoundSources() {

        soundEffectEnable = mSp.getBoolean(FieldName.EF, true);
        soundBackgroundEnable = mSp.getBoolean(FieldName.BG, true);
        mAutoUpdate = mSp.getBoolean(FieldName.AU, true);
        soundEffectVolume = mSp.getFloat(FieldName.EFV, 0.5f);
        soundBackgroundVolume = mSp.getFloat(FieldName.BGV, 0.5f);

        soundPool = new SoundPool(255, AudioManager.STREAM_MUSIC, 0);
        music_explode = soundPool.load(this, R.raw.bomb, 1);
        music_click = soundPool.load(this, R.raw.click, 1);
        music_win = soundPool.load(this, R.raw.win, 1);
        music_background = soundPool.load(this, R.raw.background, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (sampleId == music_background) playBackgroundMusic();
            }
        });
    }

    public static void playBackgroundMusic() {
        stopBackgroundMusic();
        if (!soundBackgroundEnable) return;
        stopMusic = soundPool.play(music_background, soundBackgroundVolume, soundBackgroundVolume, 1, -1, 1.0f);
    }

    public static void stopBackgroundMusic() {
        soundPool.stop(stopMusic);
    }

    public static void setBackgroundVolume(float v) {
        soundBackgroundVolume = v;
        soundPool.setVolume(music_background, v, v);
    }

    public static void playWinMusic() {
        stopBackgroundMusic();
        if (!soundEffectEnable) return;
        soundPool.play(music_win, soundEffectVolume, soundEffectVolume, 1, 0, 1.0f);
    }

    public static void playClickMusic() {
        if (!soundEffectEnable) return;
        soundPool.play(music_click, soundEffectVolume, soundEffectVolume, 1, 0, 1.0f);
    }

    public static void playLostMusic() {
        stopBackgroundMusic();
        if (!soundEffectEnable) return;
        soundPool.play(music_explode, soundEffectVolume, soundEffectVolume, 1, 0, 1.5f);
    }

    private void countGameTimes(boolean win) {

        switch (mLevel) {
            case EASY:
                if (win) mEasyTotalwin++;
                mEasyTotalgame++;
                break;
            case NORMAL:
                if (win) mNormalTotalwin++;
                mNormalTotalgame++;
                break;
            case HARD:
                if (win) mHardTotalwin++;
                mHardTotalgame++;
                break;
        }
    }

    private void gotoAnotherActicity() {
        Intent intent = new Intent(this, SettingActivity.class);
        Bundle bundle = new Bundle();
        float easyRate = mEasyTotalgame == 0 ? 0 : mEasyTotalwin * 1.0f / mEasyTotalgame;
        float normalRate = mNormalTotalgame == 0 ? 0 : mNormalTotalwin * 1.0f / mNormalTotalgame;
        float hardRate = mHardTotalgame == 0 ? 0 : mHardTotalwin * 1.0f / mHardTotalgame;
        bundle.putLong(FieldName.EASY, mEasyRecord);
        bundle.putLong(FieldName.NORMAL, mNormRecord);
        bundle.putLong(FieldName.HARD, mHardRecord);
        bundle.putFloat(FieldName.ERATE, easyRate);
        bundle.putFloat(FieldName.NRATE, normalRate);
        bundle.putFloat(FieldName.HRATE, hardRate);
        bundle.putFloat(FieldName.EFV, soundEffectVolume);
        bundle.putFloat(FieldName.BGV, soundBackgroundVolume);
        bundle.putBoolean(FieldName.EF, soundEffectEnable);
        bundle.putBoolean(FieldName.BG, soundBackgroundEnable);
        bundle.putBoolean(FieldName.AU, mAutoUpdate);
        intent.putExtra(FieldName.RECORD, bundle);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onPause() {
        mTimeView.stop();
        super.onPause();
    }

    @Override
    protected void onStart() {
        if (mCoreView.getState() == mCoreView.STATE_RUNNING) mTimeView.start();
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == -1 && requestCode == 1) {
            Bundle bundle = data.getBundleExtra(FieldName.SET);
            mCoreView.setWidthNum(bundle.getInt(FieldName.LINE));
            mCoreView.setHeightNum(bundle.getInt(FieldName.ROW));
            mCoreView.setMimes(bundle.getInt(FieldName.MINE));
            mLevel = bundle.getInt(FieldName.LEVEL);
            soundEffectEnable = bundle.getBoolean(FieldName.EF, true);
            soundBackgroundEnable = bundle.getBoolean(FieldName.BG, true);
            mAutoUpdate = bundle.getBoolean(FieldName.AU, true);
            soundEffectVolume = bundle.getFloat(FieldName.EFV, 0.5f);
            soundBackgroundVolume = bundle.getFloat(FieldName.BGV, 0.5f);
            restart();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toastDialog(int which) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MineSeeperActivity.this);
        String title = "";
        String msg = "";

        switch (which) {

            case DIALOG_RES:
                msg = "重新开始？";
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                    }
                });
                break;

            case DIALOG_LOST:
                title = "输了";
                msg = "这次没走运，很遗憾";
                builder.setPositiveButton("重来", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                    }
                });
                break;

            case DIALOG_WIN:
                title = "赢了";
                SimpleDateFormat sd = new SimpleDateFormat("mm:ss");
                if (mLevel == EASY) {
                    if ((mTime < mEasyRecord && mEasyRecord != 0) || mEasyRecord == 0) {
                        mEasyRecord = mTime;
                        msg = "恭喜您创下新记录：" + sd.format(new Date(mTime));
                    } else {
                        msg = "最高纪录：" + sd.format(new Date(mEasyRecord));
                    }
                }
                if (mLevel == NORMAL) {
                    if ((mTime < mNormRecord && mNormRecord != 0) || mNormRecord == 0) {
                        mNormRecord = mTime;
                        msg = "恭喜您创下新记录：" + sd.format(new Date(mTime));
                    } else {
                        msg = "最高纪录：" + sd.format(new Date(mNormRecord));
                    }
                }
                if (mLevel == HARD) {
                    if ((mTime < mHardRecord && mHardRecord != 0) || mHardRecord == 0) {
                        mHardRecord = mTime;
                        msg = "恭喜您创下新记录：" + sd.format(new Date(mTime));
                    } else {
                        msg = "最高纪录：" + sd.format(new Date(mHardRecord));
                    }
                }

                builder.setPositiveButton("重来", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                    }
                });
                break;

            case DIALOG_FIN:
                msg = "退出游戏？";
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                break;
            default:
                msg = "赢了";
                builder.setPositiveButton("重来", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                    }
                });

        }

        builder.setMessage(msg)
                .setTitle(title)
                .setNegativeButton("取消", null)
                .create()
                .show();

    }

    private void restart() {
        mCoreView.initGame();
        mTimeView.resst();
        mSelectView.reset();
        mGifView.reset();
        playBackgroundMusic();
    }

    private void getRecord() {
        mEasyTotalwin = mSp.getInt(FieldName.EWIN, 0);
        mNormalTotalwin = mSp.getInt(FieldName.NWIN, 0);
        mHardTotalwin = mSp.getInt(FieldName.HWIN, 0);
        mEasyTotalgame = mSp.getInt(FieldName.ETOTAL, 0);
        mNormalTotalgame = mSp.getInt(FieldName.NTOTAL, 0);
        mHardTotalgame = mSp.getInt(FieldName.HTOTAL, 0);
        mEasyRecord = mSp.getLong(FieldName.ETIME, 0);
        mNormRecord = mSp.getLong(FieldName.NTIME, 0);
        mHardRecord = mSp.getLong(FieldName.HTIME, 0);
    }

    private void saveRecord() {
        mSp.edit().putInt(FieldName.EWIN, mEasyTotalwin)
                .putInt(FieldName.NWIN, mNormalTotalwin)
                .putInt(FieldName.HWIN, mHardTotalwin)
                .putInt(FieldName.ETOTAL, mEasyTotalgame)
                .putInt(FieldName.NTOTAL, mNormalTotalgame)
                .putInt(FieldName.HTOTAL, mHardTotalgame)
                .putLong(FieldName.ETIME, mEasyRecord)
                .putLong(FieldName.NTIME, mNormRecord)
                .putLong(FieldName.HTIME, mHardRecord)
                .putBoolean(FieldName.EF, soundEffectEnable)
                .putBoolean(FieldName.BG, soundBackgroundEnable)
                .putBoolean(FieldName.AU, mAutoUpdate)
                .putFloat(FieldName.EFV, soundEffectVolume)
                .putFloat(FieldName.BGV, soundBackgroundVolume)
                .apply();
    }

    @Override
    protected void onStop() {
        resumeX = mCoreView.getLeft();
        resumeY = mCoreView.getTop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        File installPackage = new File(FieldName.DIR, FieldName.INSTALLPACKAGE + ".apk");
        if (installPackage.exists() && mDownloadProgress != null && mDownloadProgress.isShowing()) mDownloadProgress.dismiss();
        mGifView.reset();
        mCoreView.resume(new Point(resumeX, resumeY));
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        saveRecord();
        soundPool.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        toastDialog(DIALOG_FIN);
    }
}


