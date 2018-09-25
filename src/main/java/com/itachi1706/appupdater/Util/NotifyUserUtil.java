package com.itachi1706.appupdater.Util;

import android.content.Context;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.itachi1706.appupdater.R;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.appupdate.Util in AppUpdater.
 */
@SuppressWarnings("unused")
public final class NotifyUserUtil {
    /**
     * Creates a short dismissable snackbar object
     * @param currentLayout Current View Layout
     * @param message Message in Snackbar
     */
    public static void showShortDismissSnackbar(View currentLayout, String message){
        Snackbar.make(currentLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(R.string.snackbar_action_dismiss, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    /**
     * Create a Short Toast Message
     * @param context Application Context
     * @param message Message to display in Toast
     */
    public static void createShortToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
