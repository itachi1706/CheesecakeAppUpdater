package com.itachi1706.appupdater;

import android.media.MediaPlayer;
import android.util.Log;

import java.util.Random;

/**
 * Created by Kenneth on 19/4/2018.
 * for com.itachi1706.appupdater in SingBuses
 */
@SuppressWarnings("unused")
public abstract class EasterEggResMultiMusicPrefFragment extends EasterEggResMusicPrefFragment {

    private int randomNum = -1;
    private final Random random = new Random();

    private void randomizeNumber() {
        randomNum = random.nextInt();
        Log.d("MusicEgg", "Random Number: " + randomNum);
    }

    /**
     * Check if RNG number is even
     * @return true if even, false if odd
     */
    public boolean randomNumberIsEven() {
        return (randomNum & 1) == 0;
    }

    /**
     * Returns a number from 0 to max
     * E.g if max is 10, you will get a number from 1 - 9
     * @param max int Maximum number
     * @return Number generated by RNG
     */
    public int randomNumberCount(int max) {
        return randomNum % max;
    }

    @Override
    public void startEgg() {
        if (!isActive) {
            randomizeNumber();
            mp = MediaPlayer.create(getActivity(), getMusicResource());
            mp.start();
            mp.setOnCompletionListener(this);
            isActive = true;
        }
    }
}
