package com.example.translation_app.ui.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.R
import com.example.translation_app.Translator
import com.example.translation_app.dataStore
import com.example.translation_app.databinding.FragmentTextBinding
import com.example.translation_app.ui.home.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import kotlin.coroutines.coroutineContext

class TextFragment: Fragment() {

    private var _binding: FragmentTextBinding? = null
    private lateinit var targetLanguage: String
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val textViewModel =
            ViewModelProvider(this).get(TextFragmentViewModel::class.java)

        _binding = FragmentTextBinding.inflate(inflater, container, false)
        val root: View = binding.root


        readData()

//        textViewModel.outputText.observe(viewLifecycleOwner) {
//            binding.outputText.text = it
//        }


        val translator = Translator()
        binding.buttonTranslate.setOnClickListener {
//            translator.identifyLanguage(binding.inputEditText.text.toString()) { result ->
//                translator.initTranslator(binding.inputEditText.text.toString(), result, targetLanguage) { result ->
//                    binding.outputText.text = result
//                }
//            }

            binding.outputText.text = "Translating..."

        }

        return root
    }

    fun readData() = runBlocking {
        launch {
            readUserPreferences()
        }
    }

    suspend fun readUserPreferences() {
        with(CoroutineScope(coroutineContext)) {
            val dataoutputKey = stringPreferencesKey("text_language")
            val preferences = context?.dataStore?.data?.first()
            val textLanguageOutput = preferences?.get(dataoutputKey)
            targetLanguage = textLanguageOutput.toString()
            val translator = Translator()
            translator.identifyLanguage(targetLanguage) { result ->
                targetLanguage = result
            }
            val textLang = getString(R.string.camera_label)
            binding.textLabel.text = "$textLang - ${textLanguageOutput.toString()}"
        }
    }


}


