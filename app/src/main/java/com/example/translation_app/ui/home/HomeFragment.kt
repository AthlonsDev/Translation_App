package com.example.translation_app.ui.home

import android.app.Activity.RESULT_OK
import android.content.Context
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.CameraActivity
import com.example.translation_app.Constants
import com.example.translation_app.EntityExtraction
import com.example.translation_app.GalleryActivity
import com.example.translation_app.R
import com.example.translation_app.SettingsFragment
import com.example.translation_app.Translator
import com.example.translation_app.dataStore
import com.example.translation_app.databinding.FragmentHomeBinding
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.Objects
import java.util.concurrent.Flow
import java.util.prefs.Preferences
import kotlin.coroutines.coroutineContext


// At the top level of your kotlin file:
//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings").getValue(this, SettingsFragment.SPEECH_LANGUAGE_1)
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    var speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    private var tts: TextToSpeech? = null
    lateinit var inputLanguage: Locale
    lateinit var targetLanguage: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val translator = Translator()


        binding.inputText.movementMethod = ScrollingMovementMethod()
        binding.outputText.movementMethod = ScrollingMovementMethod()

        readData()


        homeViewModel.manageLanguageModels()

        // Set up the text to speech
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = inputLanguage
            }
        }

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        val button: TextView = binding.translateButton
        button.setOnClickListener {
            val inputString = binding.inputText.text.toString()
            translator.identifyLanguage(inputString) { result ->
                inputLanguage = Locale(result)
                translator.initTranslator(inputString, inputLanguage.toString(), targetLanguage) { result ->
                        binding.outputText.text = result
                        tts!!.speak(result, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }
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

    fun readData() = runBlocking {
        launch {
            readUserPreferences()
        }
    }

    suspend fun readUserPreferences() {
        with(CoroutineScope(coroutineContext)) {
            val datainputeKey = stringPreferencesKey("speech_language_1")
            val dataoutputKey = stringPreferencesKey("speech_language_2")
            val preferences = context?.dataStore?.data?.first()
            val speechLanguageInput = preferences?.get(datainputeKey)
            val speechLanguageOutput = preferences?.get(dataoutputKey)
            inputLanguage = Locale(speechLanguageInput.toString())
            targetLanguage = speechLanguageOutput.toString()
            val translator = Translator()
            translator.identifyLanguage(targetLanguage) { result ->
                targetLanguage = result
            }
            binding.textView.text = "Translating from - ${speechLanguageInput.toString()}"
            binding.textView2.text = "Translating to - ${speechLanguageOutput.toString()}"
        }
    }

    fun RecSpeech() {

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, inputLanguage)
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

