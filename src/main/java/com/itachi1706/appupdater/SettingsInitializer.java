package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.appupdater.Util.ValidationHelper;

/**
 * Created by Kenneth on 28/8/2016.
 * for com.itachi1706.appupdater in CheesecakeUtilities
 */
public class SettingsInitializer {

    private Activity context;
    private int mLauncherIcon;
    private String mServerUrl, mLegacyLink, mUpdateLink;

    /**
     * Initializes new Settings Initializer
     * @param activity Activity object that calls this
     * @param launcher_icon Resource Id to an icon to show for any notifications
     * @param serverUrl Base Server URL
     * @param legacyLink URL To full list of app downloads
     * @param updateLink URL to latest app download
     */
    public SettingsInitializer(Activity activity, int launcher_icon, String serverUrl, String legacyLink, String updateLink) {
        this.context = activity;
        this.mLauncherIcon = launcher_icon;
        this.mServerUrl = serverUrl;
        this.mLegacyLink = legacyLink;
        this.mUpdateLink = updateLink;
    }

    /**
     * Explodes settings in your preference fragment
     * @param fragment The preference fragment object
     */
    public void explodeSettings(PreferenceFragment fragment) {
        explodeSettings(fragment, true, true);
    }

    /**
     * Explodes settings in your preference fragment
     * @param fragment The preference fragment object
     * @param showOnlyForSideload Do not show any field if app is not sideloaded
     * @param showInstallLocation Only show install location field if app is sideloaded
     */
    public void explodeSettings(PreferenceFragment fragment, boolean showOnlyForSideload, boolean showInstallLocation) {
        if (showOnlyForSideload && !ValidationHelper.checkSideloaded(context) && !showInstallLocation)
            return; // Sideloaded

        if (showOnlyForSideload && showInstallLocation && !ValidationHelper.checkSideloaded(context)) {
            // Expand minimal
            fragment.addPreferencesFromResource(R.xml.pref_updater_min);
            String installLocation;
            String location = ValidationHelper.getInstallLocation(context);
            switch (ValidationHelper.checkInstallLocation(context)) {
                case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
                case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
                case ValidationHelper.SIDELOAD:
                default: installLocation = "Sideloaded";
            }
            fragment.findPreference("installer_from").setSummary(installLocation);
            return;
        }

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        fragment.addPreferencesFromResource(R.xml.pref_updater);
        fragment.findPreference("launch_updater").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AppUpdateChecker(context, sp, mLauncherIcon, mServerUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return false;
            }
        });

        fragment.findPreference("get_old_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mLegacyLink));
                context.startActivity(i);
                return false;
            }
        });

        fragment.findPreference("get_latest_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mUpdateLink));
                context.startActivity(i);
                return false;
            }
        });

        fragment.findPreference("android_changelog").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UpdaterHelper.settingGenerateChangelog(sp, context);
                return true;
            }
        });

        String installLocation;
        String location = ValidationHelper.getInstallLocation(context);
        switch (ValidationHelper.checkInstallLocation(context)) {
            case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
            case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
            case ValidationHelper.SIDELOAD:
            default: installLocation = "Sideloaded";
        }
        fragment.findPreference("installer_from").setSummary(installLocation);
    }
}
