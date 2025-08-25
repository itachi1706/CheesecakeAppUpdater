package com.itachi1706.appupdater.internal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.itachi1706.appupdater.R;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;
import com.itachi1706.helperlib.deprecation.PendingIntentDepKt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.internal in AppUpdater.
 * NOT FOR NON LIBRARY USE
 */
public final class DownloadLatestUpdate extends CoroutineAsyncTask<String, Float, Boolean> {
    private final Activity activity;
    private Exception except = null;
    private Uri link;
    private String filePath;
    private final NotificationCompat.Builder notification;
    private final NotificationManager manager;
    private final int notificationID;
    private final int notificationicon;
    private boolean ready = false;
    private final boolean internalCache;
    private static final String TASK_NAME = DownloadLatestUpdate.class.getSimpleName();
    private static final String TAG_NAME_DL = "Downloader";
    private static final String TAG_NAME_UP = "Updater";
    private static final String APK_NAME = "app-update.apk";

    /**
     * Called from AppUpdateChecker if the user decides to invoke anything
     * @param activity Activity in which AppUpdateChecker is called from
     * @param notificationBuilder Notification Builder Object
     * @param notifyManager Notification Manager Object
     * @param notifcationID Notification ID
     * @param notificationicon Icon for notification
     * @param internalCache Whether to save update APK file in internal or external cache
     */
    DownloadLatestUpdate(Activity activity, NotificationCompat.Builder notificationBuilder,
                         NotificationManager notifyManager, int notifcationID, int notificationicon, boolean internalCache) {
        super(TASK_NAME);
        this.activity = activity;
        this.notification = notificationBuilder;
        this.manager = notifyManager;
        this.notificationID = notifcationID;
        this.notificationicon = notificationicon;
        this.internalCache = internalCache;
    }

    private boolean deleteLegacyDownloads() {
        File folder = new File(activity.getApplicationContext().getExternalFilesDir(null) + File.separator + "download" + File.separator);
        if (!folder.exists()) return true; // Dont have the folder so its deleted
        File file = new File(folder, APK_NAME); // Try to find file to delete
        return !file.exists() || file.delete(); // Tries to delete file if it exists
    }

    @Override
    public Boolean doInBackground(String... updateLink) {
        try {
            link = Uri.parse(updateLink[0]);
            URL url = new URL(updateLink[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("GET");
            conn.connect();
            publishProgress();
            Log.d(TAG_NAME_UP, "Starting Download...");

            if (!deleteLegacyDownloads()) Log.e(TAG_NAME_UP, "Unable to delete legacy file. Skipping file deletion"); // Delete old downloaded apk
            filePath = ((this.internalCache) ? activity.getApplicationContext().getCacheDir() : activity.getApplicationContext().getExternalCacheDir()) + File.separator + "download" + File.separator;
            Log.i(TAG_NAME_UP, "Downloading to " + filePath);
            File folder = new File(filePath);
            if (!folder.exists() && (!tryAndCreateFolder(folder))) {
                Log.d(TAG_NAME_UP, "Cannot Create Folder. Not Downloading");
                conn.disconnect();
                return false;
            }
            File file = new File(folder, APK_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                Log.d(TAG_NAME_UP, "Connection done, File Obtained");
                ready = true;
                Log.d(TAG_NAME_UP, "Writing to file");
                float downloadSize = 0;
                int totalSize = conn.getContentLength();
                InputStream is = conn.getInputStream();
                byte[] buffer = new byte[1024];
                int len1;
                int lastProgress = 0;
                long lastNotification = 0;
                final int NOTIFICATION_DURATION = 500; // twice a second (500ms)
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    downloadSize += len1;
                    float progress = (downloadSize / totalSize) * 100;
                    long notifyTime = System.currentTimeMillis();
                    if ((Math.round(progress) > lastProgress) && (notifyTime - lastNotification) > NOTIFICATION_DURATION) {
                        Log.d(TAG_NAME_UP, "Download Size: " + downloadSize + "/" + totalSize);
                        lastProgress = Math.round(progress);
                        lastNotification = notifyTime;
                        publishProgress(progress, downloadSize, (float) totalSize);
                    }
                }
                is.close();//till here, it works fine - .apk is download to my sdcard in download file
                Log.d(TAG_NAME_UP, "Download Complete...");
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            except = e;
            return false;
        }
    }

    @Override
    public void onProgressUpdate(@NonNull Float... progress) {
        if (ready) {
            // Downloading new update... (Download Size / Total Size)
            double downloadMB = (double) Math.round((progress[1] / 1024.0 / 1024.0 * 100)) / 100;
            double downloadSizeMB = (double) Math.round((progress[2] / 1024.0 / 1024.0 * 100)) / 100;
            notification.setProgress(100, Math.round(progress[0]), false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Nougat onwards will have a new progress style
                notification.setSubText(Math.round(progress[0]) + "%");
                notification.setContentText("(" + downloadMB + "/" + downloadSizeMB + "MB)");
            } else {
                notification.setContentInfo(Math.round(progress[0]) + "%");
                notification.setContentText("Downloading new update... (" + downloadMB + "/" + downloadSizeMB + "MB)");
            }
        } else {
            notification.setProgress(0, 0, true);
        }
        manager.notify(notificationID, notification.build());
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @Override
    public void onPostExecute(Boolean passed) {
        Log.d(TAG_NAME_UP, "Processing download");
        notification.setAutoCancel(true).setOngoing(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notification.setSubText(null);
        } else {
            notification.setContentInfo(null);
        }
        boolean passVal = passed;
        if (!passVal) {
            //Update failed, update notification
            if (except != null) {
                //Print Exception
                notification.setContentTitle(activity.getString(R.string.notification_title_exception_download))
                        .setTicker(activity.getString(R.string.notification_ticker_download_fail))
                        .setContentText(activity.getString(R.string.notification_content_download_fail_exception,
                                except.getLocalizedMessage()))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(activity.getString(R.string.notification_content_download_fail_exception_expanded,
                                        except.getLocalizedMessage())))
                        .setSmallIcon(notificationicon).setProgress(0, 0, false);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                PendingIntent pendingIntent = PendingIntentDepKt.getImmutableActivity(activity, 0, intent);
                notification.setContentIntent(pendingIntent);
            } else {
                notification.setContentTitle(activity.getString(R.string.notification_title_exception_download))
                        .setTicker(activity.getString(R.string.notification_ticker_download_fail))
                        .setContentText(activity.getString(R.string.notification_content_download_fail))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(activity.getString(R.string.notification_content_download_fail_expanded)))
                        .setSmallIcon(notificationicon).setProgress(0, 0, false);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                PendingIntent pendingIntent = PendingIntentDepKt.getImmutableActivity(activity, 0, intent);
                notification.setContentIntent(pendingIntent);
            }
            manager.notify(notificationID, notification.build());
            return;
        }

        Log.d(TAG_NAME_UP, "Invoking Package Manager");
        //Invoke the Package Manager
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        File file = new File(filePath + APK_NAME);
        Log.d("DEBUG", "Retrieving from " + file.getAbsolutePath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG_NAME_DL, "Post-Nougat: Using new Content URI method");
            Log.i(TAG_NAME_DL, "Invoking Content Provider " + activity.getApplicationContext().getPackageName() + ".appupdater.provider");
            Uri contentUri = FileProvider.getUriForFile(activity.getBaseContext(), activity.getApplicationContext().getPackageName()
                    + ".appupdater.provider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Log.i(TAG_NAME_DL, "Pre-Nougat: Fallbacking to old method as they dont support contenturis");
            intent.setDataAndType(Uri.fromFile(new File(filePath + APK_NAME)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);

        //Notify User and add intent to invoke update
        PendingIntent pendingIntent = PendingIntentDepKt.getImmutableActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentTitle(activity.getString(R.string.notification_title_download_success))
                .setTicker(activity.getString(R.string.notification_ticker_download_success))
                .setContentText(activity.getString(R.string.notification_content_download_success))
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setSmallIcon(notificationicon).setProgress(0, 0, false);
        manager.notify(notificationID, notification.build());
    }

    private boolean tryAndCreateFolder(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            if (folder.isFile()) {
                //Rename it to something else
                int rename = 0;
                boolean check;
                do {
                    rename++;
                    check = folder.renameTo(new File(filePath + "_" + rename));
                } while (!check);
                folder = new File(filePath);
            }
            return folder.mkdir();
        }
        return false;
    }
}
