package com.itachi1706.appupdater.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * This is just a basic helper class to help with migration
 * The methods here will eventually head over to <a href="https://github.com/itachi1706/AndroidHelperLib>AndroidHelperLib</a> instead
 */
@SuppressWarnings("deprecation")
public class MigrationHelper {

    private MigrationHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static long getVersionCodeCompat(PackageInfo packageInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return packageInfo.getLongVersionCode();
        } else {
            return packageInfo.versionCode;
        }
    }

    public static DisplayMetrics getDisplayMetricsCompat(Context context, WindowManager windowManager) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.getDisplay().getRealMetrics(metrics);
        } else {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }
        return metrics;
    }
}
