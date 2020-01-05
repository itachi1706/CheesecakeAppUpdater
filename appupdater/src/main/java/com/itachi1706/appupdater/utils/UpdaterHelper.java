package com.itachi1706.appupdater.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.appupdater.object.AppUpdateMessageObject;
import com.itachi1706.appupdater.object.AppUpdateObject;
import com.itachi1706.helperlib.deprecation.HtmlDep;
import com.itachi1706.helperlib.helpers.ConnectivityHelper;

/**
 * Created by Kenneth on 3/3/2016.
 * For com.itachi1706.appupdate.Util in AppUpdater
 */
public final class UpdaterHelper {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout
    public static String UPDATER_NOTIFICATION_CHANNEL = "app_update_channel";

    private UpdaterHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves the changelog of the device
     * @param changelog List of Changelog parsed
     * @return Changelog
     */
    public static String getChangelogStringFromArray(AppUpdateMessageObject[] changelog){
        StringBuilder changelogBuilder = new StringBuilder();
        for (AppUpdateMessageObject obj : changelog) {
            changelogBuilder.append("<b>Changelog for ").append(obj.getVersionName());
            // Get Labels
            changelogBuilder.append(" ").append(obj.getLabels()).append("</b><br/>");

            changelogBuilder.append(obj.getUpdateText().replace("\r\n", "<br/>"));
            changelogBuilder.append("<br/><br/>");
        }
        return changelogBuilder.toString();
    }

    /**
     * Determines if an app can check for update
     * NOTE: This requires you to have a "updateOnWifi" checkbox preference to utilize
     * Edited 3/9: The utility will now handle this on its own assuming you invoke {@link com.itachi1706.appupdater.SettingsInitializer}
     * @param sp Shared Preference of the Application to get "updatewifi" check from
     * @param context The application context
     * @return True if app can check for updates, false otherwise
     */
    public static boolean canCheckUpdate(SharedPreferences sp, Context context) {
        final String TAG = "Updater";
        if (sp.getBoolean("updateOnWifi", false) && !ConnectivityHelper.isWifiConnection(context)) {
            Log.i(TAG, "Not on WIFI, Ignore Update Checking");
            return false;
        }

        if (!ConnectivityHelper.hasInternetConnection(context)) {
            Log.w(TAG, "No internet connection, skipping WiFi checking");
            return false;
        }

        if (ConnectivityHelper.shouldThrottle(context)) {
            Log.w(TAG, "Currently on metered connection with Data Saver enabled, skipping check");
            return false;
        }
        return true;
    }

    /**
     * Generate Alert Dialog showing Changelogs
     * @param sp Shared Preference Manager object
     * @param activity Activity in which this method is called from
     */
    public static void settingGenerateChangelog(SharedPreferences sp, Activity activity) {
        settingGenerateChangelog(sp, "version-changelog", activity);
    }

    /**
     * Generate Alert Dialog showing App Changelogs
     * @param sp Shared Preference Manager object
     * @param prefName Preference Key Name
     * @param activity Activity in which the method is called from
     */
    public static void settingGenerateChangelog(SharedPreferences sp, String prefName, Activity activity) {
        String changelog = sp.getString(prefName, "l");
        if (changelog.equals("l")) {
            //Not available
            new AlertDialog.Builder(activity).setTitle("No Changelog")
                    .setMessage("No changelog was found. Please check if you can connect to the server")
                    .setPositiveButton(android.R.string.ok, null).show();
        } else {
            Gson gson = new Gson();
            AppUpdateObject updater = gson.fromJson(changelog, AppUpdateObject.class);
            if (updater.getUpdateMessage().length == 0) {
                new AlertDialog.Builder(activity).setTitle("No Changelog")
                        .setMessage("No changelog was found. Please check if you can connect to the server")
                        .setPositiveButton(android.R.string.ok, null).show();
            } else {
                String message = "Latest Version: " + updater.getLatestVersion() + "<br /><br />";
                message += UpdaterHelper.getChangelogStringFromArray(updater.getUpdateMessage());

                new AlertDialog.Builder(activity).setTitle("Changelog")
                        .setMessage(HtmlDep.fromHtml(message)).setPositiveButton("Close", null).show();
            }
        }
    }

}
