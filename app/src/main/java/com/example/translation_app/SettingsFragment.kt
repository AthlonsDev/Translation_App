package com.example.translation_app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.stream.IntStream.range
import kotlin.coroutines.coroutineContext


val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

class SettingsFragment : PreferenceFragmentCompat() {


    private val speechLanguageInput = stringPreferencesKey("speech_language_1")
    private val speechLanguageOutput = stringPreferencesKey("speech_language_2")
    private val cameraLanguage = stringPreferencesKey("camera_language")

    lateinit var speechInput: String
    lateinit var speechOutput: String
    lateinit var cameraOutput: String


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val speechInSettings = findPreference<Preference>("speech_language_1")
        val speechOutSettings = findPreference<Preference>("speech_language_2")
        val cameraSettings = findPreference<Preference>("cam_language")


        readData(speechLanguageInput)
        readData(speechLanguageOutput)
        readData(cameraLanguage)

        speechInSettings?.title = "Input Language - (${speechInput})"
        speechOutSettings?.title = "Output Language - ($speechOutput)"
        cameraSettings?.title = "Camera Language - ($cameraOutput)"

        speechInSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangCode = TranslateLanguage.fromLanguageTag(newLocale.toLanguageTag())
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Input Language - ($newLangName)"
//            preference.summary = newLangName
//            Constants.SPEECH_LANGUAGE_1 = newLangCode
//            save to datastore
            main(newLangName, speechLanguageInput)
            true
        }

        speechOutSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangCode = TranslateLanguage.fromLanguageTag(newLocale.toLanguageTag())
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Input Language - ($newLangName)"
            main(newLangName, speechLanguageOutput)
            true
        }

        cameraSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Input Language - ($newLangName)"
            main(newLangName, cameraLanguage)
            true
        }



    }

    private fun main(data: String, key: Preferences.Key<String>) = runBlocking {
        launch {
            saveUserPreferences(data, key)
        }
    }

    fun readData(key: Preferences.Key<String>) = runBlocking {
        launch {
            readUserPreferences(key)
        }
    }

    suspend fun saveUserPreferences(value: String, key: Preferences.Key<String>) {
        with(CoroutineScope(coroutineContext)) {
            launch {
                context?.dataStore?.edit { preferences ->
                    preferences[key] = value
                }
            }
        }
    }

    suspend fun readUserPreferences(key: Preferences.Key<String>) {
        with(CoroutineScope(coroutineContext)) {
            val preferences = context?.dataStore?.data?.first()
            val value = preferences?.get(key)
            if (key == speechLanguageInput) {
                speechInput = value.toString()
            }
            if (key == speechLanguageOutput) {
                speechOutput = value.toString()
            }
            if (key == cameraLanguage) {
                cameraOutput = value.toString()
                Toast.makeText(context, cameraOutput, Toast.LENGTH_SHORT).show()
            }

        }
    }


}