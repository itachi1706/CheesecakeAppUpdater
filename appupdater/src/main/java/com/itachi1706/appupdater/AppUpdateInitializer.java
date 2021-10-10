package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.itachi1706.appupdater.internal.AppUpdateChecker;
import com.itachi1706.appupdater.utils.UpdaterHelper;
import com.itachi1706.helperlib.helpers.ValidationHelper;

import java.lang.ref.WeakReference;

/**
 * Created by Kenneth on 3/9/2016.
 * for com.itachi1706.appupdater in CheesecakeUtilities
 */
@SuppressWarnings("unused")
public final class AppUpdateInitializer {

    private final WeakReference<Activity> mActivityRef;
    private final SharedPreferences sp;
    private final int mNotificationIcon;
    private final String baseURL;
    private boolean fullscreen = false;
    private boolean internalCache = false;
    private boolean wifiCheck = false;
    private boolean checkSideload = true;

    public AppUpdateInitializer(Activity mActivity, SharedPreferences sp, int mNotificationIcon, String baseURL) {
        this.mActivityRef = new WeakReference<>(mActivity);
        this.sp = sp;
        this.mNotificationIcon = mNotificationIcon;
        this.baseURL = baseURL;
    }

    public AppUpdateInitializer(Activity mActivity, SharedPreferences sp, int mNotificationIcon, String baseURL, boolean fullscreen) {
        this.mActivityRef = new WeakReference<>(mActivity);
        this.sp = sp;
        this.mNotificationIcon = mNotificationIcon;
        this.baseURL = baseURL;
        this.fullscreen = fullscreen;
    }

    /**
     * Sets a flag to store the update apk file in the internal cache instead of external
     * Internal: /data/data/packagename/cache/download
     * External: /sdcard/Android/data/packagename/cache/download
     *
     * @param storeInInternalCache true to store in internal cache, false to store in external cache
     * @return The object to allow chaining
     */
    public AppUpdateInitializer storeUpdateApkInInternalCache(boolean storeInInternalCache) {
        this.internalCache = storeInInternalCache;
        return this;
    }

    /**
     * Sets a flag to allow checking for updates on wifi only
     *
     * @param onlyOnWifi true to only check on wifi, false otherwise
     * @return The object to allow chaining
     */
    public AppUpdateInitializer setOnlyOnWifiCheck(boolean onlyOnWifi) {
        this.wifiCheck = onlyOnWifi;
        return this;
    }

    /**
     * Sets a flag to allow checking for updates only on sideloaded installs
     * This is true by default to prevent Google Play from flagging it
     * @param onlyCheckSideloaded true to only check sideloaded APKs, false otherwise
     * @return The object to allow chaining
     */
    public AppUpdateInitializer setCheckOnlyForSideloadedInstalls(boolean onlyCheckSideloaded) {
        this.checkSideload = onlyCheckSideloaded;
        return this;
    }

    public void checkForUpdate() {
        Activity mActivity = mActivityRef.get();
        if (mActivity == null) {
            return; // NO-OP
        }
        if (this.checkSideload) {
            if (!ValidationHelper.checkSideloaded(mActivity)) {
                Log.i("Updater", "App is not sideloaded, disabling update check");
                return;
            }
        }
        if (this.wifiCheck) {
            if (UpdaterHelper.canCheckUpdate(sp, mActivity)) {
                update(mActivity);
            }
            return;
        }
        update(mActivity);
    }

    /**
     * Checks for Update if the app is not sideloaded
     *
     * @deprecated Use {@link AppUpdateInitializer#checkForUpdate()} instead and the various flag setters to set wifi check
     * @param doOnlyOnWifiCheck If the app should only check for update on WIFI
     */
    @Deprecated
    public void checkForUpdate(boolean doOnlyOnWifiCheck) {
        checkForUpdate(doOnlyOnWifiCheck, true);
    }

    /**
     * Checks For Update
     * @deprecated Use {@link AppUpdateInitializer#checkForUpdate()} instead and the various flag setters to set wifi and sideload checks
     * @param doOnlyOnWifiCheck If the app should only check for update on WIFI
     * @param onlyDoCheckForSideloadInstalls If the app should only check for update if the app is sideloaded
     */
    @Deprecated
    public void checkForUpdate(boolean doOnlyOnWifiCheck, boolean onlyDoCheckForSideloadInstalls) {
        this.wifiCheck = doOnlyOnWifiCheck;
        this.checkSideload = onlyDoCheckForSideloadInstalls;
        checkForUpdate();
    }

    private void update(Activity mActivity) {
        Log.i("Updater", "Checking for new updates...");
        new AppUpdateChecker(mActivity, sp, true, mNotificationIcon, baseURL, fullscreen, internalCache).executeOnExecutor();
    }
}
