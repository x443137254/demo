package com.practice.downloadtest;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadActivity extends AppCompatActivity {

    File apk;
    final String TAG = "debug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        File dir = Environment.getExternalStorageDirectory();
        apk = new File(dir, "bbb.apk");
        Log.d(TAG, "onCreate: " + apk.toString());
    }
    @TargetApi(Build.VERSION_CODES.M)
    public void click(View v){
        checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE");
        shouldShowRequestPermissionRationale("android.permission.READ_EXTERNAL_STORAGE");
        String[] s = {"android.permission.READ_EXTERNAL_STORAGE"};
        requestPermissions(s, 5555);

        Log.d(TAG, "checkAfter-------");
        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().get().url("http://192.168.2.100:8080/aaa.apk").build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(DownloadActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                InputStream fis = response.body().byteStream();
                FileOutputStream fos = new FileOutputStream(apk);
                Log.d(TAG, "onResponse: ----------------------");
                byte[] buffByte = new byte[1024];
                int lenght = fis.read(buffByte);
                while (lenght > 0 ){
                    fos.write(buffByte, 0, lenght);
                    fos.flush();
                    lenght = fis.read(buffByte);
                }
                fis.close();
                fos.close();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apk),"application/vnd.android.package-archive");
                startActivity(intent);
            }
        });
    }
}
