package com.example.translation_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import com.example.translation_app.Models.ModelActivity
import com.example.translation_app.databinding.ActivityGalleryBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileDescriptor
import java.io.IOException
import java.util.Arrays
import java.util.Locale
import kotlin.coroutines.coroutineContext

class GalleryActivity: AppCompatActivity() {

        private val pickImage = 100
        private var imageUri: Uri? = null
        private lateinit var binding: ActivityGalleryBinding
        lateinit var alphabet: String
        var targetLanguage: String = ""
        lateinit var customView: CustomView
//
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityGalleryBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val actionbar = binding.myToolbar
            actionbar!!.title = "Detect Text"
            setSupportActionBar(actionbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)


            binding.button.setOnClickListener {
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                resultLauncher.launch(gallery)
            }

            if (!allPermissionGranted()) {
                ActivityCompat.requestPermissions(
            this, Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }

//            readData()
            checkData()

        for (i in 0..1) {
            when (i) {
                0 -> {
                    setFlag(alphabet.toString()) {
                        binding.camFlagIn.text = it
                    }
                }
                1 -> {
                    setFlag(targetLanguage) {
                        binding.camFlagOut.text = it
                    }
                }
            }
        }

        checkAlphabet(alphabet)

        addAds()

        }

        private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                imageUri = data?.data
                binding.imageView.setImageURI(imageUri)
                imageToBitmap(imageUri!!)
            }
        }

        private fun checkData() {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
            targetLanguage = prefs.getString(getString(com.example.translation_app.R.string.cam_language), "").toString()
            alphabet = prefs.getString(getString(com.example.translation_app.R.string.alphabet_language_1), "").toString()
            val textRecognition = TextRecognition()
            if (targetLanguage.contains("-")) {
                targetLanguage = targetLanguage.split("-")[0]
            }
            textRecognition.identifyLanguage(targetLanguage) {
                targetLanguage = it
            }

        }

        private fun checkAlphabet(source: String) {
            alphabet = when(source) {
                "Japanese" -> "Japanese"
                "Chinese" -> "Chinese"
                "Korean" -> "Korean"
                "Devanagari" -> "Devanagari"
                "Russian" -> "Russian"
                "Arabic" -> "Arabic"
                else -> "Latin"
            }
        }

    private fun setFlag(lang: String, param: (String) -> Unit) {
        val translator = Translator()
        translator.setFlag(lang) {
            param(it)
        }
    }
    override fun onSupportNavigateUp(): Boolean {
            onBackPressed()
            return true
        }

        override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
            val inflater = menuInflater
            inflater.inflate(com.example.translation_app.R.menu.start_toolbar, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
            com.example.translation_app.R.id.models -> {
                val intent = Intent(this, ModelActivity::class.java)
                startActivity(intent)
                true
            }

            else -> {
                // The user's action isn't recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }

        private fun allPermissionGranted() =
            Constants.REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    baseContext, it
                ) == PackageManager.PERMISSION_GRANTED
            }


//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//            super.onActivityResult(requestCode, resultCode, data)
//            if(resultCode == RESULT_OK && requestCode == pickImage) {
//                imageUri = data?.data
//                binding.imageView.setImageURI(imageUri)
//                imageToBitmap(imageUri!!)
//            }
//        }

        private fun imageToBitmap(imageUri: Uri) {
            try {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(imageUri, "r") //open file descriptor for the file for reading
                val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor //get file descriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor) //decode file descriptor into bitmap
                parcelFileDescriptor.close() //close the file descriptor

                if (image != null) {
                    val inputImage = InputImage.fromBitmap(image, 0)

                    var recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    recognizer.process(inputImage)
                        .addOnSuccessListener {visionText ->

                            if(visionText.text != "") {
                                binding.preview.setBackgroundResource(R.drawable.camera_border_2)
                                val inputText = visionText.text
                                val rec = TextRecognition()
//                                processTextBlock(visionText)

                                rec.identifyLanguage(inputText) {
                                    if (it == "und") {
                                        binding.galleryText.text = "Cannot identify language"
                                    }
                                    else {
                                        rec.initTranslator(inputText, it, targetLanguage) {
                                            binding.galleryText.text = it
                                        }
                                    }
                                }
                            } else {
                                binding.preview.setBackgroundResource(R.drawable.camera_border)
                            }

//                            image.close()
                        }
                        .addOnFailureListener { e ->
//                        param("Failed to recognize text ${e.message}")
//                            Toast.makeText(this, "Failed to recognize text ${e.message}", Toast.LENGTH_SHORT).show
                            binding.galleryText.text = "Failed to recognize text ${e.message}"
                        }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


    private fun addAds() {
        val adSize = com.google.android.gms.ads.AdSize.BANNER
        val adView = binding.adView
//        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
//        adView.setAdSize(adSize)

        val testDeviceIds = Arrays.asList("33BE2250B43518CCDA7DE426D04EE231")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun drawRectangle(rect: Rect) {
        customView = CustomView(this, null, rect)
        binding.preview.addView(customView)
    }

    private fun clearCanvas() {
        customView.clearCanvas()
    }

    class CustomView(context: Context?, attrs: AttributeSet?, rect: Rect) :
        View(context, attrs)
    {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        private val boundingBox = rect
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawRect(boundingBox, paint)

        }
        fun clearCanvas() {
            val transparent = Paint()
            transparent.alpha = 0
        }

    }

}