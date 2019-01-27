package com.itachi1706.appupdater;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import androidx.appcompat.app.AppCompatActivity;

public class ViewLogsActivity extends AppCompatActivity {

    private TextView logText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView v = new ScrollView(this);
        setContentView(v);
        logText = new TextView(this);
        v.addView(logText);

        //logText.setMovementMethod(new ScrollingMovementMethod());
        //logText.setVerticalScrollBarEnabled(true);

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
}
