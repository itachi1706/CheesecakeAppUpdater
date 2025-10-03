package com.itachi1706.appupdater.internal

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.itachi1706.appupdater.NewUpdateActivity
import com.itachi1706.appupdater.R
import com.itachi1706.appupdater.`object`.AppUpdateObject
import com.itachi1706.appupdater.utils.MigrationHelper
import com.itachi1706.appupdater.utils.UpdaterHelper
import com.itachi1706.helperlib.deprecation.HtmlDep.fromHtml
import com.itachi1706.helperlib.helpers.ApiCallsHelper
import com.itachi1706.helperlib.objects.ApiResponse
import com.itachi1706.helperlib.objects.HttpRequest
import kotlinx.serialization.json.Json
import kotlin.random.Random

class PathBasedAppUpdateChecker @JvmOverloads constructor(
    val context: Context,
    val sp: SharedPreferences,
    val isMain: Boolean = false,
    val notificationIcon: Int,
    val changelogLocation: String = "version-changelog",
    val baseUrl: String = "https://api.itachi1706.com/v1/",
    val path: String? = null,
    val fullScreen: Boolean,
    val internalCache: Boolean
) : ApiCallsHelper.ApiCallListener {

    companion object {
        const val TAG = "Updater-Path"
    }

    private var localVersionCode = 0L
    private var packageName = ""

    fun checkForUpdates() {
        val pInfo = try {
            context.applicationContext.packageManager.getPackageInfo(
                context.applicationContext.packageName,
                0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get package info: Error: ${e.message}", e)
            null
        }
        packageName = pInfo?.packageName ?: ""
        localVersionCode = MigrationHelper.getVersionCodeCompat(pInfo)

        val myPath = if (path.isNullOrEmpty()) {
            "app-updates/apps/$packageName/info"
        } else {
            path
        }

        ApiCallsHelper.Builder(context)
            .setBaseUrl(baseUrl)
            .setPath(myPath)
            .setMethod(HttpRequest.Method.GET)
            .setCallback(this)
            .setDefaultAuthentication(true)
            .makeRequest()
    }

    override fun onApiCallSuccess(response: ApiResponse) {
        if (!response.success) {
            Log.e(TAG, "Error during API Call: Error: ${response.error}")
            return
        }

        // FUTURE: When we fully remove the old updater, we can set id to Int in AppUpdateObject directly
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val updater = response.getTypedData<AppUpdateObject>(json)
        if (updater == null) {
            Log.e(TAG, "Error during API Call: Unable to parse response")
            return
        }

        val serverVersion = try {
            updater.latestVersionCode?.toLong() ?: 0L
        } catch (_: NumberFormatException) {
            Log.w(TAG, "Failed to get server version. Default to 0")
            0L
        }

        processRestOfData(serverVersion, json, updater)
    }

    override fun onApiCallError(error: String) {
        Log.e(TAG, "Error during API Call: $error")
    }

    private fun processRestOfData(serverVersion: Long, json: Json, updater: AppUpdateObject) {
        Log.i(TAG, "Server: $serverVersion | Local: $localVersionCode")

        sp.edit { putString(changelogLocation, json.encodeToString(updater)) }

        if (localVersionCode >= serverVersion) {
            Log.i(TAG, "App is on the latest version")
            if (!isMain) {
                AlertDialog.Builder(context).setTitle(R.string.dialog_title_latest_update)
                    .setMessage(R.string.dialog_message_latest_update)
                    .setNegativeButton(R.string.dialog_action_positive_close, null).show()
            }
            return
        }

        if (updater.updateMessage.isEmpty()) {
            Log.e(TAG, "No Update Messages")
            return
        }

        // Parse Message
        val updateLink = updater.updateMessage[0].url
        Log.i(TAG, "Update Found! Generating update message. Latest Version: ${updater.latestVersion} (${updater.latestVersionCode})")
        val message = buildString {
            append("Latest Version: ${updater.latestVersion}<br/><br/>")
            append(UpdaterHelper.getChangelogStringFromArray(updater.updateMessage))
        }
        if (fullScreen && Build.VERSION.SDK_INT <= Build.VERSION_CODES.BAKLAVA) {
            // Full screen updater
            Log.d(TAG, "Launching Full Screen Updater Activity")
            val intent = Intent(context, NewUpdateActivity::class.java)
            intent.apply {
                putExtra("update", json.encodeToString(updater))
                putExtra("nicon", notificationIcon)
                putExtra("internalCache", internalCache)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    context.packageManager.canRequestPackageInstalls()
                    Log.i(TAG, "REQUEST_INSTALL_PACKAGES granted, showing dialog")
                } catch (_: SecurityException) {
                    Log.e(TAG, "REQUEST_INSTALL_PACKAGES not granted, showing warning dialog")
                    AlertDialog.Builder(context)
                        .setTitle(R.string.no_perm_package_install_dialog_title)
                        .setMessage(R.string.no_perm_package_install_dialog_message)
                        .setPositiveButton(R.string.dialog_action_positive_close, null)
                        .show()
                    return
                }
            }

            AlertDialog.Builder(context).setTitle("A New Update is Available!")
                .setMessage(fromHtml(message))
                .setNegativeButton("Don't Update", null)
                .setPositiveButton("Update") { _, _ ->
                    val manager = NotificationManagerCompat.from(context)
                    // Create notification channel
                    val mChannel = NotificationChannelCompat.Builder(
                        UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL,
                        NotificationManagerCompat.IMPORTANCE_LOW
                    )
                        .setName("App Updates")
                        .setDescription("Notifications when updating the application")
                        .setLightsEnabled(true)
                        .setLightColor(Color.GREEN)
                        .build()
                    manager.createNotificationChannel(mChannel)

                    val mBuilder = NotificationCompat.Builder(
                        context,
                        UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL
                    )
                        .setContentTitle(context.getString(R.string.notification_title_starting_download))
                        .setContentText(context.getString(R.string.notification_content_starting_download))
                        .setProgress(0, 0, true).setSmallIcon(notificationIcon)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setTicker(context.getString(R.string.notification_ticker_starting_download))
                    val notificationId = Random.nextInt()
                    manager.notify(notificationId, mBuilder.build())
                    DownloadLatestUpdate(
                        context,
                        mBuilder,
                        manager,
                        notificationId,
                        notificationIcon,
                        internalCache
                    ).executeOnExecutor(updateLink)
                }.show()
        }
    }
}