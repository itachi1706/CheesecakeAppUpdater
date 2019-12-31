package com.itachi1706.appupdater;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
            Intent shareTextIntent = new Intent(Intent.ACTION_SEND);
            shareTextIntent.setType("text/plain");
            shareTextIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Application Logcat for " + getPackageName());
            shareTextIntent.putExtra(Intent.EXTRA_TEXT, getPackageName() + " Logs:\n\n" + logText.getText().toString());
            startActivity(Intent.createChooser(shareTextIntent, "Share Logs for " + getPackageName() + " with"));
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.N)
    public boolean longClickBtn(View v) {
        ClipData clip = ClipData.newPlainText("logs", ((TextView) v).getText().toString());
        View.DragShadowBuilder dragShadowBuilder = new LogsDragShadowBuilder(v);
        v.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL|View.DRAG_FLAG_GLOBAL_URI_READ|
                View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
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
