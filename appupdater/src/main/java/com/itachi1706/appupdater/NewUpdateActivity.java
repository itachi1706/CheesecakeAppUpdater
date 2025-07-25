package com.itachi1706.appupdater;

import android.annotation.SuppressLint;
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
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.itachi1706.appupdater.internal.DownloadLatestUpdateFullScreen;
import com.itachi1706.appupdater.object.AppUpdateMessageObject;
import com.itachi1706.appupdater.object.AppUpdateObject;
import com.itachi1706.appupdater.utils.UpdaterHelper;
import com.itachi1706.helperlib.deprecation.HtmlDep;
import com.itachi1706.helperlib.deprecation.PendingIntentDep;
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Random;

public class NewUpdateActivity extends AppCompatActivity {

    Button showMore;
    Button enableUnknown;
    Button download;
    Button install;
    ProgressBar progressBar;
    TextView updateMessages;
    TextView progressText;
    LinearLayout progressLayout;

    NotificationManager manager;

    private String updateLink;
    private String filePath;
    private String fileName;
    private Intent installIntent;
    private int notificationIcon;

    private NotificationCompat.Builder notification = null;
    private int notificationId;

    private static final String TAG = "NewUpdateAct";
    private final Random random = new Random();

    private OnBackPressedCallback preventBackCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String fullUpdateMessage;
        boolean internalCache;
        UpdateHandler mHandler;
        AppUpdateObject update;

        preventBackCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(getApplicationContext(), "Unable to exit while updating", Toast.LENGTH_SHORT).show();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, preventBackCallback);

        if (!getIntent().hasExtra("update")) {
            Log.e(TAG, "No Update Message in intent. Exiting");
            finish();
            return;
        } else {
            Gson gson = new Gson();
            update = gson.fromJson(getIntent().getStringExtra("update"), AppUpdateObject.class);
        }

        internalCache = getIntent().getBooleanExtra("internalCache", false);

        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Version " + update.getLatestVersion() + " is available!");
        }
        
        if (getIntent().hasExtra("nicon")) {
            notificationIcon = getIntent().getExtras().getInt("nicon");
        }
        else {
            Log.e(TAG, "No Notification Icon. Exiting");
            finish();
            return;
        }
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(android.R.id.content, this, R.layout.activity_new_update);

        showMore = findViewById(R.id.btnMore);
        enableUnknown = findViewById(R.id.btnEnableUnknown);
        download = findViewById(R.id.btnDownload);
        install = findViewById(R.id.btnInstall);
        progressBar = findViewById(R.id.pbProgress);
        updateMessages = findViewById(R.id.tvUpdateMsg);
        progressText = findViewById(R.id.tvProgress);
        progressLayout = findViewById(R.id.ll_progress);
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        TextView unknownSource = findViewById(R.id.lblUnknownSource);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            unknownSource.setText(R.string.lbl_unknown_enable_api_26);
            enableUnknown.setText(R.string.grant);
        }

        // Processing
        filePath = ((internalCache) ? getApplicationContext().getCacheDir() : getApplicationContext().getExternalCacheDir()) + File.separator + "download" + File.separator;
        fileName = "app-update_" + update.getLatestVersion() + ".apk";
        mHandler = new UpdateHandler(Looper.getMainLooper(), this);
        notificationId = random.nextInt();
        
        // Generate Intent
        installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
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
                StringBuilder sb = new StringBuilder();
                if (!lbl.isEmpty()) sb.append(lbl).append("<br/>");
                sb.append(m.getUpdateText().replace("\r\n", "<br/>"));
                updateMessage = sb.toString();
                updateLink = m.getUrl();
                break;
            }
        }

        // Setup Screen
        enableUnknown.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
            else
                startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        });
        
        download.setOnClickListener(v -> {
            deleteDownload();

            download.setVisibility(View.GONE);
            install.setEnabled(false);
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            progressBar.setIndeterminate(true);
            progressText.setText(getString(R.string.progress, 0f));
            preventBackCallback.setEnabled(true);
            if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }

            notification = new NotificationCompat.Builder(getApplicationContext(), UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL);
            notification.setContentTitle(getApplicationContext().getString(R.string.notification_title_starting_download))
                    .setContentText(getApplicationContext().getString(R.string.notification_content_starting_download))
                    .setProgress(0, 0, true).setSmallIcon(notificationIcon).setAutoCancel(false)
                    .setOngoing(true).setTicker(getApplicationContext().getString(R.string.notification_ticker_starting_download));
            manager.notify(notificationId, notification.build());
            new DownloadLatestUpdateFullScreen((internalCache) ? getApplicationContext().getCacheDir() : getApplicationContext().getExternalCacheDir(),
                    update.getLatestVersion(), mHandler).executeOnExecutor(updateLink);
        });
        install.setOnClickListener(v -> {
            Log.d(TAG, "Invoking Package Manager");
            //Invoke the Package Manager
            getApplicationContext().startActivity(installIntent);
        });
        showMore.setOnClickListener(v -> new AlertDialog.Builder(NewUpdateActivity.this).setTitle("Changelog")
                .setMessage(HtmlDep.fromHtml(fullUpdateMessage))
                .setPositiveButton(R.string.dialog_action_positive_close, null).show());

        updateMessages.setText(HtmlDep.fromHtml(updateMessage));

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
            try {
                isNonPlayAppAllowed = getPackageManager().canRequestPackageInstalls();
            } catch (SecurityException e) {
                Log.e(TAG, "REQUEST_INSTALL_PACKAGES permission not granted");
                new AlertDialog.Builder(this).setTitle(R.string.no_perm_package_install_dialog_title)
                        .setMessage(R.string.no_perm_package_install_dialog_message)
                        .setOnDismissListener(dialogInterface -> finish())
                        .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                return;
            }
        } else {
            try {
                isNonPlayAppAllowed = Settings.Secure.getInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS) == 1;
            } catch (Settings.SettingNotFoundException e) {
                Log.e(TAG, "Presuming Unknown Sources Unchecked");
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }

        enableUnknown.setEnabled(!isNonPlayAppAllowed);
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

    public static final int UPDATE_NOTIFICATION = 111;
    public static final int DOWNLOAD_COMPLETE = 112;
    public static final int DOWNLOAD_FAIL = 113;
    public static final int PROCESSING_DOWNLOAD = 114;

    private static class UpdateHandler extends Handler {
        WeakReference<NewUpdateActivity> mActivity;

        UpdateHandler(Looper mainLooper, NewUpdateActivity activity) {
            super(mainLooper);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
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
                default: break;
            }
        }
    }

    private void handleProcessDownload() {
        Log.d(TAG, "Processing download");
        if (getSupportActionBar() != null && getSupportActionBar().isShowing()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        preventBackCallback.setEnabled(false);
        if (notification == null) return;
        notification.setAutoCancel(true).setOngoing(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notification.setSubText(null);
        } else {
            notification.setContentInfo(null);
        }
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
        } else {
            notification.setProgress(0, 0, true);
        }
        manager.notify(notificationId, notification.build());
    }

    @SuppressLint("UnspecifiedImmutableFlag")
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
            PendingIntent pendingIntent = PendingIntentDep.getImmutableActivity(this, 0, intent);
            notification.setContentIntent(pendingIntent);
        } else {
            notification.setContentTitle(getString(R.string.notification_title_exception_download))
                    .setTicker(getString(R.string.notification_ticker_download_fail))
                    .setContentText(getString(R.string.notification_content_download_fail))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.notification_content_download_fail_expanded)))
                    .setSmallIcon(notificationIcon).setProgress(0, 0, false);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink));
            PendingIntent pendingIntent = PendingIntentDep.getImmutableActivity(this, 0, intent);
            notification.setContentIntent(pendingIntent);
        }
        manager.notify(notificationId, notification.build());
    }

    @SuppressLint("UnspecifiedImmutableFlag")
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
        PendingIntent pendingIntent = PendingIntentDep.getImmutableActivity(this, 0, installIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentTitle(getString(R.string.notification_title_download_success))
                .setTicker(getString(R.string.notification_ticker_download_success))
                .setContentText(getString(R.string.notification_content_download_success))
                .setAutoCancel(true).setContentIntent(pendingIntent)
                .setSmallIcon(notificationIcon).setProgress(0, 0, false);
        manager.notify(notificationId, notification.build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
