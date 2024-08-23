package com.example.translation_app.ui.home

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.translation_app.R
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class HomeViewModel : ViewModel() {


    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    var identifiedLanguage = ""

    private val _inputText = MutableLiveData<String>().apply {
        value = R.string.speech_input.toString()
    }

    private val _outText = MutableLiveData<String>().apply {
        value = R.string.speech_output.toString()
    }


    val inputText: LiveData<String> = _inputText
    val outText: LiveData<String> = _outText


}