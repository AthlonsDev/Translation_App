package com.example.translation_app.ui.home

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class HomeViewModel : ViewModel() {


    //Models
    val frenchModel = TranslateRemoteModel.Builder(TranslateLanguage.FRENCH).build()
    val germanModel = TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build()
    val spanishModel = TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build()
    val chineseModel = TranslateRemoteModel.Builder(TranslateLanguage.CHINESE).build()
    val hindiModel = TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build()
    val arabicModel = TranslateRemoteModel.Builder(TranslateLanguage.ARABIC).build()
    val russianModel = TranslateRemoteModel.Builder(TranslateLanguage.RUSSIAN).build()
    val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
    val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()
    val italianModel = TranslateRemoteModel.Builder(TranslateLanguage.ITALIAN).build()

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    var identifiedLanguage = ""

    val text: LiveData<String> = _text




    fun manageLanguageModels() {
        val modelManager = RemoteModelManager.getInstance()

// Get translation models stored on the device.
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                // ...
            }
            .addOnFailureListener {
                // Error.
            }

// Delete the German model if it's on the device.
//        modelManager.deleteDownloadedModel(germanModel)
//            .addOnSuccessListener {
//                // Model deleted.
//            }
//            .addOnFailureListener {
//                // Error.
//            }

// Download the French model.
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        modelManager.download(frenchModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "French Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

        // Download the German model.
        modelManager.download(germanModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "French Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the Italian model
        modelManager.download(italianModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "Ita Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the Japanese model
        modelManager.download(japaneseModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "Jap Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }
    }


}