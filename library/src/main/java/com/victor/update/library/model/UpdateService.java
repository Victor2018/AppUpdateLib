package com.victor.update.library.model;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.victor.update.library.data.UpdateData;
import com.victor.update.library.interfaces.OnDownloadProgressListener;
import com.victor.update.library.interfaces.OnUpdateCompleteListener;
import com.victor.update.library.util.AppUtil;
import com.victor.update.library.util.Constant;

import org.json.JSONObject;

import java.io.File;

public class UpdateService extends Service implements OnUpdateCompleteListener,OnDownloadProgressListener {
    private String TAG = "UpdateService";
    private CheckUpdateTask mCheckUpdateTask;
    private DownLoadTask mDownLoadTask;
    private String updateUrl;
    private int status = Constant.Status.CHECK;//默认检测升级
    private UpdateData updateData;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constant.Msg.UPDATE_REQUEST:
                    checkUpdate(updateUrl);
                    break;
                case Constant.Msg.REQUEST_SUCCESS:
                    parseData(msg.obj.toString());
                    break;
                case Constant.Msg.REQUEST_FAILED:
                    status = Constant.Status.CHECK;
                    Toast.makeText(getApplicationContext(), "访问服务器失败！", Toast.LENGTH_SHORT).show();
                    break;
                case Constant.Msg.NETWORK_ERROR:
                    status = Constant.Status.CHECK;
                    Toast.makeText(getApplicationContext(),"网络错误，请检查网络是否连接！",Toast.LENGTH_SHORT).show();
                    break;
                case Constant.Msg.SOCKET_TIME_OUT:
                    status = Constant.Status.CHECK;
                    Toast.makeText(getApplicationContext(),"访问服务器超时，请重试！",Toast.LENGTH_SHORT).show();
                    break;
                case Constant.Msg.DOWNLOAD_OVER:
                    status = Constant.Status.DOWNLOAD_OVER;
                    UpdateData info = (UpdateData) msg.obj;
                    if (info != null) {
                        status = Constant.Status.INSTALLING;
                        DataObservable.getInstance().setData(info);
                    }
                    break;
            }
            if (status == Constant.Status.CHECK) {
                mHandler.removeCallbacks(mUpdateRunnable);
                mHandler.postDelayed(mUpdateRunnable, Constant.CHECK_DELAY_TIME);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate()......");
        initData();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand()......");
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }
        checkUpdate(updateUrl);
        return START_STICKY;
    }

    private void initData () {
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            updateUrl = appInfo.metaData.getString(Constant.UPDATE_URL_KEY);
            Log.e(TAG,"********************** updateUrl **********************");
            Log.e(TAG,"updateUrl = " + updateUrl);
            if (!TextUtils.isEmpty(updateUrl)) {
                mHandler.postDelayed(mUpdateRunnable,10 * 1000);
            } else {
                Log.e(TAG,"updateUrl is null!!!!!!");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate(String url){
        switch (status) {
            case Constant.Status.CHECK:
                status = Constant.Status.CHECKING;
                mCheckUpdateTask = new CheckUpdateTask(getApplicationContext(),this);
                mCheckUpdateTask.requestUpdateData(url);
                break;
            case Constant.Status.CHECKING:
                Log.e(TAG,"************************* CHECKING **************************");
                break;
            case Constant.Status.DOWNLOADING:
                Log.e(TAG,"************************* DOWNLOADING **************************");
                break;
            case Constant.Status.DOWNLOAD_OVER:
                Log.e(TAG,"************************* DOWNLOADING **************************");
                break;
            case Constant.Status.INSTALLING:
                Log.e(TAG,"************************* INSTALLING **************************");
                break;
        }
    }

    private void parseData(String result) {
        Log.e(TAG,"parseData()......");
        try {
            JSONObject data = new JSONObject(result);
            if (data != null) {
                updateData = new UpdateData();
                updateData.setAppName(data.optString("app_name").trim());
                updateData.setVersionName(data.optString("version_name").trim());
                updateData.setVersionCode(data.optInt("version_code"));
                updateData.setUpdateContent(data.optString("update_content"));
                updateData.setUrl(data.optString("url"));
                int oldVersionCode = AppUtil.getAppVersionCode(getApplicationContext());
                if (updateData.getVersionCode() > oldVersionCode) {
                    Log.e(TAG, "*************************** find new version *************************");
                    if (!TextUtils.isEmpty(updateData.getUrl())) {
                        Log.e(TAG, "*************************** start download new apk **************************");
                        status = Constant.Status.DOWNLOADING;
                        mDownLoadTask = new DownLoadTask(getApplicationContext(),updateData,this);
                        mDownLoadTask.requestDownloadData();
                    } else {
                        status = Constant.Status.CHECK;
                        Log.e(TAG,"downUrl is null!!!!!!");
                    }
                } else {
                    Log.e(TAG, "*************************** not find new version *************************");
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"parse data error l!!!!!!");
            Log.e(TAG,"result = " + result);

        }
    }

    Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.removeMessages(Constant.Msg.UPDATE_REQUEST);
            mHandler.sendEmptyMessage(Constant.Msg.UPDATE_REQUEST);
        }
    };

    @Override
    public void onDownloadProgress(UpdateData info) {
        if (info != null) {
            if (info.getProgress() == 100) {
                Message msg = mHandler.obtainMessage(Constant.Msg.DOWNLOAD_OVER,info);
                msg.sendToTarget();
            }
        }
    }

    @Override
    public void onUpdateCompelete(Bundle result) {
        Log.e(TAG, "onUpdateCompelete()......");
        int status = result.getInt(Constant.STATUS_KEY);
        int requstMsg = result.getInt(Constant.REQUEST_MSG_KEY);
        Message msg = new Message();
        msg.arg1 = requstMsg;
        switch (status) {
            case Constant.Msg.REQUEST_SUCCESS:
                msg.what = Constant.Msg.REQUEST_SUCCESS;
                msg.obj = result.getString(Constant.UPDATE_DATA_KEY);
                break;
            case Constant.Msg.REQUEST_FAILED:
                msg.what = Constant.Msg.REQUEST_FAILED;
                break;
            case Constant.Msg.NETWORK_ERROR:
                msg.what = Constant.Msg.NETWORK_ERROR;
                break;
            case Constant.Msg.SOCKET_TIME_OUT:
                msg.what = Constant.Msg.SOCKET_TIME_OUT;
                break;
        }
        if (msg.arg1 == Constant.Msg.UPDATE_REQUEST) {
            mHandler.sendMessage(msg);
        } else if (msg.arg1 == Constant.Msg.DOWNLOAD_OVER) {
            msg.what = Constant.Msg.DOWNLOAD_OVER;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy()......");
        if (mCheckUpdateTask != null) {
            mCheckUpdateTask = null;
        }
        if (mDownLoadTask != null) {
            mDownLoadTask = null;
        }
        if (updateData != null) {
            updateData = null;
        }
        if (updateUrl != null) {
            updateUrl = null;
        }
    }
}