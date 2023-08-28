package com.example.translation_app

import android.Manifest

object Constants {

    const val  TAG = "Camera X"
    const val REQUEST_CODE_PERMISSIONS = 123
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SS"
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    const val REQUEST_CODE_SPEECH_INPUT = 100

}