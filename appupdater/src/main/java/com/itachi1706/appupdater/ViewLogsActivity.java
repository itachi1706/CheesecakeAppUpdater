package com.itachi1706.appupdater;

import android.content.ClipData;
import android.content.Intent;
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

import com.itachi1706.appupdater.utils.MigrationHelper;
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper;
import com.itachi1706.helperlib.utils.NotifyUserUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class ViewLogsActivity extends AppCompatActivity {

    private TextView logText;
    private int height;
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView v = new ScrollView(this);
        logText = new TextView(this);
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(v, this);

        v.addView(logText);
        logText.setOnLongClickListener(v1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return longClickBtn(v1);
            return true;
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
            Log.e("ViewLogs", "Error viewing app log. Exception: " + e.getMessage(), e);
        }

        DisplayMetrics displayMetrics = MigrationHelper.getDisplayMetricsCompat(this, getWindowManager());
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
                handleShareLogException(e, logTextFull, SHARE_TAG);
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleShareLogException(RuntimeException e, String logTextFull, String shareTag) {
        if (e.getCause() instanceof TransactionTooLargeException) {
            // Too large for intent, switching to save text file and sharing it
            Log.w(shareTag, "Application logs too large, converting to file for sharing");
            File tmp = new File(getCacheDir(), "logs" + File.separator + "log-" + System.currentTimeMillis() + ".txt");
            if (!saveLogToFile(tmp, logTextFull)) {
                NotifyUserUtil.createShortToast(getApplicationContext(), "Unable to share application logs");
                return;
            }
            Log.i(shareTag, "Temp log file created. Generating uri for share");
            Intent shareFileIntent = new Intent(Intent.ACTION_SEND);
            shareFileIntent.setType("text/plain");
            Uri logUri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ? FileProvider.getUriForFile(getBaseContext(),
                    getApplicationContext().getPackageName() + ".appupdater.provider", tmp) : Uri.fromFile(tmp);
            Log.d(shareTag, "Uri generated: " + logUri);
            shareFileIntent.putExtra(Intent.EXTRA_SUBJECT, "Application Logcat for " + getPackageName());
            shareFileIntent.putExtra(Intent.EXTRA_STREAM, logUri);
            shareFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooserIntent = Intent.createChooser(shareFileIntent, "Share Log file for " + getPackageName() + " with");
            startActivity(chooserIntent);
        } else NotifyUserUtil.createShortToast(getApplicationContext(), "Error sharing application logs: " + e.getCause());
    }

    private boolean saveLogToFile(File file, String logs) {
        try {
            if (file.exists() && !file.delete()) return false;
            if (file.getParentFile() != null) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            try (FileWriter fw = new FileWriter(file)) {
                fw.append(logs);
                fw.flush();
            }
            return true;
        } catch (IOException e) {
            Log.e("ViewLogs", "Error saving log to file. Exception: " + e.getMessage(), e);
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
                outShadowSize.set(Math.min(view.getWidth(), width), Math.min(view.getHeight(), height));
                outShadowTouchPoint.set((outShadowSize.x > width) ? width / 2 : outShadowSize.x / 2, (outShadowSize.y > height) ? height / 2 : outShadowSize.y / 2);
            } else {
                outShadowSize.set(1,1);
                outShadowTouchPoint.set(0,0);
            }
        }
    }
}
