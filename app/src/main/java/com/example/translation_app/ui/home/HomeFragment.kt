package com.example.translation_app.ui.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.CameraActivity
import com.example.translation_app.GalleryActivity
import com.example.translation_app.databinding.FragmentHomeBinding
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val button: TextView = binding.translateButton
        button.setOnClickListener {
            val inputString = binding.inputText.text.toString()
            identifyLanguage(inputString, homeViewModel)
        }
//
        homeViewModel.manageLanguageModels()

        binding.cameraButton.setOnClickListener {
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }

        binding.galleryButton.setOnClickListener {
            val intent = Intent(activity, GalleryActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    fun identifyLanguage(text: String, homeViewModel: HomeViewModel) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {

                } else {
                    // The languageCode
                    homeViewModel.identifiedLanguage = languageCode
                    initTranslator(text, homeViewModel)
                }
            }
            .addOnFailureListener {
                // Model couldn’t be loaded or other internal error.
                // ...
            }
    }


    fun initTranslator(input: String, homeViewModel: HomeViewModel) {

        // Create an English-German translator:
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(homeViewModel.identifiedLanguage).toString())
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val englishGermanTranslator = Translation.getClient(options)

        englishGermanTranslator.translate(input)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
                binding.outputText.setText(translatedText)
            }
            .addOnFailureListener { exception ->
                // Error.
                // ...
                binding.outputText.setText(exception.toString())
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}