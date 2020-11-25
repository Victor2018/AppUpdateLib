package com.victor.update.library.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2020-2080, by Victor, All rights reserved.
 * -----------------------------------------------------------------
 * File: FileUtils
 * Author: Victor
 * Date: 2020/11/24 18:19
 * Description:
 * -----------------------------------------------------------------
 */
public class FileUtils {
    /**
     * 本地的主文件夹
     */
    public static final String ROOT_FOLDER = "flash";

    /**
     * Uri路径
     *
     * @param uri
     */
    public static long getFileSize(Uri uri) {
        if (uri == null)
            return 0;
        File file = new File(uri.getPath());
        long size = file.length();
        return size;
    }

    /**
     * Uri路径
     *
     * @param path
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path))
            return 0;
        File file = new File(path);
        long size = file.length();
        return size;
    }

    /**
     * 获取
     *
     * @return
     */
    public static File getExRootFolder(Context context) {
        File rootFolder = null;
        if (hasMountedSDCard()) {
            rootFolder = new File(getRootDir(context) + File.separator + ROOT_FOLDER);
            if (!rootFolder.exists())
                rootFolder.mkdirs();
        }
        return rootFolder;
    }

    public static String getRootDir(Context context) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = context.getFilesDir().getAbsolutePath();
        }
        return path;
    }

    /**
     * 获取外部指定目录
     *
     * @param folder
     * @return
     */
    public static File getExFolder(Context context,String folder) {
        File targetFolder = getExRootFolder(context);
        if (targetFolder != null) {
            targetFolder = new File(targetFolder.getAbsolutePath() + File.separator + folder);
            if (!targetFolder.exists())
                targetFolder.mkdirs();
        }
        return targetFolder;
    }

    /**
     * 获取外部指定目录
     *
     * @param folder
     * @return
     */
    public static String getExFolderStr(Context context,String folder) {
        File targetFolder = getExRootFolder(context);
        if (targetFolder != null) {
            targetFolder = new File(targetFolder.getAbsolutePath() + File.separator + folder);
            if (!targetFolder.exists())
                targetFolder.mkdirs();
        }
        return targetFolder.getAbsolutePath();
    }

    /**
     * 获取外部指定目录
     *
     * @param filename
     * @return
     */
    public static File getExFolderFile(Context context,String folder, String filename) {
        File targetFolder = getExFolder(context,folder);
        if (targetFolder != null) {
            targetFolder = new File(targetFolder.getAbsolutePath() + File.separator + filename);
        }
        return targetFolder;
    }

    public static String getFileCacheDir(Context context) {
        if (context == null || !hasMountedSDCard())
            return null;
        File file = context.getExternalFilesDir(null);
        return file != null ? file.getAbsolutePath() : null;
    }

    public static String getCacheDir(Context context) {
        if (context == null || !hasMountedSDCard())
            return null;
        return context.getExternalCacheDir().getAbsolutePath();
    }

    /**
     * 读取文件内容
     *
     * @param path
     * @return
     */
    public static String getStringFromFile(String path) {
        try {
            int len;
            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[8 * 1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            String result = new String(baos.toByteArray());
            baos.close();
            fis.close();
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否有SD卡
     *
     * @return
     */
    public static boolean hasMountedSDCard() {
        return android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 是否有多余的使用空间
     *
     * @param file
     * @param targetSize
     * @return
     */
    private static boolean hasUsableSpace(File file, long targetSize) {
        return file != null && file.getUsableSpace() > targetSize;
    }
}
