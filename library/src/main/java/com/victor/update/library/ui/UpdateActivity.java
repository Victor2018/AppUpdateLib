package com.victor.update.library.ui;import android.content.Intent;import android.content.pm.ApplicationInfo;import android.content.pm.PackageManager;import android.net.Uri;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.support.v7.app.AppCompatActivity;import android.text.TextUtils;import android.util.Log;import android.widget.Toast;import org.json.JSONObject;import java.io.File;import com.victor.update.library.R;import com.victor.update.library.data.UpdateData;import com.victor.update.library.interfaces.OnDownloadCompeleteListener;import com.victor.update.library.interfaces.OnUpdateCompleteListener;import com.victor.update.library.model.CheckUpdateTask;import com.victor.update.library.model.DownLoadTask;import com.victor.update.library.util.AppUtil;import com.victor.update.library.util.Constant;public class UpdateActivity extends AppCompatActivity implements OnUpdateCompleteListener,OnDownloadCompeleteListener {    private String TAG = "UpdateActivity";    private CheckUpdateTask mCheckUpdateTask;    private DownLoadTask mDownLoadTask;    private boolean isDownloading;//正在下载    private String updateUrl;    private UpdateData updateData;    Handler mHandler = new Handler(){        @Override        public void handleMessage(Message msg) {            switch (msg.what){                case Constant.Msg.UPDATE_REQUEST:                    checkUpdate(updateUrl);                    break;                case Constant.Msg.REQUEST_SUCCESS:                    parseData(msg.obj.toString());                    break;                case Constant.Msg.REQUEST_FAILED:                    isDownloading = false;                    Toast.makeText(getApplicationContext(), "访问服务器失败！", Toast.LENGTH_SHORT).show();                    break;                case Constant.Msg.NETWORK_ERROR:                    isDownloading = false;                    Toast.makeText(getApplicationContext(),"网络错误，请检查网络是否连接！",Toast.LENGTH_SHORT).show();                    break;                case Constant.Msg.SOCKET_TIME_OUT:                    isDownloading = false;                    Toast.makeText(getApplicationContext(),"访问服务器超时，请重试！",Toast.LENGTH_SHORT).show();                    break;                case Constant.Msg.DOWNLOAD_OVER:                    isDownloading = false;                    UpdateData info = (UpdateData) msg.obj;                    if (info != null) {                        Toast.makeText(getApplicationContext(),"下载完毕！",Toast.LENGTH_SHORT).show();                        Uri uri = Uri.parse("file://" + new File("/sdcard/" + info.getAppName() + ".apk"));                        Intent intent = new Intent(Intent.ACTION_VIEW);                        intent.setDataAndType(uri,"application/vnd.android.package-archive");                        startActivity(intent);                    }                    break;            }            if (!isDownloading) {                mHandler.removeCallbacks(mUpdateRunnable);                mHandler.postDelayed(mUpdateRunnable, 1000 * 60 * 5);            }        }    };    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_update);        initData();    }    private void initData () {        ApplicationInfo appInfo = null;        try {            appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);            updateUrl = appInfo.metaData.getString(Constant.UPDATE_URL_KEY);            Log.e(TAG,"********************** updateUrl **********************");            Log.e(TAG,"updateUrl = " + updateUrl);            if (!TextUtils.isEmpty(updateUrl)) {                mHandler.postDelayed(mUpdateRunnable,10 * 1000);            } else {                Log.e(TAG,"updateUrl is null!!!!!!");            }        } catch (PackageManager.NameNotFoundException e) {            e.printStackTrace();        }    }    private void checkUpdate(String url){        Log.e(TAG, "********************** check update ************************");        mCheckUpdateTask = new CheckUpdateTask(getApplicationContext(),this);        mCheckUpdateTask.requestUpdateData(url);    }    private void parseData(String result) {        Log.e(TAG,"parseData()......");        try {            JSONObject data = new JSONObject(result);            if (data != null) {                updateData = new UpdateData();                updateData.setAppName(data.optString("app_name").trim());                updateData.setVersionName(data.optString("version_name").trim());                updateData.setVersionCode(data.optInt("version_code"));                updateData.setUpdateContent(data.optString("update_content"));                updateData.setUrl(data.optString("url"));                int oldVersionCode = AppUtil.getAppVersionCode(getApplicationContext());                if (updateData.getVersionCode() > oldVersionCode) {                    Log.e(TAG, "*************************** find new version *************************");                    if (!TextUtils.isEmpty(updateData.getUrl())) {                        Log.e(TAG, "*************************** start download new apk **************************");                        isDownloading = true;                        mDownLoadTask = new DownLoadTask(getApplicationContext(),updateData,this);                        mDownLoadTask.requestDownloadData();                    } else {                        isDownloading = false;                        Log.e(TAG,"downUrl is null!!!!!!");                    }                } else {                    Log.e(TAG, "*************************** not find new version *************************");                }            }        }catch (Exception e) {            Log.e(TAG,"parse data error l!!!!!!");            Log.e(TAG,"result = " + result);        }    }    Runnable mUpdateRunnable = new Runnable() {        @Override        public void run() {            mHandler.removeMessages(Constant.Msg.UPDATE_REQUEST);            mHandler.sendEmptyMessage(Constant.Msg.UPDATE_REQUEST);        }    };    @Override    protected void onDestroy() {        super.onDestroy();    }    @Override    public void onUpdateCompelete(Bundle result) {        Log.e(TAG, "onUpdateCompelete()......");        int status = result.getInt(Constant.STATUS_KEY);        int requstMsg = result.getInt(Constant.REQUEST_MSG_KEY);        Message msg = new Message();        msg.arg1 = requstMsg;        switch (status) {            case Constant.Msg.REQUEST_SUCCESS:                msg.what = Constant.Msg.REQUEST_SUCCESS;                msg.obj = result.getString(Constant.UPDATE_DATA_KEY);                Log.e(TAG, "receive-msg.obj = " + msg.obj.toString());                break;            case Constant.Msg.REQUEST_FAILED:                msg.what = Constant.Msg.REQUEST_FAILED;                break;            case Constant.Msg.NETWORK_ERROR:                msg.what = Constant.Msg.NETWORK_ERROR;                break;            case Constant.Msg.SOCKET_TIME_OUT:                msg.what = Constant.Msg.SOCKET_TIME_OUT;                break;        }        if (msg.arg1 == Constant.Msg.UPDATE_REQUEST) {            mHandler.sendMessage(msg);        } else if (msg.arg1 == Constant.Msg.DOWNLOAD_OVER) {            msg.what = Constant.Msg.DOWNLOAD_OVER;            mHandler.sendMessage(msg);        }    }    @Override    public void onDownloadComplete(UpdateData info) {        Message msg = mHandler.obtainMessage(Constant.Msg.DOWNLOAD_OVER,info);        msg.sendToTarget();    }}