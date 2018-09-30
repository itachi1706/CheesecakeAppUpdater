package com.itachi1706.appupdater.Objects;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.Objects in AppUpdater
 */
@SuppressWarnings("unused")
public final class AppUpdateObject {
    private int index;
    private String id, packageName, appName, dateCreated, latestVersion, latestVersionCode, apptype;
    private AppUpdateMessageObject[] updateMessage;

    public AppUpdateObject() {
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getLatestVersionCode() {
        return latestVersionCode;
    }

    public String getApptype() {
        return apptype;
    }

    public AppUpdateMessageObject[] getUpdateMessage() {
        return updateMessage;
    }
}
