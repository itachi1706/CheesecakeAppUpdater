package com.itachi1706.appupdater.Util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kenneth on 20/8/2016.
 * for com.itachi1706.appupdater.Util in AppUpdater
 */
public class ValidationHelper {

    public static final int GOOGLE_PLAY = 1;    //com.android.vending, com.google.android.feedback
    public static final int AMAZON = 2;         //com.amazon.venezia
    public static final int SIDELOAD = 0;

    private static List<String> playstoreList = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
    private static List<String> amazonList = new ArrayList<>(Collections.singletonList("com.amazon.venezia"));

    public static boolean checkNotSideloaded(Context context) {
        List<String> mergedList = new ArrayList<>();
        mergedList.addAll(playstoreList);
        mergedList.addAll(amazonList);

        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());

        return installer != null && mergedList.contains(installer);
    }
    
    public static boolean checkSideloaded(Context context) {
        return !checkNotSideloaded(context);
    }

    public static int checkInstallLocation(Context context) {
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        if (installer == null) return SIDELOAD;
        if (playstoreList.contains(installer)) return GOOGLE_PLAY;
        if (amazonList.contains(installer)) return AMAZON;
        return SIDELOAD;
    }


}
