package com.itachi1706.appupdater;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


public class DebugInfoActivity extends AppCompatActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        @SuppressWarnings("deprecation")
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_debug);
            getPreferenceManager().setSharedPreferencesMode(MODE_MULTI_PROCESS);

            //Preference prefs =  findPreference("view_board_ver");
            findPreference("view_board_ver").setSummary(Build.BOARD);
            findPreference("view_bootloader_ver").setSummary(Build.BOOTLOADER);
            findPreference("view_brand_ver").setSummary(Build.BRAND);
            findPreference("view_cpu1_ver").setSummary(Build.CPU_ABI);
            findPreference("view_cpu2_ver").setSummary(Build.CPU_ABI2);
            findPreference("view_device_ver").setSummary(Build.DEVICE);
            findPreference("view_display_ver").setSummary(Build.DISPLAY);
            findPreference("view_fingerprint_ver").setSummary(Build.FINGERPRINT);
            findPreference("view_hardware_ver").setSummary(Build.HARDWARE);
            findPreference("view_host_ver").setSummary(Build.HOST);
            findPreference("view_id_ver").setSummary(Build.ID);
            findPreference("view_manufacturer_ver").setSummary(Build.MANUFACTURER);
            findPreference("view_model_ver").setSummary(Build.MODEL);
            findPreference("view_product_ver").setSummary(Build.PRODUCT);
            findPreference("view_radio_ver").setSummary((Build.getRadioVersion().isEmpty()) ? "Unavailable" : Build.getRadioVersion());
            findPreference("view_serial_ver").setSummary(requestSerial());
            findPreference("view_tags_ver").setSummary(Build.TAGS);
            findPreference("view_type_ver").setSummary(Build.TYPE);
            if (Build.USER != null) {
                findPreference("view_user_ver").setSummary(Build.USER);
            }
        }

        private static final int RC_PHONE_STATE = 3;

        private String requestSerial() {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                findPreference("view_serial_ver").setSelectable(true);
                findPreference("view_serial_ver").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.i("PermMan", "Requesting READ_PHONE_STATE permission to view serial number");
                        requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, RC_PHONE_STATE);
                        return false;
                    }
                });
                return "Requires READ_PHONE_STATE permission to view. Click to grant permission";
            }
            return getSerial();
        }

        @SuppressLint({"HardwareIds", "MissingPermission"})
        private String getSerial() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                //noinspection deprecation
                return Build.SERIAL;
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case RC_PHONE_STATE:
                    if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("PermMan", "Phone State Permission granted, Updating SERIAL");
                        findPreference("view_serial_ver").setSummary(getSerial());
                        return;
                    }
                    Log.d("PermMan", "Phone State Permission not granted, Displaying rationale for permission");
                    Toast.makeText(getActivity(), "READ_PHONE_STATE permission not granted, SERIAL cannot be retrieved", Toast.LENGTH_LONG).show();
                    break;
                    default:
                        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }
}
