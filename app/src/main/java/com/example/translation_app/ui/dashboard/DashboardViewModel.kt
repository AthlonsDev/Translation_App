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



    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String> get() = _translatedText

    fun setTranslatedText(text: String) {
        _translatedText.value = text
    }

}