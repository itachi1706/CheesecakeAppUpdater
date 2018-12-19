package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.TypedValue;

import com.itachi1706.appupdater.Util.PrefHelper;
import com.itachi1706.appupdater.Util.UpdaterHelper;
import com.itachi1706.appupdater.Util.ValidationHelper;
import com.itachi1706.appupdater.internal.AppUpdateChecker;

import java.security.InvalidParameterException;

import androidx.browser.customtabs.CustomTabsIntent;

/**
 * Created by Kenneth on 28/8/2016.
 * for com.itachi1706.appupdater in CheesecakeUtilities
 */
@SuppressWarnings("unused")
public final class SettingsInitializer {

    private Activity context;
    private int mLauncherIcon;
    private boolean fullscreen = false, oss = false;
    private Preference.OnPreferenceClickListener ossListener = null;
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
     * Initializes new Settings Initializer
     * @param activity Activity object that calls this
     * @param launcher_icon Resource Id to an icon to show for any notifications
     * @param serverUrl Base Server URL
     * @param legacyLink URL To full list of app downloads
     * @param updateLink URL to latest app download
     * @param fullscreen Use Full Screen Update Activity
     */
    public SettingsInitializer(Activity activity, int launcher_icon, String serverUrl, String legacyLink, String updateLink, boolean fullscreen) {
        this.context = activity;
        this.mLauncherIcon = launcher_icon;
        this.mServerUrl = serverUrl;
        this.mLegacyLink = legacyLink;
        this.mUpdateLink = updateLink;
        this.fullscreen = fullscreen;
    }

    /**
     * Initializes new Settings Initializer
     * NOTE: This is only for exploding info settings. If you want the updater setting, either use the other constructor
     * or set a launcher icon, serverURL, legacyLink and updateLink
     * @param activity Activity object that calls this
     */
    public SettingsInitializer(Activity activity) {
        this.context = activity;
    }
    public SettingsInitializer setmLauncherIcon(int mLauncherIcon) {
        this.mLauncherIcon = mLauncherIcon;
        return this;
    }

    public SettingsInitializer setmServerUrl(String mServerUrl) {
        this.mServerUrl = mServerUrl;
        return this;
    }

    public SettingsInitializer setmLegacyLink(String mLegacyLink) {
        this.mLegacyLink = mLegacyLink;
        return this;
    }
    public SettingsInitializer setmUpdateLink(String mUpdateLink) {
        this.mUpdateLink = mUpdateLink;
        return this;
    }

    /**
     * To enable the Open Source License Preference where on user click, you could use to show the licenses in your app
     * NOTE: If you use a EasterEgg fragment, set openSource to true and provide the listener there instead
     * @param enabled Whether to show the preference or not
     * @param listener Action to do when user clicks on the app (null for no action)
     */
    public SettingsInitializer setOpenSourceLicenseInfo(boolean enabled, Preference.OnPreferenceClickListener listener) {
        this.oss = enabled;
        this.ossListener = listener;
        return this;
    }

    private boolean hasAllNeededForUpdate() {
        return !(this.mUpdateLink == null || this.mLegacyLink == null || this.mServerUrl == null);
    }

    /**
     * Explodes general info settings in your preference fragment
     * NOTE: This is automatically exploded if you use a EasterEgg fragment
     * @param fragment The preference fragment object
     */
    @SuppressWarnings("WeakerAccess")
    public SettingsInitializer explodeInfoSettings(final PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.pref_appinfo);

        //Debug Info Get
        String version = "NULL", packName = "NULL";
        int versionCode = 0;
        try {
            PackageInfo pInfo = fragment.getActivity().getPackageManager().getPackageInfo(fragment.getActivity().getPackageName(), 0);
            version = pInfo.versionName;
            packName = pInfo.packageName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Preference verPref = fragment.findPreference("view_app_version");
        verPref.setSummary(version + "-b" + versionCode);
        fragment.findPreference("view_app_name").setSummary(packName);
        fragment.findPreference("view_sdk_version").setSummary(android.os.Build.VERSION.RELEASE);
        fragment.findPreference("vDevInfo").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                fragment.startActivity(new Intent(fragment.getActivity(), DebugInfoActivity.class));
                return true;
            }
        });
        // Check to enable Open Source License View or not
        fragment.findPreference("view_oss").setOnPreferenceClickListener(ossListener);
        if (!this.oss) ((PreferenceCategory) fragment.findPreference("info_category")).removePreference(fragment.findPreference("view_oss"));
        return this;
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param fragment The preference fragment object
     * @deprecated Use {@link #explodeUpdaterSettings(PreferenceFragment) instead}
     */
    public SettingsInitializer explodeSettings(PreferenceFragment fragment) {
        return explodeUpdaterSettings(fragment, true, true);
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param fragment The preference fragment object
     */
    public SettingsInitializer explodeUpdaterSettings(PreferenceFragment fragment) {
        return explodeUpdaterSettings(fragment, true, true);
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param fragment The preference fragment object
     * @param showOnlyForSideload Do not show any field if app is not sideloaded
     * @param showInstallLocation Only show install location field if app is sideloaded
     */
    @SuppressWarnings("WeakerAccess")
    public SettingsInitializer explodeUpdaterSettings(PreferenceFragment fragment, boolean showOnlyForSideload, boolean showInstallLocation) {
        if (!hasAllNeededForUpdate()) throw new InvalidParameterException("You need to set the appropriate setters for this to work");
        if (showOnlyForSideload && !ValidationHelper.checkSideloaded(context) && !showInstallLocation)
            return this; // Sideloaded

        if (showOnlyForSideload && showInstallLocation && !ValidationHelper.checkSideloaded(context)) {
            // Expand minimal
            fragment.addPreferencesFromResource(R.xml.pref_updater_min);
            String installLocation;
            String location = ValidationHelper.getInstallLocation(context);
            switch (ValidationHelper.checkInstallLocation(context)) {
                case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
                case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
                case ValidationHelper.SIDELOAD:
                default: installLocation = "Sideloaded"; if (location != null) installLocation += " (" + location + ")"; break;
            }
            fragment.findPreference("installer_from").setSummary(installLocation);
            return this;
        }

        final SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(context);
        fragment.addPreferencesFromResource(R.xml.pref_updater);
        fragment.findPreference("launch_updater").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AppUpdateChecker(context, sp, mLauncherIcon, mServerUrl, fullscreen).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return false;
            }
        });

        TypedValue colorTmp = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, colorTmp, true);
        final int colorPrimary = colorTmp.data;
        final CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setToolbarColor(colorPrimary)
                .enableUrlBarHiding().setShowTitle(true)
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right).build();

        fragment.findPreference("get_old_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                customTabsIntent.launchUrl(context, Uri.parse(mLegacyLink));
                return false;
            }
        });

        fragment.findPreference("get_latest_app").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                customTabsIntent.launchUrl(context, Uri.parse(mUpdateLink));
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
        return this;
    }
}
