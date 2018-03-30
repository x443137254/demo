package com.practice.minesweeper_20;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingActivity extends Activity implements View.OnClickListener {

    public final int EASY = 1,
            NORMAL = 2,
            HARD = 3,
            CUSTOM = 4;
    private int state = NORMAL;
    private String TAG = "debug";
    private EditText editTextLine, editTextRow, editTextMime;
    private CheckBox mSoundEffect;
    private CheckBox mSoundBackground;
    private CheckBox mAutoUpdate;
    private SeekBar mEffectVolume;
    private SeekBar mBackgroundVolume;
    private Intent mIntent;
    private Bundle mBundle;
    private RadioGroup radioGroup;
    private long mNewPackageLenght;
    private ProgressDialog mCheckInternetDialog;
    private ProgressDialog mDownloadProgress;
    private int mLine = 10, mRow = 15, mMiNe = 30, mLevel = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    new AlertDialog.Builder(SettingActivity.this)
                            .setMessage("网络错误！")
                            .setPositiveButton("OK", null)
                            .create().show();
                    break;
                case 404:
                    new AlertDialog.Builder(SettingActivity.this)
                            .setMessage("目前已是最新版本。")
                            .setPositiveButton("OK", null)
                            .create().show();
                    break;
                case 200:
                    new AlertDialog.Builder(SettingActivity.this)
                            .setMessage("发现新版本，立即更新？")
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
                                            String[] permission = {"android.permission.WRITE_EXTERNAL_STORAGE"};
                                            requestPermissions(permission, 1);
                                        } else sendDownLoadBroadcast();
                                    } else sendDownLoadBroadcast();
                                }
                            })
                            .setNegativeButton("不更新", null)
                            .create().show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        android.app.ActionBar actionBar = getActionBar();
        actionBar.setTitle("设置");

        init();
    }

    private void init() {
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        editTextLine = (EditText) findViewById(R.id.custom_edit_line);
        editTextRow = (EditText) findViewById(R.id.custom_edit_row);
        editTextMime = (EditText) findViewById(R.id.custom_edit_mime);
        mSoundEffect = (CheckBox) findViewById(R.id.soundeffect);
        mSoundBackground = (CheckBox) findViewById(R.id.soundbackground);
        mAutoUpdate = (CheckBox) findViewById(R.id.autoUpdate);
        mEffectVolume = (SeekBar) findViewById(R.id.effectseekbar);
        mBackgroundVolume = (SeekBar) findViewById(R.id.backgroundseekbar);
        setEditTextEnable(false);
        radioGroup.check(R.id.normal);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkChange(checkedId);
            }
        });
        Button commitButton = (Button) findViewById(R.id.commit);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        commitButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        mIntent = getIntent();
        mBundle = mIntent.getBundleExtra(FieldName.RECORD);

        mSoundEffect.setChecked(mBundle.getBoolean(FieldName.EF, true));
        mSoundBackground.setChecked(mBundle.getBoolean(FieldName.BG, true));
        mAutoUpdate.setChecked(mBundle.getBoolean(FieldName.AU, true));
        mSoundEffect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEffectVolume.setEnabled(isChecked);
            }
        });
        mSoundBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mBackgroundVolume.setEnabled(isChecked);
                if (isChecked) MineSeeperActivity.playBackgroundMusic();
                else MineSeeperActivity.stopBackgroundMusic();
            }
        });
        mEffectVolume.setMax(100);
        mBackgroundVolume.setMax(100);
        mEffectVolume.setProgress((int) (mBundle.getFloat(FieldName.EFV, 0.5f) * 100));
        mBackgroundVolume.setProgress((int) (mBundle.getFloat(FieldName.BGV, 0.5f) * 100));
        mBackgroundVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MineSeeperActivity.setBackgroundVolume(progress * 1.0f / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void checkChange(int id) {
        switch (id) {
            case R.id.easy:
                state = EASY;
                setEditTextEnable(false);
                break;
            case R.id.normal:
                state = NORMAL;
                setEditTextEnable(false);
                break;
            case R.id.hard:
                state = HARD;
                setEditTextEnable(false);
                break;
            case R.id.custom_radio:
                state = CUSTOM;
                setEditTextEnable(true);
                break;
        }

    }

    private void setEditTextEnable(Boolean enable) {
        editTextLine.setEnabled(enable);
        editTextRow.setEnabled(enable);
        editTextMime.setEnabled(enable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                this.finish();
                break;
            case R.id.commit:
                if (commit()) {
                    setResult(RESULT_OK, mIntent);
                    finish();
                }
                break;
        }
    }

    private void alerMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("输入错误");
        if (mLine == 0) {
            builder.setMessage("行数不能为0");
        } else if (mLine > 100) {
            builder.setMessage("行数太大");
            mLine = 100;
            editTextLine.setText("100");
        } else if (mRow == 0) {
            builder.setMessage("列数不能为0");
        } else if (mRow > 100) {
            builder.setMessage("列数太大");
            mRow = 100;
            editTextRow.setText("100");
        } else if (mMiNe == 0) {
            builder.setMessage("地雷数不能为0");
        } else if (mMiNe >= mLine * mRow) {
            builder.setMessage("地雷太多");
            mMiNe = mLine * mRow - 1;
            editTextMime.setText(String.valueOf(mLine * mRow - 1));
        }
        builder.setPositiveButton("确定", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public Boolean commit() {
        switch (state) {
            case EASY:
                mLine = 5;
                mRow = 8;
                mMiNe = 8;
                mLevel = 1;
                break;
            case NORMAL:
                mLine = 10;
                mRow = 15;
                mMiNe = 30;
                mLevel = 2;
                break;
            case HARD:
                mLine = 20;
                mRow = 30;
                mMiNe = 120;
                mLevel = 3;
                break;
            case CUSTOM:
                mLevel = 4;
                mLine = Integer.parseInt("0" + editTextLine.getText().toString());
                mRow = Integer.parseInt("0" + editTextRow.getText().toString());
                mMiNe = Integer.parseInt("0" + editTextMime.getText().toString());
                if (mLine > 100 | mRow > 100 | mMiNe > mLine * mRow | mLine * mRow * mMiNe == 0) {
                    alerMessage();
                    return false;
                }
                break;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(FieldName.LINE, mLine);
        bundle.putInt(FieldName.ROW, mRow);
        bundle.putInt(FieldName.MINE, mMiNe);
        bundle.putInt(FieldName.LEVEL, mLevel);
        bundle.putBoolean(FieldName.EF, mSoundEffect.isChecked());
        bundle.putBoolean(FieldName.BG, mSoundBackground.isChecked());
        bundle.putBoolean(FieldName.AU, mAutoUpdate.isChecked());
        bundle.putFloat(FieldName.EFV, mEffectVolume.getProgress() * 1.0f / 100);
        bundle.putFloat(FieldName.BGV, mBackgroundVolume.getProgress() * 1.0f / 100);
        mIntent.putExtra(FieldName.SET, bundle);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.checkreord:
                starCheckRecordActivity();
                break;
            case R.id.about:
                break;
        }
        return true;
    }

    private void starCheckRecordActivity() {
        Intent intent = new Intent(this, RcordActivity.class);
        intent.putExtra(FieldName.RECORD, mBundle);
        startActivity(intent);
    }

    public void checkUpdate(View v) {
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
            showProgressDialog();
            OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url(FieldName.URL).get().build();
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (mCheckInternetDialog != null && mCheckInternetDialog.isShowing())
                        mCheckInternetDialog.dismiss();
                    mHandler.sendEmptyMessage(0);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (mCheckInternetDialog != null && mCheckInternetDialog.isShowing())
                        mCheckInternetDialog.dismiss();
                    int code = response.code();
                    if (code == 404) {
                        mHandler.sendEmptyMessage(404);
                    } else if (code == 200) {
                        mNewPackageLenght = response.body().contentLength();
                        mHandler.sendEmptyMessage(200);
                    }
                }
            });
        }
    }

    private void sendDownLoadBroadcast() {
        Intent intent = new Intent(FieldName.DOWN_LOAD);
        sendBroadcast(intent);
        showDownloadProgress();
    }

    private void showDownloadProgress(){
        mDownloadProgress = new ProgressDialog(SettingActivity.this);
        mDownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadProgress.setMessage("正在下载...");
        mDownloadProgress.setMax((int) (mNewPackageLenght/1024));
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
                while (lenght <= mNewPackageLenght && mDownloadProgress.isShowing()){
                    SystemClock.sleep(100);
                    int progress = (int) (lenght/1024);
                    mDownloadProgress.setProgress(progress);
                    lenght = installPackage.length();
                }
//                if (mDownloadProgress.isShowing())
//                    mDownloadProgress.dismiss();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) sendDownLoadBroadcast();
        else {
            Toast.makeText(SettingActivity.this, "没有权限无法下载，或者您可以手动前往手机设置页面开启", Toast.LENGTH_LONG).show();
        }
    }

    private void showProgressDialog() {
        mCheckInternetDialog = new ProgressDialog(this);
        mCheckInternetDialog.setIndeterminate(false);
        mCheckInternetDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mCheckInternetDialog.setMessage("正在检查更新...");
        mCheckInternetDialog.show();
    }

    @Override
    protected void onResume() {
        File installPackage = new File(FieldName.DIR, FieldName.INSTALLPACKAGE + ".apk");
        if (installPackage.exists() && mDownloadProgress != null && mDownloadProgress.isShowing())
            mDownloadProgress.dismiss();
        super.onResume();
    }
}
