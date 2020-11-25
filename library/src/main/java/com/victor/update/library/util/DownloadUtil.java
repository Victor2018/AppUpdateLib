package com.victor.update.library.util;

import android.util.Log;

import com.victor.update.library.data.UpdateData;
import com.victor.update.library.interfaces.OnDownloadProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2020-2080, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: DownloadUtil
 * Author: Victor
 * Date: 2020/11/25 14:57
 * Description:
 * -----------------------------------------------------------------
 */
public class DownloadUtil {
    private static String TAG = "DownloadUtil";

    public static void download (UpdateData data, OnDownloadProgressListener listener) {
        long startTime = System.currentTimeMillis();
        if (data == null) return;

        int current = 0;
        int total = 0;
        URL url = null;

        try {
            url = new URL(data.url);
            URLConnection con = url.openConnection();
            total = con.getContentLength();
            InputStream is = con.getInputStream();
            File file = new File(data.path + data.appName + ".apk");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size = is.read(buffer);
            while (size != -1) {
                current += size;
                fos.write(buffer, 0, size);
                size = is.read(buffer);
                int progress = current * 100 / total;
                // 时刻将当前进度更新给onProgressUpdate方法
                Log.e(TAG,"--------------------------" + progress + "%" + "--------------------------");
                data.progress = progress;
                if (listener != null) {
                    listener.onDownloadProgress(data);
                }
            }
            fos.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long ms = System.currentTimeMillis() - startTime;
        Log.e(TAG,"download().....耗时ms = " + ms);
    }
}
