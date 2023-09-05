package com.example.translation_app

import android.content.Intent
import android.os.Bundle
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        Check data saved in dataStore
//        readData()
//        if the data is empty run the following code

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
            readUserPreferences()
        }
    }

    private suspend fun readUserPreferences() {
        with(CoroutineScope(coroutineContext)) {
            val dataoutputKey = stringPreferencesKey("text_language_1")
            val preferences = dataStore.data.first()
            val textLanguageOutput = preferences?.get(dataoutputKey)
            if (textLanguageOutput != null) {
                isDataEmpty = false
            }
        }
    }

}