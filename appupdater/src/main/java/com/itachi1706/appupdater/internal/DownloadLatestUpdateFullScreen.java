package com.itachi1706.appupdater.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itachi1706.appupdater.NewUpdateActivity;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;

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
public final class DownloadLatestUpdateFullScreen extends CoroutineAsyncTask<String, Float, Boolean> {
    private final File cacheDir;
    private Exception except = null;
    private String filePath;
    private final String version;
    private boolean ready = false;
    private final Handler handler;
    private static final String TASK_NAME = DownloadLatestUpdateFullScreen.class.getSimpleName();

    /**
     * Called from AppUpdateChecker if the user decides to invoke anything
     * @param cacheDir External Cache Folder
     * @param version Version Number of the Update
     * @param handler Handler to talk back to the context
     */
    public DownloadLatestUpdateFullScreen(File cacheDir, String version, Handler handler) {
        super(TASK_NAME);
        this.cacheDir = cacheDir;
        this.version = version;
        this.handler = handler;
    }

    @Override
    public Boolean doInBackground(String... updateLink) {
        ready = false;
        try {
            URL url = new URL(updateLink[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("GET");
            conn.connect();
            publishProgress();
            Log.d("Updater", "Starting Download...");

            filePath = cacheDir + File.separator + "download" + File.separator;
            Log.i("Updater", "Downloading to " + filePath);
            File folder = new File(filePath);
            if (!folder.exists()) {
                if (!tryAndCreateFolder(folder)) {
                    Log.d("Updater", "Cannot Create Folder. Not Downloading");
                    conn.disconnect();
                    return false;
                }
            }
            String fileName = "app-update_" + version + ".apk";
            File file = new File(folder, fileName);
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

    public void onProgressUpdate(@NonNull Float... progress) {
        Message msg = Message.obtain();
        msg.what = NewUpdateActivity.UPDATE_NOTIFICATION;
        Bundle bundle = new Bundle();
        bundle.putBoolean("ready", ready);
        if (ready) {
            if (progress.length >= 3) {
                bundle.putFloat("download", progress[1]);
                bundle.putFloat("total", progress[2]);
                bundle.putFloat("progress", progress[0]);
            }
        }
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void onPostExecute(Boolean passed) {
        Message processmsg = Message.obtain();
        processmsg.what = NewUpdateActivity.PROCESSING_DOWNLOAD;
        handler.sendMessage(processmsg);

        if (!passed) {
            //Update failed, update notification
            Message msg = Message.obtain();
            msg.what = NewUpdateActivity.DOWNLOAD_FAIL;
            String exception = "";
            if (except != null) {
                exception = except.getLocalizedMessage();
            }
            Bundle bundle = new Bundle();
            bundle.putString("except", exception);
            msg.setData(bundle);
            handler.sendMessage(msg);
            return;
        }

        Message msg = Message.obtain();
        msg.what = NewUpdateActivity.DOWNLOAD_COMPLETE;
        handler.sendMessage(msg);
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
