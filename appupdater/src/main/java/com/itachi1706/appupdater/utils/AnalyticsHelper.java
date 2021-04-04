package com.itachi1706.appupdater.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.itachi1706.appupdater.object.CAAnalytics;
import com.itachi1706.helperlib.helpers.PrefHelper;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * Created by Kenneth on 17/3/2018.
 * for com.itachi1706.appupdater.Util in CheesecakeUtilities
 */

public class AnalyticsHelper {

    private final SharedPreferences mSharedPreference;
    private final boolean mDefaultMode;
    @Nullable private PackageInfo pInfo;
    private static final String ANALYTICS_PREF = "ca_analytics";

    /**
     * Creates the Analytics Helper object
     * @param context Application Context
     * @param defaultMode Whether it should be enabled by default or not
     */
    public AnalyticsHelper(Context context, boolean defaultMode) {
        this.mSharedPreference = PrefHelper.getDefaultSharedPreferences(context);
        this.mDefaultMode = defaultMode;
        try {
            this.pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            this.pInfo = null;
        }
    }

    /**
     * Check if analytics is enabled
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return mSharedPreference.contains(ANALYTICS_PREF) && mSharedPreference.getBoolean(ANALYTICS_PREF, mDefaultMode); // Manual opt out
    }

    /**
     * @deprecated Use {@link #getData(boolean)} instead
     * @return Analytics data or null if not enabled
     */
    @Nullable
    @WorkerThread
    public CAAnalytics getData() {
        return getData(false);
    }


    /**
     * Get relevant data if analytics is not disabled. You can choose which of the data to create as custom properties
     * Remeber to follow https://firebase.google.com/docs/analytics/android/properties#set_user_properties for more information if using Firebase
     * Sample user properties fields: debug_mode, device_manufacturer, device_model, device_codename, device_fingerprint, device_cpu_abi,
     * device_tags, app_version_code, app_version, android_sdk_version, android_version, android_sec_patch
     * @return Analytics data or null if not enabled
     */
    @Nullable
    @WorkerThread
    public CAAnalytics getData(boolean debugMode) {
        if (!isEnabled()) return null;

        //Debug Info Get
        String version = "Unknown";
        int versionCode = 0;
        if (pInfo != null) {
            version = pInfo.versionName;
            versionCode = pInfo.versionCode;
        }
        boolean newAbiData = false;

        // Generate data
        CAAnalytics analytics = new CAAnalytics();
        analytics.setDebug(debugMode);
        analytics.setdManufacturer(Build.MANUFACTURER);
        analytics.setdName(Build.MODEL);
        analytics.setAppVerCode(versionCode);
        analytics.setAppVer(version);
        analytics.setSdkString(Build.VERSION.RELEASE);
        analytics.setSdkver(Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String[] support = Build.SUPPORTED_ABIS;
            if (support.length > 0) {
                analytics.setdCPU(support[0]);
                newAbiData = true;
            }
        }
        if (!newAbiData) analytics.setdCPU(Build.CPU_ABI);
        analytics.setdFingerprint(Build.FINGERPRINT);
        analytics.setdTags(Build.TAGS);
        analytics.setdCodename(Build.PRODUCT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            analytics.setSdkPatch(Build.VERSION.SECURITY_PATCH);
        else analytics.setSdkPatch("1970-01-01");
        return analytics;
    }
}
