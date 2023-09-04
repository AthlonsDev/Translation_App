package com.example.translation_app

import android.app.Activity
import android.graphics.Bitmap
import android.location.GnssAntennaInfo.Listener
import android.util.Log
import android.widget.Toast
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.Objects

class TextRecognition {


    val text_from_image = ""
    var listener: Listener? = null


    fun initTextRec(bitmap: Bitmap, param: (String) -> Unit) {
        // When using Latin script library
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

//// When using Chinese script library
//        val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
//
//// When using Devanagari script library
//        val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
//
//// When using Japanese script library
//        val recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
//
//// When using Korean script library
//        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())


        val image = InputImage.fromBitmap(bitmap, 0)
        var output = "Waiting for text..."
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // ...
                //set param to visionText
                param(visionText.text)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
//                Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show()
                param("Failed to recognize text ${e.message}")
            }

    }

    fun identifyLanguage(text: String, param: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        var identifiedLanguage = ""
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
//                    Toast.makeText(this, "Can't identify language.", Toast.LENGTH_SHORT).show()
                    identifiedLanguage = "Can't identify language."
                } else {
                    // The languageCode
//                    identifiedLanguage = languageCode
                    identifiedLanguage = languageCode
                   param(identifiedLanguage)
                }
            }
            .addOnFailureListener {
                // Model couldn’t0 be loaded or other internal error.
                // ...
                param("error: ${it.message}")
            }

    }

    fun initTranslator(input: String, identifiedLanguage: String, targetLanguage: String, param: (String) -> Unit) {

        // get current language code
        targetLanguage.uppercase()
        val newLocale = Locale(targetLanguage)
        val targetLanguageCode = TranslateLanguage.fromLanguageTag(newLocale.toLanguageTag())

        Log.d("LanguageCode", targetLanguageCode.toString())
        Log.d("LanguageCode", identifiedLanguage.toString())
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(identifiedLanguage).toString())
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguageCode.toString()).toString())
            .build()
        val translator = Translation.getClient(options)
        var output = ""
        translator.translate(input)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
//                binding.outputText.setText(translatedText)
                param(translatedText)
            }
            .addOnFailureListener { exception ->
                // Error.
                // ...
                param("Failed to translate text ${exception.message}")
            }
    }

    fun processTextBlock(result: Text) {
        // [START mlkit_process_text_block]
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                }
            }

        }
        // [END mlkit_process_text_block]
    }

}