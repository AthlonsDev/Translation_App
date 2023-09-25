package com.example.translation_app.ui.text

import android.app.Application
import android.provider.Settings.Global.getString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.translation_app.R

class TextFragmentViewModel: ViewModel() {

//    private val context = Application().applicationContext


    private val _outputText = MutableLiveData<String>().apply {
        value = "insert text here"
    }
    //
    val outputText: LiveData<String> = _outputText

}