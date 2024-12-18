package com.example.translation_app

import android.speech.tts.TextToSpeech
import com.example.translation_app.ui.home.HomeViewModel
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Objects

class Translator {

    fun identifyLanguage(text: String, param: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {

                } else {
                    // The languageCode
                    param(languageCode)
                }
            }
            .addOnFailureListener {
                // Model couldn’t be loaded or other internal error.
                // ...
                param("Failed to identify language ${it.message}")
            }
    }

    fun initTranslator(input: String, sl: String, tl: String, param: (String) -> Unit) {


        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(sl).toString())
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(tl).toString())
            .build()
        val translator = Translation.getClient(options)

        translator.translate(input)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
                param(translatedText)
            }
            .addOnFailureListener { exception ->
                // Error.
                // ...
                param("Failed to translate text, check that the model for the language is downloaded and try again.")

            }
    }
}