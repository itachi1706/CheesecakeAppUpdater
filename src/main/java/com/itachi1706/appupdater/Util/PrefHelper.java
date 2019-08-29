package com.itachi1706.appupdater.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Created by Kenneth on 19/12/2018.
 * for com.itachi1706.appupdater.Util in CheesecakeUtilities
 */
public class PrefHelper {

    /**
     * Wrapper to {@link PreferenceManager#getDefaultSharedPreferences(Context)} without invoking StrictMode Disk Read Policy Violation
     * @param context Context object
     * @return SharedPreference singleton object
     */
    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                .permitDiskReads()
                .build());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        StrictMode.setThreadPolicy(old);
        return sp;
    }

    /**
     * Check if night mode is enabled
     * @param context Activity Context
     * @return false if Night mode is disabled, true otherwise
     */
    public static boolean isNightModeEnabled(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode != Configuration.UI_MODE_NIGHT_NO;
    }

    /**
     * Set Night Mode Theme. Options include:
     * {@link AppCompatDelegate#MODE_NIGHT_NO}
     * {@link AppCompatDelegate#MODE_NIGHT_YES}
     * {@link AppCompatDelegate#MODE_NIGHT_FOLLOW_SYSTEM}
     * {@link AppCompatDelegate#MODE_NIGHT_AUTO}
     * {@link AppCompatDelegate#MODE_NIGHT_AUTO_BATTERY}
     * @param newTheme New Theme setting
     * @param themeName THeme name for logging purposes
     */
    public static void changeDarkModeTheme(int newTheme, String themeName) {
        Log.i("AppThemeChanger", "Switching over to " + themeName + " mode");
        AppCompatDelegate.setDefaultNightMode(newTheme);
    }

    /**
     * Does the handling of theme changes natively
     * @param switchedTheme Either of the values "light", "dark", "battery" or "default"
     */
    public static void handleDefaultThemeSwitch(String switchedTheme) {
        switch (String.valueOf(switchedTheme)) {
            case "light": changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_NO, "Light");break;
            case "dark": changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_YES, "Dark");break;
            case "battery": changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, "Battery Saver");break;
            case "default": changeDarkModeTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, "System Default");break;
            default:
                // Set as battery saver default if P and below
                changeDarkModeTheme((Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) ? AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                        "Unknown mode, falling back to default");
                break;
        }
    }
}
