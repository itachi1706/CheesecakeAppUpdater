package com.itachi1706.appupdater;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by Kenneth on 5/11/2016.
 * for com.itachi1706.appupdater in CheesecakeAppUpdater
 * <p>
 * Easter Egg Fragment. Setup initialization methods and call {@link #init()} ()} in your onCreate() or onCreatePreference() method (e.g super.build()) to add the easter egg and relevant stuff
 * Initialization methods are done by using {@link SettingsInitializer#explodeInfoSettings(PreferenceFragmentCompat)} after setting the relevant options found in {@link SettingsInitializer}
 */
@SuppressWarnings("ConstantConditions")
public abstract class EasterEggResMusicPrefFragment extends PreferenceFragmentCompat implements MediaPlayer.OnCompletionListener {

    /**
     * Call this when implementing the easter egg after ensuring all initalization you wish to execute has been completed from {@link SettingsInitializer}
     */
    public void init() {
        Preference verPref = findPreference("view_app_version");
        verPref.setOnPreferenceClickListener(preference -> {
            if (!isActive) {
                if (count == 10) {
                    count = 0;
                    startEgg();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getStartEggMessage(), BaseTransientBottomBar.LENGTH_LONG)
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
        Snackbar.make(getActivity().findViewById(android.R.id.content), getEndEggMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
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
