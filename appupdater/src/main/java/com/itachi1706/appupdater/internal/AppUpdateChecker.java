package com.itachi1706.appupdater.internal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itachi1706.appupdater.NewUpdateActivity;
import com.itachi1706.appupdater.R;
import com.itachi1706.appupdater.object.AppUpdateObject;
import com.itachi1706.appupdater.object.UpdateShell;
import com.itachi1706.appupdater.utils.MigrationHelper;
import com.itachi1706.appupdater.utils.UpdaterHelper;
import com.itachi1706.helperlib.concurrent.CoroutineAsyncTask;
import com.itachi1706.helperlib.deprecation.HtmlDep;
import com.itachi1706.helperlib.helpers.URLHelper;
import com.itachi1706.helperlib.utils.NotifyUserUtil;

import java.io.IOException;
import java.util.Random;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.internal in AppUpdater.
 * NOT FOR NON LIBRARY USE
 */
@SuppressWarnings("unused")
public final class AppUpdateChecker extends CoroutineAsyncTask<Void, Void, String> {

    private final Activity mActivity;
    private final SharedPreferences sp;
    private boolean main = false;
    private final int notificationIcon;
    private final String changelogLocation;
    private final String baseurl;
    private final boolean fullScreen;
    private final boolean internalCache;
    
    private static final String TAG = "Updater";
    private static final String TASK_NAME = AppUpdateChecker.class.getSimpleName();
    private final Random random = new Random();

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param notificationIcon The resource id of the icon to be used in any notifications
     * @param baseurl Base URL to check updates from
     * @param fullScreen Whether to use a Full Screen activity or not
     * @param internalCache Whether to save update APK file in internal or external cache
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, int notificationIcon, String baseurl, boolean fullScreen, boolean internalCache){
        super(TASK_NAME);
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = "version-changelog";
        this.baseurl = baseurl;
        this.fullScreen = fullScreen;
        this.internalCache = internalCache;
    }

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param isMain Whether its calling from the Main activity (hence no need to reply to user if no updates)
     * @param notificationIcon Resource ID of icon to be used in notifications
     * @param baseurl Base Update Checker URL
     * @param fullScreen Whether to use a Full Screen activity or not
     * @param internalCache Whether to save update APK file in internal or external cache
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, boolean isMain, int notificationIcon,
                            String baseurl, boolean fullScreen, boolean internalCache){
        super(TASK_NAME);
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.main = isMain;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = "version-changelog";
        this.baseurl = baseurl;
        this.fullScreen = fullScreen;
        this.internalCache = internalCache;
    }

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param notificationIcon Resource ID of icon to be used in notifications
     * @param changelogLocation Key in Shared Preference where changelog is stored in
     * @param baseurl Base Update Checker URL
     * @param fullScreen Whether to use a Full Screen activity or not
     * @param internalCache Whether to save update APK file in internal or external cache
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, int notificationIcon,
                            String changelogLocation, String baseurl, boolean fullScreen, boolean internalCache){
        super(TASK_NAME);
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = changelogLocation;
        this.baseurl = baseurl;
        this.fullScreen = fullScreen;
        this.internalCache = internalCache;
    }

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param isMain Whether its calling from the Main activity (hence no need to reply to user if no updates)
     * @param notificationIcon Resource ID of icon to be used in notifications
     * @param changelogLocation Key in Shared Preference where changelog is stored in
     * @param baseurl Base Update Checker URL
     * @param fullScreen Whether to use a Full Screen activity or not
     * @param internalCache Whether to save update APK file in internal or external cache
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, boolean isMain, int notificationIcon,
                            String changelogLocation, String baseurl, boolean fullScreen, boolean internalCache){
        super(TASK_NAME);
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.main = isMain;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = changelogLocation;
        this.baseurl = baseurl;
        this.fullScreen = fullScreen;
        this.internalCache = internalCache;
    }


    @Override
    public String doInBackground(@NonNull Void... params) {
        String url = this.baseurl;
        String packageName;
        PackageInfo pInfo;
        try {
            pInfo = mActivity.getApplicationContext().getPackageManager()
                    .getPackageInfo(mActivity.getApplicationContext().getPackageName(), 0);
            packageName = pInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            packageName = "";
        }
        url += packageName;
        URLHelper urlHelper = new URLHelper(url);

        String tmp = "";
        try {
            tmp = urlHelper.executeString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    @Override
    public void onPostExecute(String changelog){
        Log.d("Debug", changelog);

        Gson gson = new Gson();
        UpdateShell shell;
        try {
            shell = gson.fromJson(changelog, UpdateShell.class);
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Invalid JSON, might not have internet");
            return;
        }
        if (shell == null)
            return;
        if (shell.getError() == 20) {
            Log.e(TAG, "Application Not Found!");
            return;
        }
        AppUpdateObject updater = shell.getMsg();

        if (updater == null)
            return;

        long localVersion = 0;
        long serverVersion = 0;
        PackageInfo pInfo;
        try {
            pInfo = mActivity.getApplicationContext().getPackageManager()
                    .getPackageInfo(mActivity.getApplicationContext().getPackageName(), 0);
            localVersion = MigrationHelper.getVersionCodeCompat(pInfo);
            serverVersion = Long.parseLong(updater.getLatestVersionCode());
        } catch (PackageManager.NameNotFoundException | NumberFormatException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Server: " + serverVersion + " | Local: " + localVersion);

        sp.edit().putString(this.changelogLocation, gson.toJson(updater)).apply();

        if (localVersion >= serverVersion) {
            Log.i(TAG, "App is on the latest version");
            if (!main) {
                if (!mActivity.isFinishing()) {
                    new AlertDialog.Builder(mActivity).setTitle(R.string.dialog_title_latest_update)
                            .setMessage(R.string.dialog_message_latest_update)
                            .setNegativeButton(R.string.dialog_action_positive_close, null).show();
                } else {
                    NotifyUserUtil.createShortToast(mActivity.getApplicationContext(),
                            mActivity.getString(R.string.toast_message_latest_update));
                }
            }
            return;
        }

        if (updater.getUpdateMessage().length == 0) {
            Log.e(TAG, "No Update Messages");
            return;
        }

        // Parse Message
        final String updateLink = updater.getUpdateMessage()[0].getUrl();
        Log.i(TAG, "Update Found! Generating Update Message! Latest Version: " +
                updater.getLatestVersion() + " (" + updater.getLatestVersionCode() + ")");
        String message = "Latest Version: " + updater.getLatestVersion() + "<br /><br />";
        message += UpdaterHelper.getChangelogStringFromArray(updater.getUpdateMessage());
        if (!mActivity.isFinishing()) {
            if (fullScreen && Build.VERSION.SDK_INT <= Build.VERSION_CODES.BAKLAVA) {
                // Launch Full Screen Updater
                Intent intent = new Intent(mActivity, NewUpdateActivity.class);
                intent.putExtra("update", gson.toJson(updater));
                intent.putExtra("nicon", notificationIcon);
                intent.putExtra("internalCache", internalCache);
                mActivity.startActivity(intent);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        mActivity.getPackageManager().canRequestPackageInstalls();
                        Log.i(TAG, "REQUEST_INSTALL_PACKAGES permission granted");
                    } catch (SecurityException e) {
                        Log.e(TAG, "REQUEST_INSTALL_PACKAGES permission not granted");
                        new AlertDialog.Builder(mActivity).setTitle(R.string.no_perm_package_install_dialog_title)
                                .setMessage(R.string.no_perm_package_install_dialog_message)
                                .setPositiveButton(R.string.dialog_action_positive_close, null).show();
                        return;
                    }
                }
                new AlertDialog.Builder(mActivity).setTitle("A New Update is Available!")
                        .setMessage(HtmlDep.fromHtml(message))
                        .setNegativeButton("Don't Update", null)
                        .setPositiveButton("Update", (dialog, which) -> {
                            NotificationManager manager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                            if (manager == null) {
                                Log.e(TAG, "Cannot invoke NotificationManager, not found");
                                return;
                            }
                            // Create the Notification Channel
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel mChannel = new NotificationChannel(UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL, "App Updates", NotificationManager.IMPORTANCE_LOW);
                                mChannel.setDescription("Notifications when updating the application");
                                mChannel.enableLights(true);
                                mChannel.setLightColor(Color.GREEN);
                                manager.createNotificationChannel(mChannel);
                            }
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mActivity, UpdaterHelper.UPDATER_NOTIFICATION_CHANNEL);
                            mBuilder.setContentTitle(mActivity.getString(R.string.notification_title_starting_download)).setContentText(mActivity.getString(R.string.notification_content_starting_download))
                                    .setProgress(0, 0, true).setSmallIcon(notificationIcon).setAutoCancel(false)
                                    .setOngoing(true).setTicker(mActivity.getString(R.string.notification_ticker_starting_download));
                            int notificationId = random.nextInt();
                            manager.notify(notificationId, mBuilder.build());
                            new DownloadLatestUpdate(mActivity, mBuilder, manager, notificationId, notificationIcon, internalCache).executeOnExecutor(updateLink);
                        }).show();
            }
        }
    }

}
