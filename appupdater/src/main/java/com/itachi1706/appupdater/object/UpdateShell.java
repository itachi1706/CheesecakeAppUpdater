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
public final class UpdateShell {
    private AppUpdateObject msg;
    private int error;

    public UpdateShell() {
        // JSON Constructor
    }

    public AppUpdateObject getMsg() {
        return msg;
    }

    public int getError() {
        return error;
    }
}
