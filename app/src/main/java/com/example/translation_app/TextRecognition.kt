package com.example.translation_app

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.GnssAntennaInfo.Listener
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.ImageProxy
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.Objects

class TextRecognition: Activity() {


    val text_from_image = ""
    var listener: Listener? = null


    fun initTextRec(image: InputImage, alphabet: String, param: (String) -> Unit) {
        var recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        when (alphabet) {
            "Latin" -> {
                recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
            "Chinese" -> {
                recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            }
            "Devanagari" -> {
                recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            }
            "Japanese" -> {
                recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            }
            "Korean" -> {
                recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            }
        }



        var output = "Waiting for text..."
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                //set param to visionText
                param(visionText.text)
            }
    }

    fun identifyLanguage(text: String, param: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        var identifiedLanguage = ""
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
//                    identifiedLanguage = languageCode
                   identifiedLanguage = languageCode
                   param(identifiedLanguage)
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
                for (i in line.elements) {
//                    val elementText = element.text
//                    val elementCornerPoints = element.cornerPoints
//                    val elementFrame = element.boundingBox


                }
            }

        }
        // [END mlkit_process_text_block]
    }

}

class Draw(context: Context?, var rect: Rect, var text: String) : View(context) {

    lateinit var paint: Paint
    lateinit var textPaint: Paint

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE

        textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        canvas.drawText(text, rect.centerX().toFloat(), rect.centerY().toFloat(), textPaint)
        canvas.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), paint)
    }
}