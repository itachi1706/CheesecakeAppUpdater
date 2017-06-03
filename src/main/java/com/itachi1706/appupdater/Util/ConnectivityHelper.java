package com.itachi1706.appupdater.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

/**
 * Created by Kenneth on 3/3/2016.
 * For com.itachi1706.appupdate.Util in AppUpdater
 * NOTE: Requires android.permission.ACCESS_NETWORK_STATE permission
 */
@SuppressWarnings("unused")
public final class ConnectivityHelper {
    
    public static final int NO_CONNECTION = -1;

    /**
     * Gets the Network Info Object
     * @param context Context
     * @return Network Info
     */
    @Nullable
    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = getConnectivityManager(context);

        return cm.getActiveNetworkInfo();
    }
    
    private static int getNetworkType(NetworkInfo networkInfo) {
        if (networkInfo == null) return NO_CONNECTION;
        else return networkInfo.getType();
    }

    /**
     * Gets the Connectivity Manager object
     * @param context Context
     * @return Connectivity Manager
     */
    private static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Check if the android device has Internet
     * @param context Context
     * @return True if internet, false otherwise
     */
    public static boolean hasInternetConnection(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Check if connection is a WIFI connection
     * @param context Context
     * @return True if WIFI, false otherwise
     */
    public static boolean isWifiConnection(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork != null && getNetworkType(activeNetwork) == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Check if connection is Mobile Data
     * @param context Context
     * @return True if Cellular (Mobile), false otherwise
     */
    public static boolean isCellularConnection(Context context) {
        NetworkInfo activeNetwork = getNetworkInfo(context);
        return activeNetwork != null && getNetworkType(activeNetwork) == ConnectivityManager.TYPE_MOBILE;
    }

    /**
     * Get the current active network type
     * @param context Context
     * @return Active Netwrok Type (ConnectivityManager.TYPE_WIFI etc.)
     */
    public static int getActiveNetworkType(Context context) {
        return getNetworkType(getNetworkInfo(context));
    }

    /**
     * Whether the app should throttle usage of data
     * This is really for devices running API 24 (Nougat) where there is the Data Saver option
     * @param context Context
     * @return true if it should be throttled, false otherwise
     */
    public static boolean shouldThrottle(Context context) {
        ConnectivityManager manager = getConnectivityManager(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (manager.isActiveNetworkMetered()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    switch (manager.getRestrictBackgroundStatus()) {
                        case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED: return true;
                        case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                        case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED:
                        default: return false;
                    }
                } else return false;
            } else return false;
        }
        return false;
    }

    /**
     * Gets the Data Saver Option for the application itself
     * @param context Context
     * @return int One of 3 choices
     * {RESTRICT_BACKGROUND_STATUS_ENABLED, RESTRICT_BACKGROUND_STATUS_WHITELISTED, RESTRICT_BACKGROUND_STATUS_DISABLED}
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int getDataSaverSetting(Context context) {
        ConnectivityManager manager = getConnectivityManager(context);
        return manager.getRestrictBackgroundStatus();
    }
}
