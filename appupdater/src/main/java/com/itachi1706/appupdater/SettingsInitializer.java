package com.itachi1706.appupdater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceFragment;
import android.util.TypedValue;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.itachi1706.appupdater.utils.UpdaterHelper;
import com.itachi1706.appupdater.internal.AppUpdateChecker;
import com.itachi1706.helperlib.helpers.PrefHelper;
import com.itachi1706.helperlib.helpers.ValidationHelper;

import java.security.InvalidParameterException;

/**
 * Created by Kenneth on 28/8/2016.
 * for com.itachi1706.appupdater in CheesecakeUtilities
 */
@SuppressWarnings("unused")
public final class SettingsInitializer {

    @Deprecated private Activity context;
    @Deprecated private int notificationIcon;
    private boolean fullscreen = false, oss = false, aboutapp = false, issuetracking = false, bugreport = false, fdroid = false;
    @Deprecated private android.preference.Preference.OnPreferenceClickListener ossListenerDeprecated = null;
    private Preference.OnPreferenceClickListener ossListener = null, aboutAppListener = null;
    private String issueTrackingURL = null, bugReportURL = null, fdroidURL = null;
    @Deprecated private String serverUrl, legacyLink, updateLink;
    private boolean internalCache = false;
    private boolean showOnlyForSideload = true, showInstallLocation = true;

    private static final String FDROID_REPO = "fdroid", ISSUE_TRACKING = "issuetracker", ABOUT_APP = "aboutapp", VIEW_OSS = "view_oss", BUG_REPORT = "bugreport", CATEGORY_INFO = "info_category";

    /**
     * Initializes new Settings Initializer
     */
    public SettingsInitializer() {}

    /**
     * Initializes new Settings Initializer
     * @param activity Activity object that calls this
     * @param launcher_icon Resource Id to an icon to show for any notifications
     * @param serverUrl Base Server URL
     * @param legacyLink URL To full list of app downloads
     * @param updateLink URL to latest app download
     * @deprecated Use the relevant flag setting methods and {@link SettingsInitializer#explodeUpdaterSettings(Activity, int, String, String, String, PreferenceFragment)}
     * in {@link SettingsInitializer} and the new {@link SettingsInitializer#SettingsInitializer()} constructor instead
     */
    @Deprecated
    public SettingsInitializer(Activity activity, int launcher_icon, String serverUrl, String legacyLink, String updateLink) {
        this.context = activity;
        this.notificationIcon = launcher_icon;
        this.serverUrl = serverUrl;
        this.legacyLink = legacyLink;
        this.updateLink = updateLink;
    }

    /**
     * Initializes new Settings Initializer
     * @param activity Activity object that calls this
     * @param launcher_icon Resource Id to an icon to show for any notifications
     * @param serverUrl Base Server URL
     * @param legacyLink URL To full list of app downloads
     * @param updateLink URL to latest app download
     * @param fullscreen Use Full Screen Update Activity
     * @deprecated Use the relevant flag setting methods and {@link SettingsInitializer#explodeUpdaterSettings(Activity, int, String, String, String, PreferenceFragment)}
     * in {@link SettingsInitializer} and the new {@link SettingsInitializer#SettingsInitializer()} constructor instead
     */
    @Deprecated
    public SettingsInitializer(Activity activity, int launcher_icon, String serverUrl, String legacyLink, String updateLink, boolean fullscreen) {
        this.context = activity;
        this.notificationIcon = launcher_icon;
        this.serverUrl = serverUrl;
        this.legacyLink = legacyLink;
        this.updateLink = updateLink;
        this.fullscreen = fullscreen;
    }

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
        this.fullscreen = showInstallLocation;
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
     * @deprecated Use {{@link #setOpenSourceLicenseInfo(boolean, Preference.OnPreferenceClickListener)} instead
     */
    @Deprecated
    public SettingsInitializer setOpenSourceLicenseInfo(boolean enabled, android.preference.Preference.OnPreferenceClickListener listener) {
        this.oss = enabled;
        this.ossListenerDeprecated = listener;
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

    @Deprecated
    private boolean hasAllNeededForUpdate() {
        return !(this.updateLink == null || this.legacyLink == null || this.serverUrl == null);
    }

    /**
     * Explodes general info settings in your preference fragment
     * @param fragment The preference fragment object
     * @return The instance itself
     * @deprecated Use {@link #explodeInfoSettings(PreferenceFragmentCompat)} instead
     */
    @Deprecated
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
        android.preference.Preference verPref = fragment.findPreference("view_app_version");
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
        // Check to enable Open Source License View or not
        fragment.findPreference(VIEW_OSS).setOnPreferenceClickListener(ossListenerDeprecated);
        if (!this.oss) ((android.preference.PreferenceCategory) fragment.findPreference(CATEGORY_INFO)).removePreference(fragment.findPreference(VIEW_OSS));
        ((android.preference.PreferenceCategory) fragment.findPreference(CATEGORY_INFO)).removePreference(fragment.findPreference(ABOUT_APP)); // Permenantly remove for this
        ((android.preference.PreferenceCategory) fragment.findPreference(CATEGORY_INFO)).removePreference(fragment.findPreference(ISSUE_TRACKING)); // Permenantly remove for this
        ((android.preference.PreferenceCategory) fragment.findPreference(CATEGORY_INFO)).removePreference(fragment.findPreference(BUG_REPORT)); // Permenantly remove for this
        ((android.preference.PreferenceCategory) fragment.findPreference(CATEGORY_INFO)).removePreference(fragment.findPreference(FDROID_REPO)); // Permenantly remove for this
        return this;
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param fragment The preference fragment object
     * @return The instance itself
     * @deprecated Use {@link #explodeUpdaterSettings(Activity, int, String, String, String, PreferenceFragment)} instead}
     */
    @Deprecated
    public SettingsInitializer explodeSettings(PreferenceFragment fragment) {
        return explodeUpdaterSettings(fragment, true, true);
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param fragment The preference fragment object
     * @return The instance itself
     * @deprecated Use {@link #explodeUpdaterSettings(Activity, int, String, String, String, PreferenceFragment)} instead
     */
    @Deprecated
    public SettingsInitializer explodeUpdaterSettings(PreferenceFragment fragment) {
        return explodeUpdaterSettings(fragment, true, true);
    }

    /**
     * Explodes updater settings in your preference fragment
     * @param activity The activity object
     * @param notificationIcon Notification Icon Resource ID
     * @param serverUrl Base Server URL
     * @param legacyLink URL To full list of app downloads
     * @param updateLink URL to latest app download
     * @param fragment The preference fragment object
     * @return The instance of this object for chaining
     * @deprecated Use {@link #explodeUpdaterSettings(Activity, int, String, String, String, PreferenceFragmentCompat)} instead
     */
    @Deprecated
    public SettingsInitializer explodeUpdaterSettings(Activity activity, @DrawableRes final int notificationIcon,
                                                      final String serverUrl, final String legacyLink, final String updateLink,
                                                      PreferenceFragment fragment) {
        final Activity context = activity;
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
        fragment.findPreference("launch_updater").setOnPreferenceClickListener(preference -> {
            new AppUpdateChecker(context, sp, notificationIcon, serverUrl, fullscreen, internalCache).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return false;
        });

        fragment.findPreference("get_old_app").setOnPreferenceClickListener(preference -> launchCustomTabs(context, legacyLink));
        fragment.findPreference("get_latest_app").setOnPreferenceClickListener(preference -> launchCustomTabs(context, updateLink));

        fragment.findPreference("android_changelog").setOnPreferenceClickListener(preference -> {
            UpdaterHelper.settingGenerateChangelog(sp, context);
            return true;
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

    /**
     * Explodes updater settings in your preference fragment
     * @param fragment The preference fragment object
     * @param showOnlyForSideload Do not show any field if app is not sideloaded
     * @param showInstallLocation Only show install location field if app is sideloaded
     * @return The instance itself
     * @deprecated Use {@link SettingsInitializer#explodeUpdaterSettings(Activity, int, String, String, String, PreferenceFragment)} instead
     * Note that {@link #showOnlyForSideload(boolean)} and {@link #showInstallLocation(boolean)} now exists as flag setters
     */
    @Deprecated
    @SuppressWarnings("WeakerAccess")
    public SettingsInitializer explodeUpdaterSettings(PreferenceFragment fragment, boolean showOnlyForSideload, boolean showInstallLocation) {
        this.showOnlyForSideload = showOnlyForSideload;
        this.showInstallLocation = showInstallLocation;
        if (!hasAllNeededForUpdate()) throw new InvalidParameterException("You need to set the appropriate setters for this to work");
        return explodeUpdaterSettings(this.context, this.notificationIcon, this.serverUrl, this.legacyLink, this.updateLink, fragment);
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
        String version = "NULL", packName = "NULL";
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
        prefCheckToggle(check, pref, fragment, preference -> launchCustomTabs(fragment.getContext(), url));
    }

    private boolean launchCustomTabs(Context context, String url) {
        if (url != null) {
            final CustomTabsIntent cti = getCustomTabs(context);
            cti.launchUrl(context, Uri.parse(url));
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
        if (showOnlyForSideload && !ValidationHelper.checkSideloaded(mActivity) && !showInstallLocation) return this; // Sideloaded
        if (showOnlyForSideload && showInstallLocation && !ValidationHelper.checkSideloaded(mActivity)) {
            // Expand minimal
            fragment.addPreferencesFromResource(R.xml.pref_updater_min);
            String installLocation;
            String location = ValidationHelper.getInstallLocation(mActivity);
            switch (ValidationHelper.checkInstallLocation(mActivity)) {
                case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
                case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
                case ValidationHelper.SIDELOAD:
                default: installLocation = "Sideloaded"; if (location != null) installLocation += " (" + location + ")"; break;
            }
            fragment.findPreference("installer_from").setSummary(installLocation);
            return this;
        }

        final SharedPreferences sp = PrefHelper.getDefaultSharedPreferences(mActivity);
        fragment.addPreferencesFromResource(R.xml.pref_updater);
        fragment.findPreference("launch_updater").setOnPreferenceClickListener(preference -> {
            new AppUpdateChecker(mActivity, sp, notificationIcon, serverUrl, fullscreen, internalCache).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return false;
        });

        fragment.findPreference("get_old_app").setOnPreferenceClickListener(preference -> launchCustomTabs(mActivity, legacyLink));
        fragment.findPreference("get_latest_app").setOnPreferenceClickListener(preference -> launchCustomTabs(mActivity, updateLink));
        fragment.findPreference("android_changelog").setOnPreferenceClickListener(preference -> {
            UpdaterHelper.settingGenerateChangelog(sp, mActivity);
            return true;
        });

        String installLocation;
        String location = ValidationHelper.getInstallLocation(mActivity);
        switch (ValidationHelper.checkInstallLocation(mActivity)) {
            case ValidationHelper.GOOGLE_PLAY: installLocation = "Google Play (" + location + ")"; break;
            case ValidationHelper.AMAZON: installLocation = "Amazon App Store (" + location + ")"; break;
            case ValidationHelper.SIDELOAD:
            default: installLocation = "Sideloaded";
        }
        fragment.findPreference("installer_from").setSummary(installLocation);
        return this;
    }
}
