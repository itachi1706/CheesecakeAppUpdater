package com.itachi1706.appupdater.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

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
}
