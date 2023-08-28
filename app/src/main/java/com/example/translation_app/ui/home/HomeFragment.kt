package com.example.translation_app.ui.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.CameraActivity
import com.example.translation_app.Constants
import com.example.translation_app.GalleryActivity
import com.example.translation_app.databinding.FragmentHomeBinding
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale


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

        homeViewModel.manageLanguageModels()

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

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        binding.micButton.setOnTouchListener(OnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    RecSpeech()
                    binding.micButton.setText("Listening...")
                }
                MotionEvent.ACTION_UP -> {
                    speechRecognizer.stopListening()
                    binding.micButton.setText("Tap to Speak")
                }
            }
            v?.onTouchEvent(event) ?: true
        })

        if(ContextCompat.checkSelfPermission(requireContext(), Constants.REQUIRED_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        return root
    }

    fun RecSpeech() {

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        onBeginningOfSpeech()
        //get result of speech
//        onResults(speechRecognizerIntent.extras!!)
    }

    fun onBeginningOfSpeech() {
        binding.inputText.setText("Listening...")
    }


    fun onResults(bundle: Bundle) {
//        val micBtn = micButton.setImageResource(R.drawable.ic_mic_black_off)
        val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        binding.inputText.setText(data!![0])
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Constants.REQUIRED_PERMISSIONS[1]),
                Constants.REQUEST_CODE_SPEECH_INPUT
            )
        }
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