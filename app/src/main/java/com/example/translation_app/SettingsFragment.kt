package com.example.translation_app

import android.content.Context
import android.content.res.Configuration
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
    private val alphabetInput = stringPreferencesKey("alphabet_language_1")
    private val cameraLanguage = stringPreferencesKey("camera_language")
    private val textLanguage = stringPreferencesKey("text_language")

    private var speechInput: String = "Not Set"
    private var speechOutput: String = "Not Set"
    private var alphabet: String = "Not Set"
    private var cameraOutput: String = "Not Set"
    private var textOutput: String = "Not Set"


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        updateLanguage()

        val speechInSettings = findPreference<Preference>("speech_language_1")
        val speechOutSettings = findPreference<Preference>("speech_language_2")
        val alphabetSettings = findPreference<Preference>("alphabet_language_1")
        val cameraSettings = findPreference<Preference>("cam_language")
        val textSettings = findPreference<Preference>("text_language")
        val saveButton = findPreference<Preference>("button")

        readData(speechLanguageInput)
        readData(speechLanguageOutput)
        readData(alphabetInput)
        readData(cameraLanguage)
        readData(textLanguage)

        var speechInputText = "Voice Language"
        var speechOutputText = "Target Language"
        var alphabetText = "Alphabet Type"
        var cameraText = "Camera Language"
        var textText = "Text Language"

        val tr = Translator()
        val locale = Locale.ITALIAN.toString()
        tr.initTranslator(speechInputText, "en", locale) {
            speechInSettings?.title = "$it - $speechInput"
        }
        tr.initTranslator(speechOutputText, "en", locale) {
            speechOutSettings?.title = "$it - $speechOutput"
        }
        tr.initTranslator(cameraText, "en", locale) {
            cameraSettings?.title = "$it - $cameraOutput"
        }
        tr.initTranslator(alphabetText, "en", locale) {
            alphabetSettings?.title = "$it - $alphabet"
        }
        tr.initTranslator(textText, "en", locale) {
            textSettings?.title = "$it - $textOutput"
        }

        speechOutSettings?.title = "$speechOutputText - $speechOutput"
        alphabetSettings?.title = "$alphabetText - $alphabet"
        cameraSettings?.title = "$cameraText - $cameraOutput"
        textSettings?.title = "$textText - $textOutput"

        speechInSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Source Language - $newLangName"
            main(newLangName, speechLanguageInput)
            true
        }

        speechOutSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Target Language - $newLangName"
            main(newLangName, speechLanguageOutput)
            true
        }

        alphabetSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Alphabet Language - $newLangName"
            main(newLangName, alphabetInput)
            true
        }

        cameraSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangName = newLocale.getDisplayName(newLocale)
            preference.title = "Target Language - $newLangName"
            main(newLangName, cameraLanguage)
            true
        }

        textSettings?.setOnPreferenceChangeListener { preference, newValue ->
            val newLang = newValue as String
            val newLocale = Locale(newLang)
            val newLangName = newLocale.getDisplayName(newLocale)

            preference.title = "Target Language - $newLangName"
            main(newLangName, textLanguage)
            true
        }



        if(speechInput != null && speechOutput != null && cameraOutput != null && textOutput != null && alphabet != null) {
            saveButton?.isVisible = false
        }


        saveButton?.setOnPreferenceClickListener {
            val setAct = SettingsActivity()
            setAct.checkData()
            true
        }
    }

    private fun main(data: String, key: Preferences.Key<String>) = runBlocking {
        launch {
            saveUserPreferences(data, key)
        }
    }

    private fun readData(key: Preferences.Key<String>) = runBlocking {
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
            if (key == alphabetInput) {
                alphabet = value.toString()
            }
            if (key == cameraLanguage) {
                cameraOutput = value.toString()
            }
            if (key == textLanguage) {
                textOutput = value.toString()
            }

        }
    }


    fun updateLanguage() {
        val languageToLoad = "it" // your language fr etc

        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )
    }

}