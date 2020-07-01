package com.itachi1706.appupdater;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.TransactionTooLargeException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.itachi1706.helperlib.utils.NotifyUserUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ViewLogsActivity extends AppCompatActivity {

    private TextView logText;
    private int height, width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView v = new ScrollView(this);
        logText = new TextView(this);
        setContentView(v);

        v.addView(logText);
        logText.setOnLongClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return longClickBtn(v1);
            return true;
        });

        StringBuilder log = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line).append("\n");
            }
            logText.setText(log.toString());
        } catch (IOException e) {
            Log.e("ViewLogs", "Error viewing app log. Exception: " + e.toString());
            e.printStackTrace();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels - 100;
        width = displayMetrics.widthPixels - 100;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ccn_menu_view_logs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ccn_share_logcat) {
            final String SHARE_TAG = "ShareLogs";
            Log.i(SHARE_TAG, "Attempting to share Application log via plaintext"); // Try sharing plaintext of logs first
            Intent shareTextIntent = new Intent(Intent.ACTION_SEND);
            shareTextIntent.setType("text/plain");
            shareTextIntent.putExtra(Intent.EXTRA_SUBJECT, "Application Logcat for " + getPackageName());
            String logTextFull = getPackageName() + " Logs:\n\n" + logText.getText().toString();
            shareTextIntent.putExtra(Intent.EXTRA_TEXT, logTextFull);
            try {
                startActivity(Intent.createChooser(shareTextIntent, "Share Logs for " + getPackageName() + " with"));
            } catch (RuntimeException e) {
                if (e.getCause() instanceof TransactionTooLargeException) {
                    // Too large for intent, switching to save text file and sharing it
                    Log.w(SHARE_TAG, "Application logs too large, converting to file for sharing");
                    File tmp = new File(getCacheDir(), "logs" + File.separator + "log-" + System.currentTimeMillis() + ".txt");
                    if (!saveLogToFile(tmp, logTextFull)) {
                        NotifyUserUtil.createShortToast(getApplicationContext(), "Unable to share application logs");
                        return true;
                    }
                    Log.i(SHARE_TAG, "Temp log file created. Generating uri for share");
                    Intent shareFileIntent = new Intent(Intent.ACTION_SEND);
                    shareFileIntent.setType("text/plain");
                    Uri logUri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? FileProvider.getUriForFile(getBaseContext(),
                            getApplicationContext().getPackageName() + ".appupdater.provider", tmp) : Uri.fromFile(tmp);
                    Log.d(SHARE_TAG, "Uri generated: " + logUri);
                    shareFileIntent.putExtra(Intent.EXTRA_SUBJECT, "Application Logcat for " + getPackageName());
                    shareFileIntent.putExtra(Intent.EXTRA_STREAM, logUri);
                    Intent chooserIntent = Intent.createChooser(shareFileIntent, "Share Log file for " + getPackageName() + " with");
                    List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY); // Grant permissions for intents
                    for (ResolveInfo resolveInfo : resInfoList) this.grantUriPermission(resolveInfo.activityInfo.packageName, logUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(chooserIntent);
                } else NotifyUserUtil.createShortToast(getApplicationContext(), "Error sharing application logs: " + e.getCause());
            }
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private boolean saveLogToFile(File file, String logs) {
        try {
            if (file.exists() && !file.delete()) return false;
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(file);
            fw.append(logs);
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.N)
    public boolean longClickBtn(View v) {
        ClipData clip = ClipData.newPlainText("logs", ((TextView) v).getText().toString());
        View.DragShadowBuilder dragShadowBuilder = new LogsDragShadowBuilder(v);
        if (!v.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ | View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION))
            NotifyUserUtil.createShortToast(getApplicationContext(), "Application logs are too large to do this. Please share it instead");
        return true;
    }

    class LogsDragShadowBuilder extends View.DragShadowBuilder {

        LogsDragShadowBuilder(View view) {
            super(view);
        }

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            final View view = super.getView();
            if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
                outShadowSize.set((view.getWidth() > width) ? width : view.getWidth(), (view.getHeight() > height) ? height : view.getHeight());
                outShadowTouchPoint.set((outShadowSize.x > width) ? width / 2 : outShadowSize.x / 2, (outShadowSize.y > height) ? height / 2 : outShadowSize.y / 2);
            } else {
                outShadowSize.set(1,1);
                outShadowTouchPoint.set(0,0);
            }
        }
    }
}
