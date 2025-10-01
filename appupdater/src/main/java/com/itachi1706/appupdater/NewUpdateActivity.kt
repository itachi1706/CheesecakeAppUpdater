package com.itachi1706.appupdater

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.itachi1706.appupdater.internal.DownloadLatestUpdateFullScreen
import com.itachi1706.appupdater.`object`.AppUpdateObject
import com.itachi1706.appupdater.utils.UpdaterHelper
import com.itachi1706.helperlib.deprecation.HtmlDep.fromHtml
import com.itachi1706.helperlib.deprecation.PendingIntentDep.getImmutableActivity
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.roundToInt
import kotlin.random.Random

class NewUpdateActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NewUpdateActivity"

        const val UPDATE_NOTIFICATION: Int = 111
        const val DOWNLOAD_COMPLETE: Int = 112
        const val DOWNLOAD_FAIL: Int = 113
        const val PROCESSING_DOWNLOAD: Int = 114
    }

    private var showMore: Button? = null
    private var enableUnknown: Button? = null
    private var download: Button? = null
    private var install: Button? = null
    private var progressBar: ProgressBar? = null
    private var updateMessages: TextView? = null
    private var progressText: TextView? = null
    private var progressLayout: LinearLayout? = null

    private var manager: NotificationManagerCompat? = null

    private var updateLink: String? = null
    private var filePath: String? = null
    private var fileName: String? = null
    private var installIntent: Intent? = null
    private var notificationIcon = 0

    private var notification: NotificationCompat.Builder? = null
    private var notificationId = 0

    private lateinit var preventBackCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preventBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                Toast.makeText(applicationContext, "Unable to exit while updating", Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this, preventBackCallback)

        if (!intent.hasExtra("update")) {
            Log.e(TAG, "No update message in intent. Exiting")
            finish()
            return
        }

        val json = Json { ignoreUnknownKeys = true }
        val update = json.decodeFromString<AppUpdateObject>(intent.getStringExtra("update") ?: "")
        val internalCache = intent.getBooleanExtra("internalCache", false)

        supportActionBar?.let {
            if (it.isShowing) {
                it.setHomeAsUpIndicator(R.drawable.ic_close)
                it.setDisplayHomeAsUpEnabled(true)
                it.title = "Version ${update.latestVersion} is available!"
            }
        }

        if (intent.hasExtra("nicon")) {
            notificationIcon = intent.extras?.getInt("nicon") ?: 0
        } else {
            Log.e(TAG, "No notification icon. Exiting...")
            finish()
            return
        }

        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(android.R.id.content, this, R.layout.activity_new_update)

        showMore = findViewById(R.id.btnMore)
        enableUnknown = findViewById(R.id.btnEnableUnknown)
        download = findViewById(R.id.btnDownload)
        install = findViewById(R.id.btnInstall)
        progressBar = findViewById(R.id.pbProgress)
        updateMessages = findViewById(R.id.tvUpdateMsg)
        progressText = findViewById(R.id.tvProgress)
        progressLayout = findViewById(R.id.ll_progress)

        manager = NotificationManagerCompat.from(this)

        val unknownSource = findViewById<TextView>(R.id.lblUnknownSource)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            unknownSource.setText(R.string.lbl_unknown_enable_api_26)
            enableUnknown?.setText(R.string.grant)
        }

        // Processing
        filePath = (if (internalCache) {
            applicationContext.cacheDir
        } else {
            applicationContext.externalCacheDir
        }).toString() + "${File.separator}download${File.separator}"
        fileName = "app-update_${update.latestVersion}.apk"
        val mHandler = UpdateHandler(Looper.getMainLooper(), this)
        notificationId = Random.nextInt()

        // Generate Intent
        installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        installIntent?.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        val file = File(filePath + fileName)
        Log.d(TAG, "Retrieving from ${file.absolutePath}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "Post-Nougat: Using new Content URI Method")
            Log.i(TAG, "Invoking Content Provider ${applicationContext.packageName}.appupdater.provider")
            val contentUri = FileProvider.getUriForFile(baseContext, "${applicationContext.packageName}.appupdater.provider", file)
            installIntent?.setDataAndType(contentUri, "application/vnd.android.package-archive")
            installIntent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            Log.i(TAG, "Pre-Nougat: Fallbacking to old method as they dont support contenturis")
            installIntent?.setDataAndType(Uri.fromFile(File(filePath + fileName)), "application/vnd.android.package-archive")
            installIntent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Parse update
        val fullUpdateMessage = UpdaterHelper.getChangelogStringFromArray(update.updateMessage)
        var updateMessage = "No Update Message"
        for (m in update.updateMessage) {
            if (m.versionCode.equals(update.latestVersionCode, ignoreCase = true)) {
                // Done
                val lbl = m.labels.replace("<font color=\"green\">LATEST</font>", "").trim()
                Log.d(TAG, lbl)
                val sb = StringBuilder()
                if (lbl.isNotEmpty()) sb.append("$lbl<br/>")
                sb.append(m.updateText?.replace("\r\n", "<br/>"))
                updateMessage = sb.toString()
                updateLink = m.url
                break
            }
        }

        // Setup Screen
        enableUnknown?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
            } else {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            }
        }

        download?.setOnClickListener {
            deleteDownload()

            download?.visibility = View.GONE
            install?.isEnabled = false
            progressLayout?.visibility = View.VISIBLE
            progressBar?.progress = 0
            progressBar?.isIndeterminate = true
            progressText?.text = getString(R.string.progress, 0f)
            preventBackCallback.isEnabled = true
            supportActionBar?.let { ab ->
                if (ab.isShowing) {
                    ab.setDisplayHomeAsUpEnabled(false)
                }
            }

            notification = NotificationCompat.Builder(applicationContext, UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL)
            notification!!.setContentTitle(applicationContext.getString(R.string.notification_title_starting_download))
                .setContentText(applicationContext.getString(R.string.notification_content_starting_download))
                .setProgress(0, 0, true).setSmallIcon(notificationIcon).setAutoCancel(false)
                .setOngoing(true)
                .setTicker(applicationContext.getString(R.string.notification_ticker_starting_download))
            manager?.notify(notificationId, notification!!.build())
            DownloadLatestUpdateFullScreen(if (internalCache) applicationContext.cacheDir else applicationContext.externalCacheDir,
                update.latestVersion, mHandler).executeOnExecutor(updateLink)
        }
        install?.setOnClickListener {
            Log.d(TAG, "Invoking Package Manager")
            applicationContext.startActivity(installIntent)
        }
        showMore?.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Changelog")
                .setMessage(fromHtml(fullUpdateMessage))
                .setPositiveButton(R.string.dialog_action_positive_close, null).show()
        }

        updateMessages?.text = fromHtml(updateMessage)

        // Create notification channel required
        val mChannel = NotificationChannelCompat.Builder(UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("App Updates")
            .setDescription("Notifications when updating the application")
            .setLightsEnabled(true)
            .setLightColor(Color.GREEN)
            .build()
        manager?.createNotificationChannel(mChannel)
    }

    override fun onResume() {
        super.onResume()

        // Check buttons and hide
        var isNonPlayAppAllowed = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                isNonPlayAppAllowed = packageManager.canRequestPackageInstalls()
            } catch (e: SecurityException) {
                Log.e(TAG, "REQUEST_INSTALL_PACKAGES permission not granted")
                AlertDialog.Builder(this).setTitle(R.string.no_perm_package_install_dialog_title)
                    .setMessage(R.string.no_perm_package_install_dialog_message)
                    .setOnDismissListener { finish() }
                    .setPositiveButton(R.string.dialog_action_positive_close, null).show()
                return
            }
        } else {
            try {
                isNonPlayAppAllowed = Settings.Secure.getInt(
                    contentResolver,
                    Settings.Secure.INSTALL_NON_MARKET_APPS
                ) == 1
            } catch (e: SettingNotFoundException) {
                Log.e(TAG, "Presuming Unknown Sources Unchecked")
                Log.e(TAG, "Exception: ${e.message}")
            }
        }

        enableUnknown?.setEnabled(!isNonPlayAppAllowed)
        if (updateLink!!.isEmpty()) download?.visibility = View.GONE
        else download?.visibility = View.VISIBLE

        val f = File(filePath, fileName!!)
        if (f.exists()) {
            download?.visibility = View.VISIBLE
            download?.setText(R.string.redownload)
            install?.setEnabled(true)
            progressLayout?.visibility = View.VISIBLE
            progressBar?.progress = 100
            progressBar?.isIndeterminate = false
            progressText?.text = getString(R.string.progress, 100f)
        } else {
            download?.visibility = View.VISIBLE
            download?.setText(R.string.download)
            install?.setEnabled(false)
            progressLayout?.visibility = View.GONE
        }
    }

    private fun deleteDownload() {
        // Delete any past download
        val folder = File(filePath!!)
        if (folder.exists()) {
            val f = File(folder, fileName!!)
            if (f.exists()) {
                Log.i(TAG, "Delete File status: ${f.delete()}")
            }
        }
    }

    inner class UpdateHandler(mainLooper: Looper, activity: NewUpdateActivity?) : Handler(mainLooper) {
        var mActivity: WeakReference<NewUpdateActivity?> = WeakReference<NewUpdateActivity?>(activity)

        override fun handleMessage(msg: Message) {
            val activity = mActivity.get()
            super.handleMessage(msg)
            when (msg.what) {
                UPDATE_NOTIFICATION -> {
                    val ready = msg.getData().getBoolean("ready")
                    if (ready) {
                        val dl = msg.getData().getFloat("download")
                        val dlt = msg.getData().getFloat("total")
                        val dlp = msg.getData().getFloat("progress")
                        activity?.updateNotification(true, dlp, dl, dlt)
                    } else activity?.updateNotification(false)
                }

                PROCESSING_DOWNLOAD -> activity?.handleProcessDownload()
                DOWNLOAD_COMPLETE -> activity?.handleSuccess()
                DOWNLOAD_FAIL -> {
                    val except = msg.getData().getString("except", "")
                    activity?.handleFailure(except)
                }

                else -> Log.e(TAG, "Unknown message received: ${msg.what}")
            }
        }
    }

    private fun handleProcessDownload() {
        Log.d(TAG, "Processing download")
        supportActionBar?.let {
            if (it.isShowing) {
                it.setDisplayHomeAsUpEnabled(true)
            }
        }
        preventBackCallback.isEnabled = false
        if (notification == null) return
        notification?.let {
            it.setAutoCancel(true).setOngoing(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.setSubText(null)
            } else {
                it.setContentInfo(null)
            }
        }
    }

    private fun updateNotification(ready: Boolean, vararg progress: Float?) {
        if (notification == null) return
        if (ready) {
            // Downloading new update... (Download Size / Total Size)
            val downloadMB = Math.round((progress[1]!! / 1024.0 / 1024.0 * 100)).toDouble() / 100
            val downloadSizeMB =
                Math.round((progress[2]!! / 1024.0 / 1024.0 * 100)).toDouble() / 100
            notification?.setProgress(100, Math.round(progress[0]!!), false)
            progressBar?.setIndeterminate(false)
            progressBar?.setProgress(Math.round(progress[0]!!))
            progressText?.setText(getString(R.string.progress, progress[0]))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Nougat onwards will have a new progressText style
                notification?.setSubText(progress[0]?.roundToInt().toString() + "%")
                notification?.setContentText("(" + downloadMB + "/" + downloadSizeMB + "MB)")
            } else {
                notification?.setContentInfo(progress[0]?.roundToInt().toString() + "%")
                notification?.setContentText("Downloading new update... (" + downloadMB + "/" + downloadSizeMB + "MB)")
            }
        } else {
            notification!!.setProgress(0, 0, true)
        }
        manager!!.notify(notificationId, notification!!.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun handleFailure(except: String) {
        // Reenable download button, reset progressText bar, delete failed download
        download!!.setVisibility(View.VISIBLE)
        progressLayout!!.setVisibility(View.GONE)
        install!!.setEnabled(false)
        download!!.setText(R.string.download)
        deleteDownload()

        if (notification == null) return
        if (except.isNotEmpty()) {
            //Print Exception
            notification!!.setContentTitle(getString(R.string.notification_title_exception_download))
                .setTicker(getString(R.string.notification_ticker_download_fail))
                .setContentText(
                    getString(
                        R.string.notification_content_download_fail_exception,
                        except
                    )
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            getString(
                                R.string.notification_content_download_fail_exception_expanded,
                                except
                            )
                        )
                )
                .setSmallIcon(notificationIcon).setProgress(0, 0, false)
            val intent = Intent(Intent.ACTION_VIEW, updateLink?.toUri())
            val pendingIntent = getImmutableActivity(this, 0, intent)
            notification!!.setContentIntent(pendingIntent)
        } else {
            notification!!.setContentTitle(getString(R.string.notification_title_exception_download))
                .setTicker(getString(R.string.notification_ticker_download_fail))
                .setContentText(getString(R.string.notification_content_download_fail))
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.notification_content_download_fail_expanded))
                )
                .setSmallIcon(notificationIcon).setProgress(0, 0, false)
            val intent = Intent(Intent.ACTION_VIEW, updateLink?.toUri())
            val pendingIntent = getImmutableActivity(this, 0, intent)
            notification!!.setContentIntent(pendingIntent)
        }
        manager!!.notify(notificationId, notification!!.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun handleSuccess() {
        // Download button becomes redownload button, 100% progressText bar, enable install button
        progressText!!.setText(getString(R.string.progress, 100f))
        progressBar!!.setProgress(100)
        progressBar!!.setIndeterminate(false)
        progressLayout!!.setVisibility(View.VISIBLE)
        download!!.setVisibility(View.VISIBLE)
        download!!.setText(R.string.redownload)
        install!!.setEnabled(true)

        //Notify User and add intent to invoke update
        if (notification == null) return
        val pendingIntent =
            getImmutableActivity(this, 0, installIntent!!, PendingIntent.FLAG_UPDATE_CURRENT)

        notification!!.setContentTitle(getString(R.string.notification_title_download_success))
            .setTicker(getString(R.string.notification_ticker_download_success))
            .setContentText(getString(R.string.notification_content_download_success))
            .setAutoCancel(true).setContentIntent(pendingIntent)
            .setSmallIcon(notificationIcon).setProgress(0, 0, false)
        manager!!.notify(notificationId, notification!!.build())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}