package com.itachi1706.appupdater.object;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.Objects in AppUpdater
 */
@SuppressWarnings("unused")
public final class UpdateShell {
    private AppUpdateObject msg;
    private int error;

    public UpdateShell() {
    }

    public AppUpdateObject getMsg() {
        return msg;
    }

    public int getError() {
        return error;
    }
}
