package com.example.translation_app

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.location.GnssAntennaInfo.Listener
import android.util.AttributeSet
import android.util.Log
import android.view.View
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

class TextRecognition: Activity() {


    val text_from_image = ""
    var listener: Listener? = null
    lateinit var bb: BoundingBox


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

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(identifiedLanguage).toString())
            .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLanguage)!!)
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



    fun recognizeText(image: InputImage, param: (Text?) -> Unit) {

        // [START get_detector_default]
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        // [END get_detector_default]
        var _text: Text? = null

        // [START run_detector]
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                // [START_EXCLUDE]
                // [START get_text]
                for (block in visionText.textBlocks) {
                    val boundingBox = block.boundingBox
                    val cornerPoints = block.cornerPoints
                    val text = block.text

                    _text = visionText
                    for (line in block.lines) {
                        for (element in line.elements) {
                            // ...
                        }
                    }
                }
                param(_text)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }
    }

    private fun processText(text: Text): String {
        return text.toString()
    }


    private fun drawRectangle(rect: Rect) {

        bb = BoundingBox(this, null, rect)
//    binding.preview.addView(customView)
    }

    private fun clearCanvas() {
        bb.clearCanvas()
    }

}

class BoundingBox(context: Context?, attrs: AttributeSet?, rect: Rect) :
    View(context, attrs)
    {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        private val boundingBox = rect
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(boundingBox, paint)

        }
        fun clearCanvas() {
            val transparent = Paint()
            transparent.alpha = 0
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
        canvas.drawText(text, rect.centerX().toFloat(), rect.centerY().toFloat(), textPaint)
        canvas.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), paint)
    }
}