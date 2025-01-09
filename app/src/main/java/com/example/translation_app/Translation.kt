package com.example.translation_app

import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.example.translation_app.ui.home.HomeViewModel
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import java.util.Objects

class Translator {

    fun identifyLanguage(text: String, param: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {

                } else {
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

    fun setFlag(lang: String, param: (String) -> Unit) {
        if(lang == "日本語 - \uD83C\uDDEF\uD83C\uDDF5") {
            val fix = "jp".uppercase()
            val firstLetter = Character.codePointAt(fix, 0) - 0x41
            val secondLetter = Character.codePointAt(fix, 1) - 0x41
            param(
                String(Character.toChars(firstLetter + 0x1F1E6)) + String(
                    Character.toChars(
                        secondLetter + 0x1F1E6
                    )
                )
            )
        }
        identifyLanguage(lang) {
            var fix = ""
            when(it) {
                "en" -> {
                    fix = "gb".uppercase()
                    val firstLetter = Character.codePointAt(fix, 0) - 0x41 + 0x1F1E6
                    val secondLetter = Character.codePointAt(fix, 1) - 0x41 + 0x1F1E6
                    param(
                        String(Character.toChars(firstLetter)) + String(
                            Character.toChars(
                                secondLetter
                            )
                        )
                    )
                }

                "ja" -> {
                    fix = "jp".uppercase()
                    val firstLetter = Character.codePointAt(fix, 0) - 0x41
                    val secondLetter = Character.codePointAt(fix, 1) - 0x41
                    param(
                        String(Character.toChars(firstLetter + 0x1F1E6)) + String(
                            Character.toChars(
                                secondLetter + 0x1F1E6
                            )
                        )
                    )
                }

                "ko" -> {
                    fix = "kr".uppercase()
                    val firstLetter = Character.codePointAt(fix, 0) - 0x41
                    val secondLetter = Character.codePointAt(fix, 1) - 0x41
                    param(
                        String(Character.toChars(firstLetter + 0x1F1E6)) + String(
                            Character.toChars(
                                secondLetter + 0x1F1E6
                            )
                        )
                    )
                }

                "zh" -> {
                    fix = "cn".uppercase()
                    val firstLetter = Character.codePointAt(fix, 0) - 0x41
                    val secondLetter = Character.codePointAt(fix, 1) - 0x41
                    val result = String(Character.toChars(firstLetter + 0x1F1E6)) + String(
                        Character.toChars(
                            secondLetter + 0x1F1E6
                        )
                    )
                    param(
                        result
                    )
                }

                "ar" -> {
                    fix = "sa".uppercase()
                    val firstLetter = Character.codePointAt(fix, 0) - 0x41
                    val secondLetter = Character.codePointAt(fix, 1) - 0x41
                    param(
                        String(Character.toChars(firstLetter + 0x1F1E6)) + String(
                            Character.toChars(
                                secondLetter + 0x1F1E6
                            )
                        )
                    )
                }

                else -> {
                    val firstLetter = Character.codePointAt(it.uppercase(), 0) - 0x41 + 0x1F1E6
                    val secondLetter = Character.codePointAt(it.uppercase(), 1) - 0x41 + 0x1F1E6
                    param(
                        String(Character.toChars(firstLetter)) + String(
                            Character.toChars(
                                secondLetter
                            )
                        )
                    )
                }

            }
        }
    }
}