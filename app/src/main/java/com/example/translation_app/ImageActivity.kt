package com.example.translation_app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.translation_app.databinding.ActivityImageBinding
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageActivity: AppCompatActivity() {

    var bmp: Bitmap? = null
    val bitmap = bmp?.let { Bitmap.createScaledBitmap(it, 224, 224, true) }
    val input = ByteBuffer.allocateDirect(224*224*3*4).order(ByteOrder.nativeOrder())
    var identifiedLanguage = ""
    private lateinit var binding: ActivityImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filename = intent.getStringExtra("image")
        try {
            val `is` = openFileInput(filename)
            bmp = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.imageView2.setImageBitmap(bmp)

        initTextRec(bmp!!, binding)


    }

    fun initTextRec(bitmap: Bitmap, binding: ActivityImageBinding) {
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

        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // ...
                binding.textView3.setText(visionText.text)
                identifyLanguage(binding.textView3.text.toString())
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
                Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show()
            }

    }

    fun identifyLanguage(text: String) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Toast.makeText(this, "Can't identify language.", Toast.LENGTH_SHORT).show()
                } else {
                    // The languageCode
                    identifiedLanguage = languageCode
                    initTranslator(text, binding)
                }
            }
            .addOnFailureListener {
                // Model couldn’t be loaded or other internal error.
                // ...
            }
    }

    private fun initTranslator(input: String, binding: ActivityImageBinding) {

        // Create an English-German translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(identifiedLanguage).toString())
            .setTargetLanguage(TranslateLanguage.FRENCH)
            .build()
        val englishGermanTranslator = Translation.getClient(options)

        englishGermanTranslator.translate(input)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
                binding.outputText.setText(translatedText)
            }
            .addOnFailureListener { exception ->
                // Error.
                // ...
                binding.outputText.setText(exception.toString())
            }
    }
}