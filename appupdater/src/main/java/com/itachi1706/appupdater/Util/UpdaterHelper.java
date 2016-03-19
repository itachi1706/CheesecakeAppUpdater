package com.itachi1706.appupdater.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Objects.AppUpdateMessageObject;
import com.itachi1706.appupdater.Objects.AppUpdateObject;

/**
 * Created by Kenneth on 3/3/2016.
 * For com.itachi1706.appupdate.Util in AppUpdater
 */
public class UpdaterHelper {

    public static int HTTP_QUERY_TIMEOUT = 15000; //15 seconds timeout

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
     * NOTE: This requires you to have a "updatewifi" checkbox preference to utilize
     * @param sp Shared Preference of the Application to get "updatewifi" check from
     * @param context The application context
     * @return True if app can check for updates, false otherwise
     */
    public static boolean canCheckUpdate(SharedPreferences sp, Context context) {
        if (sp.getBoolean("updatewifi", false) && !ConnectivityHelper.isWifiConnection(context)) {
            Log.i("Updater", "Not on WIFI, Ignore Update Checking");
            return false;
        }

        if (!ConnectivityHelper.hasInternetConnection(context)) {
            Log.w("Updater", "No internet connection, skipping WiFi checking");
            return false;
        }
        return true;
    }

    public static void settingGenerateChangelog(SharedPreferences sp, Activity activity) {
        settingGenerateChangelog(sp, "version-changelog", activity);
    }

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
                        .setMessage(Html.fromHtml(message)).setPositiveButton("Close", null).show();
            }
        }
    }

}
