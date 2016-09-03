package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.appupdater.internal.AppUpdateChecker;

/**
 * Created by Kenneth on 3/9/2016.
 * for com.itachi1706.appupdater in CheesecakeUtilities
 */
public final class AppUpdateInitializer {

    private Activity mActivity;
    private SharedPreferences sp;
    private int mNotificationIcon;
    private String baseURL;

    public AppUpdateInitializer(Activity mActivity, SharedPreferences sp, int mNotificationIcon, String baseURL) {
        this.mActivity = mActivity;
        this.sp = sp;
        this.mNotificationIcon = mNotificationIcon;
        this.baseURL = baseURL;
    }

    public void checkForUpdate(boolean doOnlyOnWifiCheck) {
        if (doOnlyOnWifiCheck) {
            if (UpdaterHelper.canCheckUpdate(sp, mActivity)) {
                update();
            }
            return;
        }
        update();
    }

    private void update() {
        Log.i("Updater", "Checking for new updates...");
        new AppUpdateChecker(mActivity, sp, true, mNotificationIcon, baseURL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
