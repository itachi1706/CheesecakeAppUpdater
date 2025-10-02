package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.itachi1706.appupdater.internal.AppUpdateChecker;
import com.itachi1706.appupdater.internal.PathBasedAppUpdateChecker;
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

    /**
     * Use path based API (v2) or query based API (v1 - legacy)
     * This is currently false by default for compatibility reasons.
     * We will move it to true by default in the next major release
     * At that time the old method will be deprecated
     */
    private boolean usePathBasedApi = false;

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
     * Use path based API (v2) or query based API (v1 - legacy)
     * @param enabled true to use path based API, false to use query based API
     * @return The instance itself
     */
    public AppUpdateInitializer setPathBasedApi(boolean enabled) {
        this.usePathBasedApi = enabled;
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
        if (this.checkSideload && (!ValidationHelper.checkSideloaded(mActivity))) {
            Log.i("Updater", "App is not sideloaded, disabling update check");
            return;
        }
        if (this.wifiCheck) {
            if (UpdaterHelper.canCheckUpdate(sp, mActivity)) {
                update(mActivity);
            }
            return;
        }
        update(mActivity);
    }

    private void update(Activity mActivity) {
        Log.i("Updater", "Checking for new updates...");
        if (!usePathBasedApi) {
            Log.d("Updater", "Using Query Based API");
            new AppUpdateChecker(mActivity, sp, true, mNotificationIcon, baseURL, fullscreen, internalCache).executeOnExecutor();
        } else {
            Log.d("Updater", "Using Path Based API");
            PathBasedAppUpdateChecker chk = new PathBasedAppUpdateChecker(mActivity.getApplicationContext(), sp, true, mNotificationIcon,
                    "version-changelog", baseURL, fullscreen, internalCache);
            chk.checkForUpdates();
        }
    }
}
