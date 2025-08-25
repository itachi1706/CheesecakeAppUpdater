package com.itachi1706.cheesecakeappupdatersample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itachi1706.cheesecakeappupdatersample.databinding.ActivityMainBinding
import com.itachi1706.helperlib.temp.EdgeToEdgeHelperTmp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        EdgeToEdgeHelperTmp.setEdgeToEdgeWithContentView(binding.root, this)
        binding.viewSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}
