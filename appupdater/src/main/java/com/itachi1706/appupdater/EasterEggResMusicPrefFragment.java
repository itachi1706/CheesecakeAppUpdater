package com.itachi1706.appupdater;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.Snackbar;

/**
 * Created by Kenneth on 5/11/2016.
 * for com.itachi1706.appupdater in CheesecakeAppUpdater
 *
 * Easter Egg Fragment. Setup initialization methods and call {@link #build()} in your onCreate() or onCreatePreference() method (e.g super.build()) to add the easter egg and relevant stuff
 */
@SuppressWarnings("ConstantConditions")
public abstract class EasterEggResMusicPrefFragment extends PreferenceFragmentCompat implements MediaPlayer.OnCompletionListener {

    // Easter Egg Initialization (Run these in onCreate)
    private boolean openSource = false, aboutApp = false;
    private Preference.OnPreferenceClickListener openSourceListener = null, aboutAppListener = null;

    /**
     * Whether we should display OSS License Info Option
     * @param enabled true if we should show the option
     * @param listener What to run when the option is selected
     */
    public void setShouldShowOpenSource(boolean enabled, @Nullable Preference.OnPreferenceClickListener listener) {
        this.openSource = enabled;
        this.openSourceListener = listener;
    }

    /**
     * Whether we should display About App Option
     * @param enabled true if we should show option
     * @param listener What to run when option is selected
     */
    public void setShouldShowAboutApp(boolean enabled, @Nullable Preference.OnPreferenceClickListener listener) {
        this.aboutApp = enabled;
        this.aboutAppListener = listener;
    }

    /**
     * Builds the fragment
     */
    public void build() {
        addPreferencesFromResource(R.xml.pref_appinfo);

        // Check to enable Open Source License View or not
        findPreference("view_oss").setOnPreferenceClickListener(openSourceListener);
        if (!openSource) ((PreferenceCategory) findPreference("info_category")).removePreference(findPreference("view_oss"));

        // Check to enable About App View or not
        findPreference("aboutapp").setOnPreferenceClickListener(aboutAppListener);
        if (!aboutApp) ((PreferenceCategory) findPreference("info_category")).removePreference(findPreference("aboutapp"));

        //Debug Info Get
        String version = "NULL", packName = "NULL";
        int versionCode = 0;
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
            packName = pInfo.packageName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Preference verPref = findPreference("view_app_version");
        verPref.setSummary(version + "-b" + versionCode);
        findPreference("view_app_name").setSummary(packName);
        findPreference("view_sdk_version").setSummary(android.os.Build.VERSION.RELEASE);
        findPreference("vDevInfo").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), DebugInfoActivity.class));
            return true;
        });
        findPreference("vAppLog").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), ViewLogsActivity.class));
            return true;
        });

        verPref.setOnPreferenceClickListener(preference -> {
            if (!isActive) {
                if (count == 10) {
                    count = 0;
                    startEgg();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getStartEggMessage(), Snackbar.LENGTH_LONG)
                            .setAction(getStopEggButtonText(), v -> killEgg()).show();
                    return false;
                } else if (count > 5)
                    prompt(10 - count);
                count++;
            }
            return false;
        });
    }

    /**
     * @deprecated Use {{@link #build()}} instead after setting the relevant preference
     */
    @Deprecated
    public void addEggMethods() {
        addEggMethods(false, null);
    }

    /**
     * @deprecated Use {{@link #build()}} instead after setting the relevant preference
     */
    @Deprecated
    public void addEggMethods(boolean openSource, Preference.OnPreferenceClickListener openSourceListener) {
        addEggMethods(openSource, openSourceListener, false, null);
    }

    /**
     * Should be called when implementing the easter egg
     * @param openSource true if to enable OSS license view
     * @param openSourceListener OSS license view listener
     * @param aboutApp true if to enable about app view
     * @param aboutAppListener About App View Listener
     * @deprecated Use {{@link #build()}} instead after setting the relevant preference
     */
    @Deprecated
    public void addEggMethods(boolean openSource, Preference.OnPreferenceClickListener openSourceListener, boolean aboutApp, Preference.OnPreferenceClickListener aboutAppListener) {
        setShouldShowOpenSource(openSource, openSourceListener);
        setShouldShowAboutApp(aboutApp, aboutAppListener);
        build();
    }

    /**
     * Eggs are always nice :wink:
     */

    MediaPlayer mp;
    int count = 0;
    Toast toasty;
    boolean isActive = false;

    private void prompt(int left){
        if (toasty != null)
            toasty.cancel();
        if (left > 1)
            toasty = Toast.makeText(getActivity(), left + " more clicks to have fun!", Toast.LENGTH_SHORT);
        else
            toasty = Toast.makeText(getActivity(), left + " more click to have fun!", Toast.LENGTH_SHORT);
        toasty.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        count = 0;
    }

    @Override
    public void onPause(){
        super.onPause();
        endEgg();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        endEgg();
    }

    public void startEgg() {
        if (!isActive) {
            mp = MediaPlayer.create(getActivity(), getMusicResource());
            mp.start();
            mp.setOnCompletionListener(this);
            isActive = true;
        }
    }

    private void endEgg() {
        count = 0;
        isActive = false;
        if (mp != null){
            if (mp.isPlaying()){
                mp.stop();
                mp.reset();
            }
            mp.release();
            mp = null;
        }
    }

    private void killEgg() {
        Log.i("Egg", "Killing egg");
        Snackbar.make(getActivity().findViewById(android.R.id.content), getEndEggMessage(), Snackbar.LENGTH_SHORT).show();
        endEgg();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i("Egg", "Completed song. Ending...");
        killEgg();
    }

    /**
     * Music file in resource
     * @return Music file res id
     */
    public abstract int getMusicResource();

    /**
     * What to show in snackbar when egg is activated
     * @return Text to show
     */
    public abstract String getStartEggMessage();

    /**
     * What to show in snackbar when egg is stopped manually
     * @return Text to show
     */
    public abstract String getEndEggMessage();

    /**
     * What to show in the button to stop the egg manually
     * @return Text to show
     */
    public abstract String getStopEggButtonText();
}
