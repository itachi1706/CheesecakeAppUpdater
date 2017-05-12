package com.itachi1706.appupdater;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Kenneth on 5/11/2016.
 * for com.itachi1706.appupdater in CheesecakeAppUpdater
 */

/**
 * Easter Egg Fragment. Call {@link #addEggMethods()} in your onCreate() method (e.g super.addEggMethods()) to add the easter egg and relevant stuff
 */
public abstract class EasterEggResMusicPrefFragment extends PreferenceFragment {

    /**
     * Should be called when implementing the easter egg
     */
    public void addEggMethods() {
        addPreferencesFromResource(R.xml.pref_appinfo);

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
                                        Snackbar.make(getActivity().findViewById(android.R.id.content), getEndEggMessage(), Snackbar.LENGTH_SHORT).show();
                                        endEgg();
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

    private void startEgg() {
        if (!isActive) {
            mp = MediaPlayer.create(getActivity(), getMusicResource());
            mp.start();
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