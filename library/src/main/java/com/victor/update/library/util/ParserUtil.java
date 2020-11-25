package com.victor.update.library.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.victor.update.library.data.UpdateData;
import com.victor.update.library.model.DownLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2020-2080, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: ParserUtil
 * Author: Victor
 * Date: 2020/11/25 14:24
 * Description:
 * -----------------------------------------------------------------
 */
public class ParserUtil {
    private static String TAG = "ParserUtil";

    public static UpdateData parse(Context context,String result) {
        Log.e(TAG,"parse()......");
        if (context == null) return null;
        if (TextUtils.isEmpty(result)) return null;

        UpdateData updateData = null;
        JSONObject data = null;
        try {
            data = new JSONObject(result);
            if (data != null) {
                updateData = new UpdateData();
                updateData.appName = data.optString("name");
                updateData.versionName = data.optString("versionShort");
                updateData.versionCode = data.optInt("version");
                updateData.updateContent = data.optString("changelog");
                updateData.url = data.optString("install_url");
//                updateData.url = "http://192.168.2.37:8080/data/LongTV_v1.2.0_22_main.apk";

                String path = FileUtils.getExRootFolder(context).getAbsolutePath() + File.separator;
                updateData.path = path;

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"parse data error l!!!!!!");
            Log.e(TAG,"result = " + result);
        }
        return updateData;
    }
}
