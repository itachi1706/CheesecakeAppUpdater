package com.itachi1706.appupdater.object;

import androidx.annotation.Keep;

import kotlinx.serialization.Serializable;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.Objects in AppUpdater
 */
@SuppressWarnings("unused")
@Keep
@Serializable
public final class AppUpdateMessageObject {
    private int index;
    private String id;
    private String appid;
    private String updateText;
    private String dateModified;
    private String versionCode;
    private String versionName;
    private String labels;
    private String url;

    public AppUpdateMessageObject() {
        // JSON Constructor
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getAppid() {
        return appid;
    }

    public String getUpdateText() {
        return updateText;
    }

    public String getDateModified() {
        return dateModified;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getLabels() {
        return labels;
    }

    public String getUrl() {
        return url;
    }
}
