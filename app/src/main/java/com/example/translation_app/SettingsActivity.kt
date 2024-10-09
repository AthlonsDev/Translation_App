package com.example.translation_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.translation_app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.coroutineContext

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")
class SettingsActivity: AppCompatActivity() {

    var isDataEmpty = true

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkData()

    }

    fun saveUserPref(
        languageInput1: String,
        languageInput2: String,
        languageInput3: String,
        languageInput4: String,
        languageInput5: String,
    ) {
        val dataOutputKey1 = stringPreferencesKey("speech_language_1")
        val dataOutputKey2 = stringPreferencesKey("speech_language_2")
        val dataOutputKey3 = stringPreferencesKey("camera_language")
        val dataOutputKey4 = stringPreferencesKey("text_language")
        val dataOutputKey5 = stringPreferencesKey("alphabet_language_1")

        runBlocking {
            saveUserPreferences(languageInput1, dataOutputKey1)
            saveUserPreferences(languageInput2, dataOutputKey2)
            saveUserPreferences(languageInput3, dataOutputKey3)
            saveUserPreferences(languageInput4, dataOutputKey4)
            saveUserPreferences(languageInput5, dataOutputKey5)
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private suspend fun saveUserPreferences(value: String, key: Preferences.Key<String>) {
        with(CoroutineScope(coroutineContext)) {
            launch {
                this@SettingsActivity.dataStore?.edit { preferences ->
                    preferences[key] = value
                }
            }
        }
    }

    fun checkInitData(): Boolean {
        readData()
        return isDataEmpty
    }

    fun checkData() {
        readData()
        if (isDataEmpty) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
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