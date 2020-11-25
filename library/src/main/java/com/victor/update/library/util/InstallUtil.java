package com.victor.update.library.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2020-2080, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: InstallUtil
 * Author: Victor
 * Date: 2020/11/25 12:14
 * Description:
 * -----------------------------------------------------------------
 */
public class InstallUtil {
    private static final String TAG = "InstallUtil";
    public static final int UNKNOWN_APP_SOURCES_REQUEST_CODE = 4;

    public static void installApk (Activity activity,String fileName) {
        Log.e(TAG,"installApk()......");
        if (activity == null) return;
        //兼容8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 判断是否有权限
            boolean haveInstallPermission = activity.getPackageManager().canRequestPackageInstalls();
            Log.e(TAG,"installApk()......haveInstallPermission = " + haveInstallPermission);
            if(!haveInstallPermission){
                //权限没有打开则提示用户去手动打开
                Uri packageUri = Uri.parse("package:" + activity.getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                activity.startActivityForResult(intent, UNKNOWN_APP_SOURCES_REQUEST_CODE);
                return;
            }
        }

        File file = new File(fileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri = FileProvider.getUriForFile(activity, "com.victor.com.update.fileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            try {
                activity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            activity.startActivity(intent);
        }
    }
}
