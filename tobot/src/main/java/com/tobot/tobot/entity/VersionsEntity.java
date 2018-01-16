package com.tobot.tobot.entity;

/**
 * Created by Javen on 2018/1/9.
 */

public class VersionsEntity {
    private int versionCode;
    private String versionName;
    private String apk;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }
}
