package com.practice.minesweeper_20;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.net.URI;

/**
 * Created by Administrator on 2018/3/20.
 */
public class InstallReciver extends BroadcastReceiver {

    private final String TAG = "debug";

    @Override
    public void onReceive(Context context, Intent intent) {
        File packageFile = new File(FieldName.DIR, FieldName.INSTALLPACKAGE + ".apk");
        Uri uri = Uri.fromFile(packageFile);
        Intent it = new Intent(Intent.ACTION_VIEW);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(it);
    }
}
