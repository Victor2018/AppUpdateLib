package com.victor.update.library.model;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import com.victor.update.library.interfaces.OnDownloadCompeleteListener;
import com.victor.update.library.util.Constant;
import com.victor.update.library.util.HttpUtil;

public class DownLoadTask {
	private String TAG = "DownLoadTask";
	private int requestCount;
	private Context mContext;
	private String mAppName;
	private String mUrl;
	private int current;
	private int total;
	private OnDownloadCompeleteListener mOnDownloadCompeleteListener;

	public DownLoadTask(Context context,String appName,String url,OnDownloadCompeleteListener listener) {
		mContext = context;
		mAppName = appName;
		mUrl = url;
		mOnDownloadCompeleteListener = listener;
	}

	public void requestDownloadData() {
		requestCount++;
		Log.e(TAG, "requestCount=" + requestCount);
		if(requestCount > 5){
			return;
		}
		new UpdateTask().execute(mUrl);
	}

	class UpdateTask extends AsyncTask<String, Integer, Bundle>{

		@Override
		protected Bundle doInBackground(String... params) {
			// TODO Auto-generated method stub
			int status = 0;
			Bundle responseData = new Bundle();
			try {
				if (HttpUtil.isNetEnable(mContext)){
					URL url = new URL(params[0]);
					URLConnection con = url.openConnection();
					total = con.getContentLength();
					InputStream is = con.getInputStream();
					File file = new File("/sdcard/" + mAppName + ".apk");
					if (!file.exists()) {
						file.createNewFile();
					}
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int size = is.read(buffer);
					while (size != -1) {
						current += size;
						fos.write(buffer, 0, size);
						size = is.read(buffer);
						int progress = current * 100 / total;
						//    时刻将当前进度更新给onProgressUpdate方法
						publishProgress(progress);
					}
					fos.close();
					status = Constant.Msg.REQUEST_SUCCESS;
				} else {
					status = Constant.Msg.NETWORK_ERROR;
				}
			}catch (Exception e) {
				status = Constant.Msg.REQUEST_FAILED;
			}
			responseData.putInt(Constant.STATUS_KEY, status);
			responseData.putInt(Constant.REQUEST_MSG_KEY, Constant.Msg.DOWNLOAD_OVER);
			return responseData;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			Log.e(TAG, "download-progress = " + values[0] + "%");
			if (mOnDownloadCompeleteListener != null && values[0] == 100) {
				mOnDownloadCompeleteListener.onDownloadComplete();
			}
		}

		protected void onPostExecute(Bundle result) {
			if(result != null){
				DataObservable.getInstance().setData(result);
			}else{
				requestDownloadData();
			}
		}
	}

}