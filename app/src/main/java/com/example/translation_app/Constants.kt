package com.example.translation_app

import android.Manifest

object Constants {

    const val  TAG = "Camera X"
    const val REQUEST_CODE_PERMISSIONS = 123
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SS"
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    const val REQUEST_CODE_SPEECH_INPUT = 100
    val LANGUAGES = arrayOf("English", "Español", "Français", "Deutsch", "Italiano", "中國人", "日本語", "Korean", "Russian", "Arabic", "Hindi", "Portuguese", "Dutch", "Turkish", "Greek", "Hebrew", "Swedish", "Danish", "Finnish", "Norwegian", "Polish", "Czech", "Slovak", "Romanian", "Hungarian", "Catalan", "Indonesian", "Malay", "Vietnamese", "Thai", "Filipino", "Swahili", "Afrikaans", "Albanian", "Amharic", "Armenian", "Azerbaijani", "Basque", "Belarusian", "Bengali", "Bosnian", "Bulgarian", "Cebuano", "Chichewa", "Corsican", "Croatian", "Dutch", "Esperanto", "Estonian", "Frisian", "Galician", "Georgian", "Gujarati", "Haitian Creole", "Hausa", "Hawaiian", "Hmong", "Icelandic", "Igbo", "Irish", "Javanese", "Kannada", "Kazakh", "Khmer", "Kurdish", "Kyrgyz", "Lao", "Latin", "Latvian", "Lithuanian", "Luxembourgish", "Macedonian", "Malagasy", "Malayalam", "Maltese", "Maori", "Marathi", "Mongolian", "Myanmar", "Nepali", "Pashto", "Persian", "Punjabi", "Samoan", "Scots Gaelic", "Serbian", "Sesotho", "Shona", "Sindhi", "Sinhala", "Slovak", "Slovenian", "Somali", "Sundanese", "Swahili", "Tajik", "Tamil", "Telugu", "Ukrainian", "Urdu", "Uzbek", "Welsh", "Xhosa", "Yiddish", "Yoruba", "Zulu")

}