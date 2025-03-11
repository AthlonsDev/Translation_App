package com.example.translation_app.ui.home

import com.google.mlkit.nl.translate.Translator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HomeFragmentTest {
    lateinit var transcript: String
    lateinit var inputLanguage: String
    lateinit var outputLanguage: String

    @Before
    fun setUp() {
        transcript = "Hello, World!"
        inputLanguage = "English"
        outputLanguage = "Spanish"
    }

    @Test
    fun testTranslate() {
        val translator = com.example.translation_app.Translator()

        translator.identifyLanguage(transcript) {
            assertEquals("en", it)
        }

        translator.initTranslator(transcript, inputLanguage, outputLanguage) {
            assertEquals("Hola, Mundo!", it)
        }
    }
}