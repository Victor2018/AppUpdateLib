package com.victor.update.library.data;

/**
 * Created by victor on 2016/12/16.
 */
public class UpdateData {
    private String appName;
    private String versionName;
    private int versionCode;
    private String updateContent;
    private String url;
    private int progress;

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {

        this.progress = progress;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public String getUrl() {
        return url;
    }

    public void setAppName(String appName) {

        this.appName = appName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
