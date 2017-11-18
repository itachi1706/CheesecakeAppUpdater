package com.itachi1706.appupdater;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Objects.AppUpdateMessageObject;
import com.itachi1706.appupdater.Objects.AppUpdateObject;
import com.itachi1706.appupdater.Util.DeprecationHelper;
import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.appupdater.internal.DownloadLatestUpdateFullScreen;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Random;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

public class NewUpdateActivity extends AppCompatActivity {

    Button showMore, enableUnknown, download, install;
    ProgressBar progressBar;
    TextView updateMessages, progressText;
    LinearLayout progressLayout;

    NotificationManager manager;

    private String fullUpdateMessage, updateLink, filePath, fileName;
    private AppUpdateObject update;
    private Intent installIntent;
    private boolean processing = false;
    private int notificationIcon;
    private UpdateHandler mHandler;

    private NotificationCompat.Builder notification = null;
    private int notificationId;

    private static final String TAG = "NewUpdateAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra("update")) {
            Log.e(TAG, "No Update Message in intent. Exiting");
            finish();
            return;
        } else {
            Gson gson = new Gson();
            update = gson.fromJson(getIntent().getStringExtra("update"), AppUpdateObject.class);
        }

        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Version " + update.getLatestVersion() + " is available!");
        }
        
        if (getIntent().hasExtra("nicon")) notificationIcon = getIntent().getExtras().getInt("nicon");
        else {
            Log.e(TAG, "No Notification Icon. Exiting");
            finish();
            return;
        }
        setContentView(R.layout.activity_new_update);

        showMore = findViewById(R.id.btnMore);
        enableUnknown = findViewById(R.id.btnEnableUnknown);
        download = findViewById(R.id.btnDownload);
        install = findViewById(R.id.btnInstall);
        progressBar = findViewById(R.id.pbProgress);
        updateMessages = findViewById(R.id.tvUpdateMsg);
        progressText = findViewById(R.id.tvProgress);
        progressLayout = findViewById(R.id.ll_progress);
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Processing
        filePath = getApplicationContext().getExternalCacheDir() + File.separator + "download" + File.separator;
        fileName = "app-update_" + update.getLatestVersion() + ".apk";
        mHandler = new UpdateHandler(this);
        Random random = new Random();
        notificationId = random.nextInt();
        
        // Generate Intent
        installIntent = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath + fileName);
        Log.d(TAG, "Retrieving from " + file.getAbsolutePath());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.i(TAG, "Post-Nougat: Using new Content URI method");
            Log.i(TAG, "Invoking Content Provider " + getApplicationContext().getPackageName() + ".appupdater.provider");
            Uri contentUri = FileProvider.getUriForFile(getBaseContext(), getApplicationContext().getPackageName()
                    + ".appupdater.provider", file);
            installIntent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            Log.i(TAG, "Pre-Nougat: Fallbacking to old method as they dont support contenturis");
            installIntent.setDataAndType(Uri.fromFile(new File(filePath + fileName)), "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // Parse Update
        fullUpdateMessage = UpdaterHelper.getChangelogStringFromArray(update.getUpdateMessage());
        String updateMessage = "No Update Message";
        for (AppUpdateMessageObject m : update.getUpdateMessage()) {
            if (m.getVersionCode().equalsIgnoreCase(update.getLatestVersionCode())) {
                // Done
                String lbl = m.getLabels().replace("<font color=\"green\">LATEST</font>", "").trim();
                Log.d(TAG, lbl);
                updateMessage = "";
                if (!lbl.isEmpty()) updateMessage += lbl + "<br/>";
                updateMessage += m.getUpdateText().replace("\r\n", "<br/>");
                updateLink = m.getUrl();
                break;
            }
        }

        // Setup Screen
        enableUnknown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
                else
                    startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
            }
        });
        
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDownload();

                download.setVisibility(View.GONE);
                install.setEnabled(false);
                progressLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
                progressBar.setIndeterminate(true);
                progressText.setText(getString(R.string.progress, 0f));
                processing = true;
                if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }

                notification = new NotificationCompat.Builder(getApplicationContext(), UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL);
                notification.setContentTitle(getApplicationContext().getString(R.string.notification_title_starting_download))
                        .setContentText(getApplicationContext().getString(R.string.notification_content_starting_download))
                        .setProgress(0, 0, true).setSmallIcon(notificationIcon).setAutoCancel(false)
                        .setOngoing(true).setTicker(getApplicationContext().getString(R.string.notification_ticker_starting_download));
                manager.notify(notificationId, notification.build());
                new DownloadLatestUpdateFullScreen(getApplicationContext().getExternalCacheDir(),
                        update.getLatestVersion(), mHandler).executeOnExecutor(THREAD_POOL_EXECUTOR, updateLink);
            }
        });
        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Invoking Package Manager");
                //Invoke the Package Manager
                getApplicationContext().startActivity(installIntent);
            }
        });
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(NewUpdateActivity.this).setTitle("Changelog")
                        .setMessage(DeprecationHelper.Html.fromHtml(fullUpdateMessage))
                        .setPositiveButton(R.string.dialog_action_positive_close, null).show();
            }
        });

        updateMessages.setText(DeprecationHelper.Html.fromHtml(updateMessage));

        // Create the Notification Channel needed for the app
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL, "App Updates", NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription("Notifications when updating the application");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.GREEN);
            manager.createNotificationChannel(mChannel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check for all the buttons/hide
        boolean isNonPlayAppAllowed = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isNonPlayAppAllowed = getPackageManager().canRequestPackageInstalls();
        } else {
            try {
                //noinspection deprecation
                isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                // Presume not enabled
                Log.e(TAG, "Presuming Unknown Sources Unchecked");
            }
        }

        if (isNonPlayAppAllowed) enableUnknown.setEnabled(false);
        else enableUnknown.setEnabled(true);
        if (updateLink.isEmpty()) download.setVisibility(View.GONE);
        else download.setVisibility(View.VISIBLE);

        File f = new File(filePath, fileName);
        if (f.exists()) {
            download.setVisibility(View.VISIBLE);
            download.setText(R.string.redownload);
            install.setEnabled(true);
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setProgress(100);
            progressBar.setIndeterminate(false);
            progressText.setText(getString(R.string.progress, 100f));
        } else {
            download.setVisibility(View.VISIBLE);
            download.setText(R.string.download);
            install.setEnabled(false);
            progressLayout.setVisibility(View.GONE);
        }
    }

    private void deleteDownload() {
        // Delete any past downlooad
        File folder = new File(filePath);
        if (folder.exists()) {
            File f = new File(folder, fileName);
            if (f.exists()) {
                Log.i(TAG, "Delete File status: " + f.delete());
            }
        }
    }

    public static final int UPDATE_NOTIFICATION = 111, DOWNLOAD_COMPLETE = 112,
            DOWNLOAD_FAIL = 113, PROCESSING_DOWNLOAD = 114;

    private static class UpdateHandler extends Handler {
        WeakReference<NewUpdateActivity> mActivity;

        UpdateHandler(NewUpdateActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            NewUpdateActivity activity = mActivity.get();
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_NOTIFICATION:
                    boolean ready = msg.getData().getBoolean("ready");
                    if (ready) {
                        float dl = msg.getData().getFloat("download");
                        float dlt = msg.getData().getFloat("total");
                        float dlp = msg.getData().getFloat("progress");
                        activity.updateNotification(true, dlp, dl, dlt);
                    } else activity.updateNotification(false);
                    break;
                case PROCESSING_DOWNLOAD: activity.handleProcessDownload(); break;
                case DOWNLOAD_COMPLETE: activity.handleSuccess(); break;
                case DOWNLOAD_FAIL:
                    String except = msg.getData().getString("except", "");
                    activity.handleFailure(except); break;
            }
        }
    }

    private void handleProcessDownload() {
        Log.d(TAG, "Processing download");
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        processing = false;
        if (notification == null) return;
        notification.setAutoCancel(true).setOngoing(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notification.setSubText(null);
        } else {
            notification.setContentInfo(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (!processing) super.onBackPressed();
        else Toast.makeText(getApplicationContext(), "Unable to exit while updating", Toast.LENGTH_SHORT).show();
    }

    private void updateNotification(boolean ready, Float... progress) {
        if (notification == null) return;
        if (ready) {
            // Downloading new update... (Download Size / Total Size)
            double downloadMB = (double) Math.round((progress[1] / 1024.0 / 1024.0 * 100)) / 100;
            double downloadSizeMB = (double) Math.round((progress[2] / 1024.0 / 1024.0 * 100)) / 100;
            notification.setProgress(100, Math.round(progress[0]), false);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(Math.round(progress[0]));
            progressText.setText(getString(R.string.progress, progress[0]));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Nougat onwards will have a new progressText style
                notification.setSubText(Math.round(progress[0]) + "%");
                notification.setContentText("(" + downloadMB + "/" + downloadSizeMB + "MB)");
            } else {
                notification.setContentInfo(Math.round(progress[0]) + "%");
                notification.setContentText("Downloading new update... (" + downloadMB + "/" + downloadSizeMB + "MB)");
            }
            manager.notify(notificationId, notification.build());
        } else {
            notification.setProgress(0, 0, true);
            manager.notify(notificationId, notification.build());
        }
    }

    private void handleFailure(String except) {
        // Reenable download button, reset progressText bar, delete failed download
        download.setVisibility(View.VISIBLE);
        progressLayout.setVisibility(View.GONE);
        install.setEnabled(false);
        download.setText(R.string.download);
        deleteDownload();

        if (notification == null) return;
        if (!except.isEmpty()) {
            //Print Exception
            notification.setContentTitle(getString(R.string.notification_title_exception_download))
                    .setTicker(getString(R.string.notification_ticker_download_fail))
                    .setContentText(getString(R.string.notification_content_download_fail_exception,
                            except))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.notification_content_download_fail_exception_expanded,
                                    except)))
                    .setSmallIcon(notificationIcon).setProgress(0, 0, false);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            notification.setContentIntent(pendingIntent);
            manager.notify(notificationId, notification.build());
        } else {
            notification.setContentTitle(getString(R.string.notification_title_exception_download))
                    .setTicker(getString(R.string.notification_ticker_download_fail))
                    .setContentText(getString(R.string.notification_content_download_fail))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.notification_content_download_fail_expanded)))
                    .setSmallIcon(notificationIcon).setProgress(0, 0, false);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            notification.setContentIntent(pendingIntent);
            manager.notify(notificationId, notification.build());
        }
    }

    private void handleSuccess() {
        // Download button becomes redownload button, 100% progressText bar, enable install button
        progressText.setText(getString(R.string.progress, 100f));
        progressBar.setProgress(100);
        progressBar.setIndeterminate(false);
        progressLayout.setVisibility(View.VISIBLE);
        download.setVisibility(View.VISIBLE);
        download.setText(R.string.redownload);
        install.setEnabled(true);

        //Notify User and add intent to invoke update
        if (notification == null) return;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, installIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentTitle(getString(R.string.notification_title_download_success))
                .setTicker(getString(R.string.notification_ticker_download_success))
                .setContentText(getString(R.string.notification_content_download_success))
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setSmallIcon(notificationIcon).setProgress(0, 0, false);
        manager.notify(notificationId, notification.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
