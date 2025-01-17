package com.example.translation_app.ui.home

//import android.app.Fragment
//import androidx.fragment.app.Fragment
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent.EXTRA_LANGUAGE
import android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL
import android.speech.RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE
import android.speech.RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE
import android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
import android.speech.RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.Constants
import com.example.translation_app.R
import com.example.translation_app.Translator
import com.example.translation_app.dataStore
import com.example.translation_app.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.Objects
import kotlin.coroutines.coroutineContext


// At the top level of your kotlin file:
//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings").getValue(this, SettingsFragment.SPEECH_LANGUAGE_1)
class HomeFragment : androidx.fragment.app.Fragment(), RecognitionListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!



    var speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    private var tts: TextToSpeech? = null
    lateinit var inputLanguage: Locale
    lateinit var targetLanguage: String
    private lateinit var transcript: String

    @SuppressLint("ClickableViewAccessibility")
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
        readData()
        for (i in 0..1) {
            when (i) {
                0 -> {
                    setFlag(inputLanguage.toString()) {
                        binding.speechFlagIn.text = it
                    }
                }
                1 -> {
                    setFlag(targetLanguage) {
                        binding.speechFlagOut.text = it
                    }
                }
            }
        }
        // Set up the text to speech
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts!!.language = inputLanguage
            }
        }

        transcript = ""
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        binding.micButton.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    RecSpeech()
                }

                MotionEvent.ACTION_UP -> {
                    speechRecognizer.stopListening()
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        if(ContextCompat.checkSelfPermission(requireContext(), Constants.REQUIRED_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        var flag = true
        binding.flipButton.setOnClickListener {
            if (flag) {
                flag = false
                val temp = inputLanguage
                inputLanguage = Locale(targetLanguage)
                targetLanguage = temp.toString()
                setFlag(inputLanguage.toString()) {
                    binding.speechFlagIn.text = it
                }
                setFlag(targetLanguage) {
                    binding.speechFlagOut.text = it
                }
            } else {
                val temp = inputLanguage
                inputLanguage = Locale(targetLanguage)
                targetLanguage = temp.toString()
                setFlag(inputLanguage.toString()) {
                    binding.speechFlagIn.text = it
                }
                setFlag(targetLanguage) {
                    binding.speechFlagOut.text = it
                }
            }
        }

        binding.textButton.setOnClickListener {
            if (binding.transcriptView.visibility == View.INVISIBLE) {
                binding.transcriptView.visibility = View.VISIBLE
            } else {
                binding.transcriptView.visibility = View.INVISIBLE
            }
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)

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
            inputLanguage = Locale(speechLanguageInput)
            val input = Locale("Japanese")
            val getIso = input.displayVariant
//            Toast.makeText(requireContext(), getIso, Toast.LENGTH_SHORT).show()
            targetLanguage = speechLanguageOutput.toString()
//            val locale = Locale.getDefault()
//            val langName = locale.getDisplayName(locale)
//            targetLanguage = langName
            val translator = Translator()
            translator.identifyLanguage(targetLanguage) { result ->
                targetLanguage = result
            }
//            binding.textView.text = "${getString(R.string.speech_input)} - ${speechLanguageInput.toString()}"
//            binding.textView2.text = "${getString(R.string.speech_output)} - ${speechLanguageOutput.toString()}"
        }
    }

    private fun setFlag(lang: String, param: (String) -> Unit) {
        val translator = Translator()
        translator.setFlag(lang) {
            param(it)
        }
    }

    fun RecSpeech() {

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        speechRecognizer.setRecognitionListener(this)
        speechRecognizer.startListening(speechRecognizerIntent)

        if (inputLanguage.toString() == "Not Set" || inputLanguage.toString() == "null") {
            Toast.makeText(requireContext(), "Please set input language", Toast.LENGTH_SHORT).show()
            return
        }

//        inputLanguage of speech needs to be translated to english
        val translator = Translator()
        translator.identifyLanguage(inputLanguage.toString()) { result ->

            speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntent.putExtra(
                EXTRA_LANGUAGE_MODEL,
                LANGUAGE_MODEL_FREE_FORM
            )
            val cap = result.uppercase()
            val it = "$result-$cap"
//            val newLocale = Locale("italian", "IT")
//            val it = "it-IT"

            speechRecognizerIntent.putExtra(EXTRA_LANGUAGE, it)
            speechRecognizerIntent.putExtra(EXTRA_LANGUAGE_PREFERENCE, it)
            speechRecognizerIntent.putExtra(EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Translate")

//            onBeginningOfSpeech()
            //get result of speech
            try {
//                setRecognitionListener(this)
//                startActivityForResult(speechRecognizerIntent, Constants.REQUEST_CODE_SPEECH_INPUT)
//                textToSpeechEngine.speak("Speak to Translate", TextToSpeech.QUEUE_FLUSH, null, null)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast.makeText(requireContext(), " " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val textToSpeechEngine: TextToSpeech by lazy {
        // Pass in context and the listener.
        TextToSpeech(requireContext(),
            TextToSpeech.OnInitListener { status ->
                // set our locale only if init was success.
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechEngine.language = Locale.UK
                }
            })
    }

    private fun translateText(text: String, targetLanguage: String, inputLanguage: Locale) {
        val translator = Translator()
        translator.identifyLanguage(transcript) { result ->
            translator.initTranslator(transcript, result, targetLanguage) {
                textToSpeechEngine.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
                binding.transcriptView.text = it
            }
        }
    }

    private fun setRecognitionListener (listener: RecognitionListener) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        recognizer.setRecognitionListener(listener)
        recognizer.startListening(speechRecognizerIntent)


    }



    override fun onReadyForSpeech(p0: Bundle?) {
        val data = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        transcript = data?.get(0).toString()
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onRmsChanged(p0: Float) {

    }

    override fun onBufferReceived(p0: ByteArray?) {

    }

    override fun onEndOfSpeech() {
    }

    override fun onError(p0: Int) {
//        binding.inputText.setText("Error $p0")
    }

    override fun onResults(p0: Bundle?) {
        val data = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        transcript = data?.get(0).toString()
        Toast.makeText(requireContext(), transcript, Toast.LENGTH_SHORT).show()
        translateText(transcript, targetLanguage, inputLanguage)
    }

    override fun onPartialResults(p0: Bundle?) {
        val data = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        transcript = data?.get(0).toString()
    }

    override fun onLanguageDetection(results: Bundle) {
        super.onLanguageDetection(results)
        val lang = results.getString("language")
        Toast.makeText(requireContext(), lang, Toast.LENGTH_SHORT).show()
    }

    override fun onEvent(p0: Int, p1: Bundle?) {

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
                transcript = (
                    Objects.requireNonNull(res)[0]

                )
            }
        }
    }
}

