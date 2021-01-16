package com.itachi1706.cheesecakeappupdatersample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.itachi1706.cheesecakeappupdatersample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}
