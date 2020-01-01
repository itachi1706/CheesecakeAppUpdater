package com.itachi1706.cheesecakeappupdatersample

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment
import com.itachi1706.appupdater.SettingsInitializer

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
    }

    class SettingsFragment : EasterEggResMultiMusicPrefFragment() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            SettingsInitializer().setFullscreen(true).explodeUpdaterSettings(activity, R.mipmap.ic_launcher, "https://api.itachi1706.com/api/appupdatechecker.php?action=androidretrievedata&packagename=",
                resources.getString(R.string.link_legacy), resources.getString(R.string.link_updates), this)
                .setAboutApp(true) { Toast.makeText(context, "This will launch about app", Toast.LENGTH_SHORT).show(); true }
                .setOpenSourceLicenseInfo(true, Preference.OnPreferenceClickListener{ Toast.makeText(context, "This will launch OSS prompt", Toast.LENGTH_SHORT).show(); true })
                .setIssueTracking(true, "https://itachi1706.atlassian.net/browse/CAUANDLIB")
                .explodeInfoSettings(this)
            super.init()
        }

        override fun getMusicResource(): Int {
            return R.raw.sample_lq
        }

        override fun getStopEggButtonText(): String {
            return "Stop"
        }

        override fun getEndEggMessage(): String {
            return "Music Stopped"
        }

        override fun getStartEggMessage(): String {
            return "Music Start"
        }
    }
}