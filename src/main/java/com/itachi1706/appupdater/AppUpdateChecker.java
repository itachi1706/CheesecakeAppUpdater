package com.itachi1706.appupdater;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.itachi1706.appupdater.Objects.AppUpdateObject;
import com.itachi1706.appupdater.Objects.UpdateShell;
import com.itachi1706.appupdater.Util.NotifyUserUtil;
import com.itachi1706.appupdater.Util.UpdaterHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate in AppUpdater.
 */
public class AppUpdateChecker extends AsyncTask<Void, Void, String> {

    private Activity mActivity;
    private Exception except = null;
    private SharedPreferences sp;
    ArrayList<String> changelogStrings = new ArrayList<>();
    private boolean main = false;
    private int notificationIcon;
    private String changelogLocation;
    private String baseurl;

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param notificationIcon The resource id of the icon to be used in any notifications
     * @param baseurl Base URL to check updates from
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, int notificationIcon, String baseurl){
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = "version-changelog";
        this.baseurl = baseurl;
    }

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param isMain Whether its calling from the Main activity (hence no need to reply to user if no updates)
     * @param notificationIcon Resource ID of icon to be used in notifications
     * @param baseurl Base Update Checker URL
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, boolean isMain, int notificationIcon, String baseurl){
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.main = isMain;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = "version-changelog";
        this.baseurl = baseurl;
    }

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param notificationIcon Resource ID of icon to be used in notifications
     * @param changelogLocation Key in Shared Preference where changelog is stored in
     * @param baseurl Base Update Checker URL
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, int notificationIcon, String changelogLocation, String baseurl){
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.notificationIcon = notificationIcon;
        this.changelogLocation = changelogLocation;
        this.baseurl = baseurl;
    }

    /**
     * Initialize App Update Checker
     * @param activity Android Activity calling the function
     * @param sharedPrefs Shared Preference Manager Object
     * @param isMain Whether its calling from the Main activity (hence no need to reply to user if no updates)
     * @param notificationIcon Resource ID of icon to be used in notifications
     * @param changelogLocation Key in Shared Preference where changelog is stored in
     * @param baseurl Base Update Checker URL
     */
    public AppUpdateChecker(Activity activity, SharedPreferences sharedPrefs, boolean isMain, int notificationIcon, String changelogLocation, String baseurl){
        this.mActivity = activity;
        this.sp = sharedPrefs;
        this.main = isMain;
        this.changelogLocation = changelogLocation;
        this.baseurl = baseurl;
    }


    @Override
    protected String doInBackground(Void... params) {
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
        String tmp = "";
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(UpdaterHelper.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(UpdaterHelper.HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            changelogStrings.clear();
            while((line = reader.readLine()) != null)
            {
                str.append(line).append("\n");
                changelogStrings.add(line);
            }
            in.close();
            tmp = str.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    protected void onPostExecute(String changelog){
        if (except != null){
            NotifyUserUtil.createShortToast(mActivity.getApplicationContext(),
                    mActivity.getString(R.string.toast_cannot_contact_update_server));
            return;
        }
        Log.d("Debug", changelog);

        Gson gson = new Gson();
        UpdateShell shell = gson.fromJson(changelog, UpdateShell.class);
        if (shell == null)
            return;
        if (shell.getError() == 20) {
            Log.e("Updater", "Application Not Found!");
            return;
        }
        AppUpdateObject updater = shell.getMsg();

        if (updater == null)
            return;

        int localVersion = 0;
        int serverVersion = 0;
        PackageInfo pInfo;
        try {
            pInfo = mActivity.getApplicationContext().getPackageManager()
                    .getPackageInfo(mActivity.getApplicationContext().getPackageName(), 0);
            localVersion = pInfo.versionCode;
            serverVersion = Integer.parseInt(updater.getLatestVersionCode());
        } catch (PackageManager.NameNotFoundException | NumberFormatException e) {
            e.printStackTrace();
        }

        Log.i("Updater", "Server: " + serverVersion + " | Local: " + localVersion);

        sp.edit().putString(this.changelogLocation, gson.toJson(updater)).apply();

        if (localVersion >= serverVersion) {
            Log.i("Updater", "App is on the latest version");
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
            Log.e("Updater", "No Update Messages");
            return;
        }

        // Parse Message
        final String updateLink = updater.getUpdateMessage()[0].getUrl();
        Log.i("Updater", "Update Found! Generating Update Message! Latest Version: " +
                updater.getLatestVersion() + " (" + updater.getLatestVersionCode() + ")");
        String message = "Latest Version: " + updater.getLatestVersion() + "<br /><br />";
        message += UpdaterHelper.getChangelogStringFromArray(updater.getUpdateMessage());
        if (!mActivity.isFinishing()) {
            new AlertDialog.Builder(mActivity).setTitle("A New Update is Available!")
                    .setMessage(Html.fromHtml(message))
                    .setNegativeButton("Don't Update", null)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NotificationManager manager = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mActivity);
                            mBuilder.setContentTitle(mActivity.getString(R.string.notification_title_starting_download)).setContentText(mActivity.getString(R.string.notification_content_starting_download))
                                    .setProgress(0, 0, true).setSmallIcon(notificationIcon).setAutoCancel(false)
                                    .setOngoing(true).setTicker(mActivity.getString(R.string.notification_ticker_starting_download));
                            Random random = new Random();
                            int notificationId = random.nextInt();
                            manager.notify(notificationId, mBuilder.build());
                            new DownloadLatestUpdate(mActivity, mBuilder, manager, notificationId, notificationIcon).executeOnExecutor(THREAD_POOL_EXECUTOR, updateLink);
                        }
                    }).show();
        }
    }

}
