package com.example.translation_app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.translation_app.databinding.ActivityScreenBinding
import com.example.translation_app.start.StartActivity

class ScreenActivity: AppCompatActivity() {

    private lateinit var binding: ActivityScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val previouslyStarted = prefs.getBoolean(getString(com.example.translation_app.R.string.prev_started), false)
        if (previouslyStarted) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        else {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }



    }
}