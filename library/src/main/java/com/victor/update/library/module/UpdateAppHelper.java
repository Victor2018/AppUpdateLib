package com.victor.update.library.module;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.victor.update.library.data.UpdateData;
import com.victor.update.library.interfaces.OnDownloadProgressListener;
import com.victor.update.library.model.DownLoadTask;
import com.victor.update.library.util.AppUtil;
import com.victor.update.library.util.Constant;
import com.victor.update.library.util.HttpUtil;
import com.victor.update.library.util.InstallUtil;
import com.victor.update.library.util.ParserUtil;
import com.victor.update.library.util.PermissionUtil;
import com.victor.update.library.util.UpdateHandler;

import java.util.HashMap;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2020-2080, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: UpdateAppHelper
 * Author: Victor
 * Date: 2020/11/25 12:21
 * Description:
 * -----------------------------------------------------------------
 */
public class UpdateAppHelper implements OnDownloadProgressListener {
    private String TAG = "UpdateAppHelper";
    public static final int UNKNOWN_APP_SOURCES_REQUEST_CODE = 10086;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 6;
    private Activity mActivity;
    private Handler mUpdateHandler;
    private HandlerThread mUpdateHandlerThread;
    private OnDownloadProgressListener mOnDownloadProgressListener;
    private String updateUrl;
    private UpdateData mUpdateData;

    public UpdateAppHelper(Activity activity, OnDownloadProgressListener listener) {
        mActivity = activity;
        mOnDownloadProgressListener = listener;
        initData();
        startRequestTask ();
    }

    private void startRequestTask (){
        mUpdateHandlerThread = new HandlerThread("AppUpdateTask");
        mUpdateHandlerThread.start();
        mUpdateHandler = new Handler(mUpdateHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constant.Action.CHECK_UPDATE_REQUEST:
                        checkUpdate();
                        Log.e(TAG,"handleMessage-CHECK_UPDATE_REQUEST");
                        sendRequestDelayed(Constant.Action.CHECK_UPDATE_REQUEST);
                        break;
                    case Constant.Action.INSTALL_APK:
                        if (mUpdateData != null) {
                            return;
                        }
                        HashMap<Integer,Object> dowloadMap = (HashMap<Integer, Object>) msg.obj;
                        if (dowloadMap != null) {
                            mUpdateData = (UpdateData) dowloadMap.get(msg.what);
                            installApk();
                        }

                        break;
                }
            }
        };
    }

    public void sendRequestWithParms (int action,Object requestData) {
        HashMap<Integer, Object> requestMap = new HashMap<Integer, Object>();
        requestMap.put(action, requestData);
        Message msg = mUpdateHandler.obtainMessage(action,requestMap);
        mUpdateHandler.sendMessage(msg);
    }

    public void sendRequest (int action) {
        Message msg = mUpdateHandler.obtainMessage(action);
        mUpdateHandler.sendMessage(msg);
    }

    public void sendRequestDelayed (int action) {
        Message msg = mUpdateHandler.obtainMessage(action);
        mUpdateHandler.sendMessageDelayed(msg,Constant.CHECK_DELAY_TIME);
    }

    public void onDestroy (){
        if (mUpdateHandlerThread != null) {
            mUpdateHandlerThread.quit();
            mUpdateHandlerThread = null;
        }

        updateUrl = null;
        mUpdateData = null;
    }

    private void initData () {
        ApplicationInfo appInfo = null;
        try {
            appInfo = mActivity.getPackageManager().getApplicationInfo(mActivity.getPackageName(), PackageManager.GET_META_DATA);
            updateUrl = appInfo.metaData.getString(Constant.UPDATE_URL_KEY);
            Log.e(TAG,"********************** updateUrl **********************");
            Log.e(TAG,"updateUrl = " + updateUrl);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate () {
        if (!PermissionUtil.hasPermission(mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionUtil.requestPermission(mActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_CODE);
            return;
        }

        String result = HttpUtil.HttpGetRequest(updateUrl);
        UpdateData parseData = ParserUtil.parse(mActivity,result);
        if (parseData != null) {
            int oldVersionCode = AppUtil.getAppVersionCode(mActivity);
            if (parseData.versionCode > oldVersionCode) {
                Log.e(TAG, "*************************** find new version *************************");
                if (!TextUtils.isEmpty(parseData.url)) {
                    Log.e(TAG, "*************************** start download new apk **************************");
                    DownLoadTask mDownLoadTask = new DownLoadTask(mActivity,parseData,this);
                    mDownLoadTask.requestDownloadData();
                } else {
                    Log.e(TAG,"downUrl is null!!!!!!");
                }
            } else {
                Log.e(TAG, "*************************** not find new version *************************");
            }
        }
    }

    private void installApk () {
        Log.e(TAG,"installApk>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                //兼容8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 判断是否有权限
            boolean haveInstallPermission = mActivity.getPackageManager().canRequestPackageInstalls();
            Log.e(TAG,"installApk()......haveInstallPermission = " + haveInstallPermission);
            if(!haveInstallPermission) {
                Log.e(TAG,"installApk>>>>>>>>>>>>>>>>>>>>>> 5555 >>>>>>>>>>>>>>>>");
                //权限没有打开则提示用户去手动打开
                Uri packageUri = Uri.parse("package:" + mActivity.getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivityForResult(intent, UNKNOWN_APP_SOURCES_REQUEST_CODE);
                return;
            }
        }

        if (mUpdateData != null) {
            String fileName = mUpdateData.path + mUpdateData.appName + ".apk";
            InstallUtil.installApk(mActivity,fileName);
        }

    }

    @Override
    public void onDownloadProgress(final UpdateData info) {
        UpdateHandler.runMainThread(new Runnable() {
            @Override
            public void run() {
                if (mOnDownloadProgressListener != null) {
                    mOnDownloadProgressListener.onDownloadProgress(info);
                }
            }
        });

        if (info != null) {
            if (info.progress == 100) {
                sendRequestWithParms(Constant.Action.INSTALL_APK,info);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_CODE) {
            if (PermissionUtil.hasPermission(mActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e(TAG,"onRequestPermissionsResult-CHECK_UPDATE_REQUEST");
                sendRequest(Constant.Action.CHECK_UPDATE_REQUEST);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == UNKNOWN_APP_SOURCES_REQUEST_CODE) {
                installApk();
            }
        }
    }
}
