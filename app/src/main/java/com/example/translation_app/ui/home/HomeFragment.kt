package com.example.translation_app.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.CameraActivity
import com.example.translation_app.Constants
import com.example.translation_app.EntityExtraction
import com.example.translation_app.GalleryActivity
import com.example.translation_app.R
import com.example.translation_app.databinding.FragmentHomeBinding
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import java.util.Objects


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    var speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    private var tts: TextToSpeech? = null
    var targetLanguage = Locale.ENGLISH

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.inputText.movementMethod = ScrollingMovementMethod()
        binding.outputText.movementMethod = ScrollingMovementMethod()

        // get reference to the string array that we just created
        val languages = resources.getStringArray(R.array.Languages)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, languages)
        // get reference to the autocomplete text view
        val autocompleteTV = binding.dropdownMenu
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter)

//        convert the selected item from autocompleteTextView to a locale
        autocompleteTV.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            targetLanguage = when (selectedItem) {
                "English" -> Locale.ENGLISH
                "Spanish" -> Locale("es")
                "French" -> Locale("fr")
                "German" -> Locale("de")
                "Italian" -> Locale("it")
                "Japanese" -> Locale("ja")
                "Korean" -> Locale("ko")
                "Portuguese" -> Locale("pt")
                "Russian" -> Locale("ru")
                "Chinese" -> Locale("zh")
                else -> Locale.ENGLISH
            }
            Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_LONG).show()
        }

        homeViewModel.manageLanguageModels()

        // Set up the text to speech
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = targetLanguage
            }
        }

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        val button: TextView = binding.translateButton
        button.setOnClickListener {
            val inputString = binding.inputText.text.toString()
            identifyLanguage(inputString, homeViewModel)
        }
//
        homeViewModel.manageLanguageModels()

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

        binding.inputText.text = "Waiting for input..."
        binding.outputText.text = "Waiting for Translation..."

        if(ContextCompat.checkSelfPermission(requireContext(), Constants.REQUIRED_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        return root
    }



    fun RecSpeech() {

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT")
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Translate")

        onBeginningOfSpeech()
        //get result of speech
        try {
            startActivityForResult(speechRecognizerIntent, Constants.REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            // on below line we are displaying error message in toast
            Toast.makeText(requireContext(), " " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onBeginningOfSpeech() {
        binding.inputText.setText("Listening...")
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

        val tl = targetLanguage.toString()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(homeViewModel.identifiedLanguage).toString())
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val englishGermanTranslator = Translation.getClient(options)

        englishGermanTranslator.translate(input)
            .addOnSuccessListener { translatedText ->
                // Translation successful.
                binding.outputText.setText(translatedText)

                // Speak the translated text
                tts!!.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, "")
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

    // on below line we are calling on activity result method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == Constants.REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                // on below line we are setting data
                // to our output text view.
                binding.inputText.setText(
                    Objects.requireNonNull(res)[0]

                )
            }
        }
    }
}

