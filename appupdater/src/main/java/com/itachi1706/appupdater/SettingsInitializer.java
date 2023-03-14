package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.itachi1706.appupdater.internal.AppUpdateChecker;
import com.itachi1706.appupdater.utils.UpdaterHelper;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.helperlib.helpers.ValidationHelper;
import com.itachi1706.helperlib.utils.NotifyUserUtil;

/**
 * Created by Kenneth on 28/8/2016.
 * for com.itachi1706.appupdater in CheesecakeUtilities
 */
@SuppressWarnings("unused")
public final class SettingsInitializer {

    private boolean fullscreen = false;
    private boolean oss = false;
    private boolean aboutapp = false;
    private boolean issuetracking = false;
    private boolean bugreport = false;
    private boolean fdroid = false;
    private Preference.OnPreferenceClickListener ossListener = null;
    private Preference.OnPreferenceClickListener aboutAppListener = null;
    private String issueTrackingURL = null;
    private String bugReportURL = null;
    private String fdroidURL = null;
    private boolean internalCache = false;
    private boolean showOnlyForSideload = true;
    private boolean showInstallLocation = true;

    private static final String FDROID_REPO = "fdroid";
    private static final String ISSUE_TRACKING = "issuetracker";
    private static final String ABOUT_APP = "aboutapp";
    private static final String VIEW_OSS = "view_oss";
    private static final String BUG_REPORT = "bugreport";
    private static final String CATEGORY_INFO = "info_category";

    /**
     * Initializes new Settings Initializer
     */
    public SettingsInitializer() { /* Empty Constructor */ }

    /**
     * Sets if we should use the fullscreen version of the updater
     * @param fullscreen To use full screen updater activity or not
     * @return This object instance for chaining
     */
    public SettingsInitializer setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        return this;
    }

    /**
     * Sets a flag to hide fields if app is not sideloaded
     * This is true by default to prevent Google Play from flagging it
     * @param showOnlyForSideload Do not show any field if app is not sideloaded
     * @return The object to allow chaining
     */
    public SettingsInitializer showOnlyForSideload(boolean showOnlyForSideload) {
        this.showOnlyForSideload = showOnlyForSideload;
        return this;
    }

    /**
     * Sets if we should use the fullscreen version of the updater
     * @param showInstallLocation Only show install location field if app is sideloaded
     * @return This object instance for chaining
     */
    public SettingsInitializer showInstallLocation(boolean showInstallLocation) {
        this.showInstallLocation = showInstallLocation;
        return this;
    }

    /**
     * Sets if app updates should be saved into the internal or external cache
     * Internal: /data/data/packagename/cache/download
     * External: /sdcard/Android/data/packagename/cache/download
     *
     * @param internalCache To save to internal cache if true, external cache if false
     * @return This object instance for chaining
     */
    public SettingsInitializer setInternalCache(boolean internalCache) {
        this.internalCache = internalCache;
        return this;
    }

    /**
     * To enable the Open Source License Preference where on user click, you could use to show the licenses in your app
     * @param enabled Whether to show the preference or not
     * @param listener Action to do when user clicks on the app (null for no action)
     * @return The instance itself
     */
    public SettingsInitializer setOpenSourceLicenseInfo(boolean enabled, @Nullable Preference.OnPreferenceClickListener listener) {
        this.oss = enabled;
        this.ossListener = listener;
        return this;
    }

    /**
     * To enable the About App Preference where on user click, you could use to display information about the application
     * @param enabled Whether to show the preference or not
     * @param listener Action to do when user clicks on the app (null for no action)
     * @return The instance itself
     */
    public SettingsInitializer setAboutApp(boolean enabled, @Nullable Preference.OnPreferenceClickListener listener) {
        this.aboutapp = enabled;
        this.aboutAppListener = listener;
        return this;
    }

    /**
     * To enable the Issue Tracker Preference where on user click, you could link to your issue tracker instead
     * @param enabled Whether to show the preference or not
     * @param url Link to the issue tracker
     * @return The instance itself
     */
    public SettingsInitializer setIssueTracking(boolean enabled, @Nullable String url) {
        this.issuetracking = enabled;
        this.issueTrackingURL = url;
        return this;
    }

    /**
     * To enable the Bug Report Preference where on user click, you could link to your bug report/feature request/support page
     * @param enabled Whether to show the preference or not
     * @param url Link to the bug report page (support page)
     * @return The instance itself
     */
    public SettingsInitializer setBugReporting(boolean enabled, @Nullable String url) {
        this.bugreport = enabled;
        this.bugReportURL = url;
        return this;
    }

    /**
     * To enable the F-Droid Preference where on user click, you could go to F-Droid repo
     * @param enabled Whether to show the preference or not
     * @param repoURL Link to the F-Droid Repository
     * @return The instance itself
     */
    public SettingsInitializer setFDroidRepo(boolean enabled, @Nullable String repoURL) {
        this.fdroid = enabled;
        this.fdroidURL = repoURL;
        return this;
    }

    /**
     * Explodes general info settings in your preference fragment
     * NOTE: This is automatically exploded if you use a EasterEgg fragment
     * @param fragment The preference fragment compat object
     * @return The instance itself
     */
    @SuppressWarnings("ConstantConditions")
    public SettingsInitializer explodeInfoSettings(final PreferenceFragmentCompat fragment) {
        fragment.addPreferencesFromResource(R.xml.pref_appinfo);

        //Debug Info Get
        String version = "NULL";
        String packName = "NULL";
        long versionCode = 0;
        try {
            PackageInfo pInfo = fragment.getActivity().getPackageManager().getPackageInfo(fragment.getActivity().getPackageName(), 0);
            version = pInfo.versionName;
            packName = pInfo.packageName;
            versionCode = PackageInfoCompat.getLongVersionCode(pInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        androidx.preference.Preference verPref = fragment.findPreference("view_app_version");
        verPref.setSummary(version + "-b" + versionCode);
        fragment.findPreference("view_app_name").setSummary(packName);
        fragment.findPreference("view_sdk_version").setSummary(android.os.Build.VERSION.RELEASE);
        fragment.findPreference("vDevInfo").setOnPreferenceClickListener(preference -> {
            fragment.startActivity(new Intent(fragment.getActivity(), DebugInfoActivity.class));
            return true;
        });
        fragment.findPreference("vAppLog").setOnPreferenceClickListener(preference -> {
            fragment.startActivity(new Intent(fragment.getActivity(), ViewLogsActivity.class));
            return true;
        });
        prefCheckToggle(this.oss, VIEW_OSS, fragment, ossListener); // Check to enable Open Source License View or not
        prefCheckToggle(this.aboutapp, ABOUT_APP, fragment, aboutAppListener); // Check to enable About App View or not
        prefCheckToggle(this.issuetracking, ISSUE_TRACKING, fragment, issueTrackingURL); // Check to enable Issue Tracking View or not
        prefCheckToggle(this.bugreport, BUG_REPORT, fragment, bugReportURL); // Check to enable Bug Report View or not
        prefCheckToggle(this.fdroid, FDROID_REPO, fragment, fdroidURL); // Check to enable F-Droid View or not
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    private void prefCheckToggle(boolean check, String pref, PreferenceFragmentCompat fragment, Preference.OnPreferenceClickListener listener) {
        if (!check) ((PreferenceCategory) fragment.findPreference(CATEGORY_INFO)).removePreference(fragment.findPreference(pref));
        else fragment.findPreference(pref).setOnPreferenceClickListener(listener);
    }

    private void prefCheckToggle(boolean check, String pref, PreferenceFragmentCompat fragment, String url) {
        prefCheckToggle(check, pref, fragment, preference -> {
            boolean result = launchCustomTabs(fragment.getContext(), url);
            if (!result) NotifyUserUtil.createShortToast(fragment.getContext(), "F-Droid is not installed on this device!");
            return result;
        });
    }

    private boolean launchCustomTabs(Context context, String url) {
        if (url != null) {
            final CustomTabsIntent cti = getCustomTabs(context);
            try {
                cti.launchUrl(context, Uri.parse(url));
            } catch (ActivityNotFoundException e) {
                Log.e("CustomTabs", "No apps found installed on device supporting this URL launch");
                return false;
            }
        }
        return true;
    }

    private CustomTabsIntent customTabsIntent = null;
    private CustomTabsIntent getCustomTabs(Context context) {
        if (customTabsIntent == null) {
            TypedValue colorTmp = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorPrimary, colorTmp, true);
            final int colorPrimary = colorTmp.data;
            customTabsIntent = new CustomTabsIntent.Builder().setToolbarColor(colorPrimary)
                    .enableUrlBarHiding().setShowTitle(true)
                    .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right).build();
        }
        return customTabsIntent;
    }

    private String getInstallLocation(Activity mActivity) {
        String installLocation;
        String location = ValidationHelper.getInstallLocation(mActivity);
        switch (ValidationHelper.checkInstallLocation(mActivity)) {
            case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
            case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
            case ValidationHelper.SIDELOAD:
            default: installLocation = "Sideloaded"; if (location != null) installLocation += " (" + location + ")"; break;
        }
        return installLocation;
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param activity The activity object
     * @param notificationIcon Notification Icon Resource ID
     * @param serverUrl Base Server URL
     * @param legacyLink URL To full list of app downloads
     * @param updateLink URL to latest app download
     * @param fragment The preference fragment compat object
     * @return The instance of this object for chaining
     */
    @SuppressWarnings("ConstantConditions")
    public SettingsInitializer explodeUpdaterSettings(Activity activity, @DrawableRes final int notificationIcon,
                                                      final String serverUrl, final String legacyLink, final String updateLink,
                                                      PreferenceFragmentCompat fragment) {
        final Activity mActivity = activity;
        if (showOnlyForSideload && !ValidationHelper.checkSideloaded(mActivity)) {
            if (!showInstallLocation) return this; // Sideloaded
            // Expand minimal
            fragment.addPreferencesFromResource(R.xml.pref_updater_min);
            String installLocation = getInstallLocation(mActivity);
            fragment.findPreference("installer_from").setSummary(installLocation);
            return this;
        }

        final SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(mActivity);
        fragment.addPreferencesFromResource(R.xml.pref_updater);
        Preference launchUpdaterPref = fragment.findPreference("launch_updater");
        launchUpdaterPref.setOnPreferenceClickListener(preference -> {
            new AppUpdateChecker(mActivity, sp, notificationIcon, serverUrl, fullscreen, internalCache).executeOnExecutor();
            return false;
        });

        fragment.findPreference("get_old_app").setOnPreferenceClickListener(preference -> launchCustomTabs(mActivity, legacyLink));
        fragment.findPreference("get_latest_app").setOnPreferenceClickListener(preference -> launchCustomTabs(mActivity, updateLink));
        fragment.findPreference("android_changelog").setOnPreferenceClickListener(preference -> {
            UpdaterHelper.settingGenerateChangelog(sp, mActivity);
            return true;
        });

        String installLocation = getInstallLocation(mActivity);
        fragment.findPreference("installer_from").setSummary(installLocation);

        // Check if REQUEST_INSTALL_PACKAGES permission is granted for >= SDK 25
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    mActivity.getPackageManager().canRequestPackageInstalls()) {
                Log.i("PICheck", "Can install package and above Android O");
            }
        } catch (SecurityException e) {
            Log.w("PICheck", "Cannot install packages and device is above Android O");
            fragment.findPreference("updateOnWifi").setEnabled(false);
            launchUpdaterPref.setEnabled(false);
            launchUpdaterPref.setSummary("REQUEST_INSTALL_PACKAGES permission not granted. Grant this permission to check for updates.");
        }

        return this;
    }
}
