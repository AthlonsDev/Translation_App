package com.example.translation_app.ui.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        val translator = Translator()
        binding.buttonTranslate.setOnClickListener {
            translator.identifyLanguage(binding.inputText.text.toString()) { result ->
                translator.initTranslator(binding.inputText.text.toString(), result, targetLanguage) { result ->
                    binding.outputText.text = result
                }
            }

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
            binding.textView6.text = "Translating to - ${textLanguageOutput.toString()}"
        }
    }


}