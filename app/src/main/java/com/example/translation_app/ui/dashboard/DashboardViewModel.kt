package com.example.translation_app.ui.dashboard

import android.app.Activity
import android.provider.Settings.Global.getString
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.translation_app.R
import androidx.core.app.ActivityCompat
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService

class DashboardViewModel : ViewModel() {



    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _inputText = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
//
    val inputText: LiveData<String> = _inputText

//    val contentResolver = requireActivity().contentResolver

//    private val _outputText = MutableLiveData<String>().apply {
//        value = getString(contentResolver, R.string.speech_output.toString())
//    }
//
//    val outputText: LiveData<String> = _outputText

}