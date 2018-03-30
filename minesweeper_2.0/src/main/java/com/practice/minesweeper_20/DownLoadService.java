package com.practice.minesweeper_20;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownLoadService extends Service {

    private final String TAG = "debug";
    private long mStartIndex;
    private boolean mAbandon;
    OkHttpClient mHttpClient;

    BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == FieldName.ABANDON) mAbandon = true;
            else if (intent.getAction() == FieldName.DOWN_LOAD) downLoad();
        }
    };

    public DownLoadService() {
    }

    private void downLoad(){
        mAbandon = false;
        File installPackage = new File(FieldName.DIR, FieldName.INSTALLPACKAGE);
        if (installPackage.exists()) mStartIndex = installPackage.length();
        else mStartIndex = 0;
        mHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder = requestBuilder.get().url(FieldName.URL);
        if (mStartIndex != 0) requestBuilder.header("RANGE", "bytes=" + mStartIndex + "-");
        mHttpClient.newCall(requestBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MineSeeperActivity.mHandler.sendEmptyMessage(4);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (mAbandon) return;
                ResponseBody body = response.body();
                File instellPackage = new File(FieldName.DIR, FieldName.INSTALLPACKAGE);
                instellPackage.setReadable(true,false);
                InputStream downLoadStream = body.byteStream();
                FileOutputStream fileWriter = new FileOutputStream(instellPackage,true);
                byte[] buffBytes = new byte[1024*1024];
                int lenght = downLoadStream.read(buffBytes);
                while (lenght != -1){
                    if (mAbandon) return;
                    fileWriter.write(buffBytes, 0, lenght);
                    fileWriter.flush();
                    lenght = downLoadStream.read(buffBytes);
                }
                if (downLoadStream != null) downLoadStream.close();
                if (fileWriter != null) fileWriter.close();
                if (instellPackage.length() == body.contentLength() + mStartIndex) {
                    instellPackage.renameTo(new File(FieldName.DIR, FieldName.INSTALLPACKAGE + ".apk"));
                }
                sendBroadcast(new Intent(FieldName.INSTALLACTION));
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        IntentFilter intentFilter =new IntentFilter();
        intentFilter.addAction(FieldName.DOWN_LOAD);
        intentFilter.addAction(FieldName.ABANDON);
        registerReceiver(mDownloadReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mDownloadReceiver);
        super.onDestroy();
    }

}
