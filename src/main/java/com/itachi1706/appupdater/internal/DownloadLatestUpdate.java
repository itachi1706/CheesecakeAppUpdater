package com.itachi1706.appupdater.internal;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.itachi1706.appupdater.R;

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
public final class DownloadLatestUpdate extends AsyncTask<String, Float, Boolean> {
    private Activity activity;
    private Exception except = null;
    private Uri link;
    private String filePATH;
    private NotificationCompat.Builder notification;
    private NotificationManager manager;
    private int notificationID;
    private int notificationicon;
    private boolean ready = false;

    /**
     * Called from AppUpdateChecker if the user decides to invoke anything
     * @param activity Activity in which AppUpdateChecker is called from
     * @param notificationBuilder Notification Builder Object
     * @param notifyManager Notification Manager Object
     * @param notifcationID Notification ID
     * @param notificationicon Icon for notification
     */
    protected DownloadLatestUpdate(Activity activity, NotificationCompat.Builder notificationBuilder,
                                NotificationManager notifyManager, int notifcationID, int notificationicon) {
        this.activity = activity;
        this.notification = notificationBuilder;
        this.manager = notifyManager;
        this.notificationID = notifcationID;
        this.notificationicon = notificationicon;
    }

    private boolean deleteLegacyDownloads() {
        File folder = new File(activity.getApplicationContext().getExternalFilesDir(null) + File.separator + "download" + File.separator);
        if (!folder.exists()) return true; // Dont have the folder so its deleted
        File file = new File(folder, "app-update.apk"); // Try to find file to delete
        return !file.exists() || file.delete(); // Tries to delete file if it exists
    }

    @Override
    protected Boolean doInBackground(String... updateLink) {
        try {
            link = Uri.parse(updateLink[0]);
            URL url = new URL(updateLink[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("GET");
            conn.connect();
            publishProgress();
            Log.d("Updater", "Starting Download...");

            if (!deleteLegacyDownloads()) Log.e("Updater", "Unable to delete legacy file. Skipping file deletion"); // Delete old downloaded apk
            filePATH = activity.getApplicationContext().getExternalCacheDir() + File.separator + "download" + File.separator;
            File folder = new File(filePATH);
            if (!folder.exists()) {
                if (!tryAndCreateFolder(folder)) {
                    Log.d("Updater", "Cannot Create Folder. Not Downloading");
                    conn.disconnect();
                    return false;
                }
            }
            File file = new File(folder, "app-update.apk");
            FileOutputStream fos = new FileOutputStream(file);
            Log.d("Updater", "Connection done, File Obtained");
            ready = true;
            Log.d("Updater", "Writing to file");
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
                    Log.d("Updater", "Download Size: " + downloadSize + "/" + totalSize);
                    lastProgress = Math.round(progress);
                    lastNotification = notifyTime;
                    publishProgress(progress, downloadSize, (float) totalSize);
                }
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file
            Log.d("Updater", "Download Complete...");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            except = e;
            return false;
        }
    }

    protected void onProgressUpdate(Float... progress) {
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
            manager.notify(notificationID, notification.build());
        } else {
            notification.setProgress(0, 0, true);
            manager.notify(notificationID, notification.build());
        }
    }

    @Override
    protected void onPostExecute(Boolean passed) {
        Log.d("Updater", "Processing download");
        notification.setAutoCancel(true).setOngoing(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notification.setSubText(null);
        } else {
            notification.setContentInfo(null);
        }
        if (!passed) {
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
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
                notification.setContentIntent(pendingIntent);
                manager.notify(notificationID, notification.build());
            } else {
                notification.setContentTitle(activity.getString(R.string.notification_title_exception_download))
                        .setTicker(activity.getString(R.string.notification_ticker_download_fail))
                        .setContentText(activity.getString(R.string.notification_content_download_fail))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(activity.getString(R.string.notification_content_download_fail_expanded)))
                        .setSmallIcon(notificationicon).setProgress(0, 0, false);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
                notification.setContentIntent(pendingIntent);
                manager.notify(notificationID, notification.build());
            }
            return;
        }

        Log.d("Updater", "Invoking Package Manager");
        //Invoke the Package Manager
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePATH + "app-update.apk");
        Log.d("DEBUG", "Retrieving from " + file.getAbsolutePath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i("Downloader", "Post-Nougat: Using new Content URI method");
            Log.i("Downloader", "Invoking Content Provider " + activity.getApplicationContext().getPackageName() + ".appupdater.provider");
            Uri contentUri = FileProvider.getUriForFile(activity.getBaseContext(), activity.getApplicationContext().getPackageName()
                    + ".appupdater.provider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Log.i("Downloader", "Pre-Nougat: Fallbacking to old method as they dont support contenturis");
            intent.setDataAndType(Uri.fromFile(new File(filePATH + "app-update.apk")), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);

        //Notify User and add intent to invoke update
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
                    check = folder.renameTo(new File(filePATH + "_" + rename));
                } while (!check);
                folder = new File(filePATH);
            }
            return folder.mkdir();
        }
        return false;
    }
}
