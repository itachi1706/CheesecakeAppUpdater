package com.itachi1706.cheesecakeappupdatersample

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.appupdater.EasterEggResMultiMusicPrefFragment
import com.itachi1706.appupdater.SettingsInitializer
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(R.id.settings, this, R.layout.settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
    }

    class SettingsFragment : EasterEggResMultiMusicPrefFragment() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//            val serverUri = "https://api.itachi1706.com/api/appupdatechecker.php?action=androidretrievedata&packagename="
            val serverUri = "https://api.itachi1706.com/v1"
            SettingsInitializer().setFullscreen(true).explodeUpdaterSettings(activity, R.mipmap.ic_launcher, serverUri,
                resources.getString(R.string.link_legacy), resources.getString(R.string.link_updates), this)
                .setAboutApp(true) { Toast.makeText(context, "This will launch about app", Toast.LENGTH_SHORT).show(); true }
                .setOpenSourceLicenseInfo(true) {
                    Toast.makeText(
                        context,
                        "This will launch OSS prompt",
                        Toast.LENGTH_SHORT
                    ).show(); true
                }
                .setIssueTracking(true, "https://itachi1706.atlassian.net/browse/CAUANDLIB")
                .setBugReporting(true, "https://itachi1706.atlassian.net/servicedesk/customer/portal/3")
                .setFDroidRepo(true, "fdroidrepos://fdroid.itachi1706.com/repo?fingerprint=B321F84BCAC7C296CF50923FF98965B11019BB5FD30C8B8F3A39F2F649AF9691")
                .setPathBasedApi(true)
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