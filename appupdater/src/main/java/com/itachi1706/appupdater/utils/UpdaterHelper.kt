package com.itachi1706.appupdater.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log.i
import android.util.Log.w
import com.itachi1706.appupdater.`object`.AppUpdateMessageObject
import com.itachi1706.appupdater.`object`.AppUpdateObject
import com.itachi1706.helperlib.deprecation.HtmlDep
import com.itachi1706.helperlib.helpers.ConnectivityHelper
import kotlinx.serialization.json.Json

object UpdaterHelper {

    const val HTTP_QUERY_TIMEOUT = 15000 // 15 seconds timeout
    const val UPDATER_NOTIFICATION_CHANNEL = "app_update_channel"

    /**
     * Retrieves the changelog of the app
     * @param changelog Array of [AppUpdateMessageObject] containing the changelog information
     * @return Formatted changelog in HTML format
     */
    @JvmStatic
    fun getChangelogStringFromArray(changelog: Array<AppUpdateMessageObject>): String {
        val changelogBuilder = StringBuilder()
        for (obj in changelog) {
            changelogBuilder.append("<b>Changelog for ${obj.versionName}")
            // Get labels
            changelogBuilder.append(" ${obj.labels}</b><br/>")

            changelogBuilder.append(obj.updateText?.replace(Regex("\\r\\n|\\r|\\n"), "<br/>"))
            changelogBuilder.append("<br/><br/>")
        }
        return changelogBuilder.toString()
    }

    /**
     * Check if we can check for updates based on user preferences and connectivity
     * Note: Requires "updateOnWifi" preference to be set in [SharedPreferences]
     * @param sp [SharedPreferences] containing user preferences
     * @param context [Context] to check connectivity
     * @return True if we can check for updates, false otherwise
     */
    @JvmStatic
    fun canCheckUpdate(sp: SharedPreferences, context: Context): Boolean {
        val tag = "Updater"
        if (sp.getBoolean("updateOnWifi", false) && !ConnectivityHelper.isWifiConnection(context)) {
            i(tag, "Not on WiFi, skipping update check")
            return false
        }

        if (!ConnectivityHelper.hasInternetConnection(context)) {
            w(tag, "No internet connection, skipping update check")
            return false
        }

        if (ConnectivityHelper.shouldThrottle(context)) {
            w(tag, "Currently on metered connection via Data Saver, skipping checks")
            return false
        }

        return true
    }

    /**
     * Check if user wants to see changelog dialog on app update
     * @param sp [SharedPreferences] containing user preferences
     * @param activity [Activity] to show changelog dialog
     */
    @JvmStatic
    fun settingGenerateChangelog(sp: SharedPreferences, activity: Activity) {
        settingGenerateChangelog(sp, "version-changelog", activity)
    }

    /**
     * Generate Alert Dialog showing changelog
     * @param sp [SharedPreferences] containing user preferences
     * @param prefName Preference name in [SharedPreferences] where changelog is stored
     * @param activity [Activity] to show changelog dialog
     */
    @JvmStatic
    fun settingGenerateChangelog(sp: SharedPreferences, prefName: String, activity: Activity) {
        val changelog = sp.getString(prefName, "l") ?: "l"
        if (changelog == "l") {
            // Not available
            AlertDialog.Builder(activity).setTitle("No Changelog")
                .setMessage("No changelog found. Please check if you can connect to the server or if the changelog is available.")
                .setPositiveButton(android.R.string.ok, null).show()
        } else {
            val json = Json {ignoreUnknownKeys = true}
            val updater = json.decodeFromString<AppUpdateObject>(changelog)
            if (updater.updateMessage.size == 0) {
                AlertDialog.Builder(activity).setTitle("No Changelog")
                    .setMessage("No changelog found. Please check if you can connect to the server or if the changelog is available.")
                    .setPositiveButton(android.R.string.ok, null).show()
            } else {
                var message = "Latest Version: ${updater.latestVersion}<br/><br/>"
                message += UpdaterHelper.getChangelogStringFromArray(updater.updateMessage)

                AlertDialog.Builder(activity).setTitle("Changelog")
                    .setMessage(HtmlDep.fromHtml(message)).setPositiveButton("Close", null).show()
            }
        }
    }
}