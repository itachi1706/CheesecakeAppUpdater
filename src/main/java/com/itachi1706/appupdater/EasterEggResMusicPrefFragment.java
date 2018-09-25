package com.itachi1706.appupdater;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Kenneth on 5/11/2016.
 * for com.itachi1706.appupdater in CheesecakeAppUpdater
 */

/**
 * Easter Egg Fragment. Call {@link #addEggMethods()} in your onCreate() method (e.g super.addEggMethods()) to add the easter egg and relevant stuff
 */
public abstract class EasterEggResMusicPrefFragment extends PreferenceFragment implements MediaPlayer.OnCompletionListener {

    public void addEggMethods() {
        addEggMethods(false, null);
    }

    /**
     * Should be called when implementing the easter egg
     */
    public void addEggMethods(boolean openSource, Preference.OnPreferenceClickListener openSourceListener) {
        addPreferencesFromResource(R.xml.pref_appinfo);

        // Check to enable Open Source License View or not
        findPreference("view_oss").setOnPreferenceClickListener(openSourceListener);
        if (!openSource) ((PreferenceCategory) findPreference("info_category")).removePreference(findPreference("view_oss"));

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
        findPreference("vDevInfo").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), DebugInfoActivity.class));
                return true;
            }
        });

        verPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!isActive) {
                    if (count == 10) {
                        count = 0;
                        startEgg();
                        Snackbar.make(getActivity().findViewById(android.R.id.content), getStartEggMessage(), Snackbar.LENGTH_LONG)
                                .setAction(getStopEggButtonText(), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        killEgg();
                                    }
                                }).show();
                        return false;
                    } else if (count > 5)
                        prompt(10 - count);
                    count++;
                }
                return false;
            }
        });
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
