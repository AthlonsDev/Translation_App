package com.example.translation_app

import android.content.Context
import android.content.Intent
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
import androidx.preference.SwitchPreference
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
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

        val speechInSettings = findPreference<Preference>(getString(R.string.speech_language_1))
        val speechOutSettings = findPreference<Preference>(getString(R.string.speech_language_2))
        val alphabetSettings = findPreference<Preference>(getString(R.string.alphabet_language_1))
        val cameraSettings = findPreference<Preference>(getString(R.string.cam_language))
        val textSettings = findPreference<Preference>(getString(R.string.text_language))



//        set ouput language to locale as default
        val locale = Locale.getDefault()
        val langName = locale.getDisplayName(locale)
        speechOutput = langName
        cameraOutput = langName
        textOutput = langName
        alphabet = "Latin"



        readData(speechLanguageInput)
        readData(speechLanguageOutput)
        readData(alphabetInput)
        readData(cameraLanguage)
        readData(textLanguage)

//        checkModels()

//        if(speechInput != "Not Set" || speechOutput != "Not Set" || cameraOutput != "Not Set" || textOutput != "Not Set" || alphabet != "Not Set") {
//            saveButton?.isVisible = false
//        }

        var speechInputText = getString(R.string.speech_language_1)
        var speechOutputText = getString(R.string.speech_language_2)
        var alphabetText = getString(R.string.alphabet_language_1)
        var cameraText = getText(R.string.cam_language)
        var textText = getText(R.string.text_language)


        Toast.makeText(context, "$speechInputText", Toast.LENGTH_LONG).show()
        speechInSettings?.title = "$speechInputText - $speechInput"
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

    }

    fun saveUserPref(
        languageInput1: String,
        languageInput2: String,
        languageInput3: String,
        languageInput4: String,
        languageInput5: String,
    ): Boolean {
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

//        checkData()
        return true
    }

    private fun checkData(): Boolean {
        if (speechInput != "Not Set" || speechOutput != "Not Set" || cameraOutput != "Not Set" || textOutput != "Not Set" || alphabet != "Not Set") {
            return false
        }
        readData(speechLanguageInput)
        readData(speechLanguageOutput)
        readData(alphabetInput)
        readData(cameraLanguage)
        readData(textLanguage)
        return true
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
                val context = context
                context?.dataStore?.edit { preferences ->
                    preferences[key] = value
                }
            }
        }
    }

    suspend fun readUserPreferences(key: Preferences.Key<String>) {
        with(CoroutineScope(coroutineContext)) {
            val preferences = context?.dataStore?.data?.first()
            val locale = Locale.getDefault()
            val langName = locale.getDisplayName(locale)
            val value = preferences?.get(key)
            if (key == speechLanguageInput) {
                speechInput = value.toString()
                if(speechInput == null) {
                    speechInput = "Not Set"
                }
            }
            if (key == speechLanguageOutput) {
                speechOutput = value.toString()
                if (speechOutput == null) {
                    speechOutput = langName
                }
            }
            if (key == alphabetInput) {
                alphabet = value.toString()
                if (alphabet == null) {
                    alphabet = "latin"
                }
            }
            if (key == cameraLanguage) {
                cameraOutput = value.toString()
                if (cameraOutput == null) {
                    cameraOutput = langName
                }
            }
            if (key == textLanguage) {
                textOutput = value.toString()
                if (textOutput == null) {
                    textOutput = langName
                }
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