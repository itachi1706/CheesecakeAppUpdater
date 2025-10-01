package com.itachi1706.appupdater.internal

import android.app.Activity
import android.app.AlertDialog
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
import com.itachi1706.appupdater.`object`.UpdateShell
import com.itachi1706.appupdater.utils.MigrationHelper
import com.itachi1706.appupdater.utils.UpdaterHelper
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask
import com.itachi1706.helperlib.deprecation.HtmlDep.fromHtml
import com.itachi1706.helperlib.helpers.URLHelper
import com.itachi1706.helperlib.utils.NotifyUserUtil.createShortToast
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.random.Random

class AppUpdateChecker @JvmOverloads constructor(
    val mActivity: Activity,
    val sp: SharedPreferences,
    val isMain: Boolean = false,
    val notificationIcon: Int,
    val changelogLocation: String = "version-changelog",
    val baseUrl: String,
    val fullScreen: Boolean,
    val internalCache: Boolean
) : CoroutineAsyncTask<Unit, Unit, String>(TASK_NAME) {

    companion object {
        const val TAG = "Updater"
        val TASK_NAME = AppUpdateChecker::class.simpleName ?: "AppUpdateChecker"
    }

    override fun doInBackground(vararg params: Unit?): String {
        var url = this.baseUrl
        var packageName = ""
        var pInfo = try {
            mActivity.applicationContext.packageManager.getPackageInfo(mActivity.applicationContext.packageName, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        packageName = pInfo?.packageName ?: ""
        url += packageName
        val urlHelper = URLHelper(url)

        var tmp = ""
        try {
            tmp = urlHelper.executeString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tmp
    }

    override fun onPostExecute(result: String?) {
        Log.d(TAG, "Debug: $result")

        val json = Json { ignoreUnknownKeys = true }
        val shell = try {
            json.decodeFromString<UpdateShell>(result ?: "")
        } catch (e: SerializationException) {
            Log.e(TAG, "Invalid JSON, might not have internet")
            return
        }

        if (shell.error == 20) {
            Log.e(TAG, "Application Not Found!")
            return
        }

        val updater = shell.msg
        if (updater == null) return

        val pInfo = try {
            mActivity.applicationContext.packageManager.getPackageInfo(mActivity.applicationContext.packageName, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        val localVersion = MigrationHelper.getVersionCodeCompat(pInfo)
        val serverVersion = try {
            updater.latestVersionCode.toLong()
        } catch (_: NumberFormatException) {
            0L
        }

        Log.i(TAG, "Server: $serverVersion | Local: $localVersion")

        sp.edit { putString(changelogLocation, json.encodeToString(updater)) }

        if (localVersion >= serverVersion) {
            Log.i(TAG, "App is on the latest version")
            if (!isMain) {
                if (!mActivity.isFinishing) {
                    AlertDialog.Builder(mActivity).setTitle(R.string.dialog_title_latest_update)
                        .setMessage(R.string.dialog_message_latest_update)
                        .setNegativeButton(R.string.dialog_action_positive_close, null).show();
                } else {
                    createShortToast(
                        mActivity.applicationContext,
                        mActivity.getString(R.string.toast_message_latest_update)
                    )
                }
                return
            }

            if (updater.updateMessage.size == 0) {
                Log.e(TAG, "No Update Messages")
                return
            }

            // Parse Message
            val updateLink = updater.updateMessage[0].url
            Log.i(TAG, "Update Found! Generating update message. Latest Version: ${updater.latestVersion} (${updater.latestVersionCode})")
            var message = "Latest Version: ${updater.latestVersion}<br/><br/>"
            message += UpdaterHelper.getChangelogStringFromArray(updater.updateMessage)
            if (!mActivity.isFinishing) {
                if (fullScreen && Build.VERSION.SDK_INT <= Build.VERSION_CODES.BAKLAVA) {
                    // Full screen updater
                    Log.d(TAG, "Launching Full Screen Updater Activity")
                    val intent = Intent(mActivity, NewUpdateActivity::class.java)
                    intent.apply {
                        putExtra("update", json.encodeToString(updater))
                        putExtra("nicon", notificationIcon)
                        putExtra("internalCache", internalCache)
                    }
                    mActivity.startActivity(intent)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            mActivity.packageManager.canRequestPackageInstalls()
                            Log.i(TAG, "REQUEST_INSTALL_PACKAGES granted, showing dialog")
                        } catch (e: SecurityException) {
                            Log.e(TAG, "REQUEST_INSTALL_PACKAGES not granted, showing warning dialog")
                            AlertDialog.Builder(mActivity)
                                .setTitle(R.string.no_perm_package_install_dialog_title)
                                .setMessage(R.string.no_perm_package_install_dialog_message)
                                .setPositiveButton(R.string.dialog_action_positive_close, null)
                                .show()
                            return
                        }
                    }

                    AlertDialog.Builder(mActivity).setTitle("A New Update is Available!")
                        .setMessage(fromHtml(message))
                        .setNegativeButton("Don't Update", null)
                        .setPositiveButton("Update") { _, _ ->
                            val manager = NotificationManagerCompat.from(mActivity)
                            // Create noticication channel
                            val mChannel = NotificationChannelCompat.Builder(UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL,
                                NotificationManagerCompat.IMPORTANCE_LOW)
                                .setName("App Updates")
                                .setDescription("Notifications when updating the application")
                                .setLightsEnabled(true)
                                .setLightColor(Color.GREEN)
                                .build()
                            manager.createNotificationChannel(mChannel)

                            val mBuilder = NotificationCompat.Builder(mActivity, UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL)
                                .setContentTitle(mActivity.getString(R.string.notification_title_starting_download))
                                .setContentText(mActivity.getString(R.string.notification_content_starting_download))
                                .setProgress(0, 0, true).setSmallIcon(notificationIcon)
                                .setAutoCancel(false)
                                .setOngoing(true)
                                .setTicker(mActivity.getString(R.string.notification_ticker_starting_download))
                            val notificationId = Random.nextInt()
                            manager.notify(notificationId, mBuilder.build())
                            DownloadLatestUpdate(mActivity, mBuilder, manager, notificationId, notificationIcon, internalCache).executeOnExecutor(updateLink)
                        }.show()
                }
            }
        }
    }
}