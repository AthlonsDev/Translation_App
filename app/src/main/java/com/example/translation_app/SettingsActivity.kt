package com.example.translation_app

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.translation_app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext


class SettingsActivity: AppCompatActivity() {

    var isDataEmpty = true

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkData()

    }

    fun checkData() {
        readData()
        if (isDataEmpty) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
        }
        else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun readData() = runBlocking {
        launch {
            readAllUserPreferences()
        }
    }

    private suspend fun readAllUserPreferences() {
        with(CoroutineScope(coroutineContext)) {
            val dataOutputKey1 = stringPreferencesKey("speech_language_1")
            val dataOutputKey2 = stringPreferencesKey("speech_language_2")
            val dataOutputKey3 = stringPreferencesKey("camera_language")
            val dataOutputKey4 = stringPreferencesKey("text_language")
            val dataOutputKey5 = stringPreferencesKey("alphabet_language_1")
            val preferences = dataStore.data.first()
            val languageOutput1 = preferences?.get(dataOutputKey1)
            val languageOutput2 = preferences?.get(dataOutputKey2)
            val languageOutput3 = preferences?.get(dataOutputKey3)
            val languageOutput4 = preferences?.get(dataOutputKey4)
            val languageOutput5 = preferences?.get(dataOutputKey5)

            when {
                languageOutput1.equals("Not Set") || languageOutput1 == null -> {
                    isDataEmpty = true
                }
                languageOutput2.equals("Not Set") || languageOutput2 == null -> {
                    isDataEmpty = true
                }
                languageOutput3.equals("Not Set") || languageOutput3 == null -> {
                    isDataEmpty = true
                }
                languageOutput4.equals("Not Set") || languageOutput4 == null -> {
                    isDataEmpty = true
                }
                languageOutput5.equals("Not Set") || languageOutput5 == null -> {
                    isDataEmpty = true
                }

                else -> {
                    isDataEmpty = false
                }
            }

        }
    }

}