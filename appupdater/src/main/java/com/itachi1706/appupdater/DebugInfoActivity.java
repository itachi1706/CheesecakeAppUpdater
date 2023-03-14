package com.itachi1706.appupdater;


import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;


public class DebugInfoActivity extends AppCompatActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {
        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_debug);

            findPreference("view_board_ver").setSummary(Build.BOARD);
            findPreference("view_bootloader_ver").setSummary(Build.BOOTLOADER);
            findPreference("view_brand_ver").setSummary(Build.BRAND);

            String cpu1;
            String cpu2 = "Unused";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                String[] abisArr = Build.SUPPORTED_ABIS;
                // Join abis to a single string seperated by a comma
                StringBuilder abis = new StringBuilder();
                for (String abi : abisArr) {
                    abis.append(abi).append(", ");
                }
                cpu1 = abis.toString();
            } else {
                cpu1 = Build.CPU_ABI;
                cpu2 = Build.CPU_ABI2;
            }

            findPreference("view_cpu1_ver").setSummary(cpu1);
            findPreference("view_cpu2_ver").setSummary(cpu2);
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
            findPreference("view_tags_ver").setSummary(Build.TAGS);
            findPreference("view_type_ver").setSummary(Build.TYPE);
            if (Build.USER != null) {
                findPreference("view_user_ver").setSummary(Build.USER);
            }
        }
    }
}
