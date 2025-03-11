package com.example.translation_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import com.example.translation_app.Constants
import com.example.translation_app.MainActivity
import com.example.translation_app.Models.ModelActivity
import com.example.translation_app.R
import com.example.translation_app.databinding.ActivityCameraBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext


class CameraActivity: AppCompatActivity() {

        private lateinit var binding: ActivityCameraBinding
        private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
        private lateinit var cameraSelector: CameraSelector
        private lateinit var outputDirectory: File
        private lateinit var cameraExecutor: ExecutorService
        private var imageUri: Uri? = null
        lateinit var alphabet: String
        private var targetLanguage = ""
        private var translatedText = ""
        lateinit var customView: CustomView
        private val pickImage = 100
        //
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityCameraBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //set directory to store images captured by camera
            outputDirectory = getOutputDirectory()
            //set up camera executor to run camera operations
            cameraExecutor = Executors.newSingleThreadExecutor()

            //initialize camera provider and camera selector
            cameraProviderFuture = ProcessCameraProvider.getInstance(this) //binds camera lifecycle to the activity lifecycle
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA //selects the back camera as the default camera

            binding.imageText.movementMethod = android.text.method.ScrollingMovementMethod()

            if (!allPermissionGranted()) {
                ActivityCompat.requestPermissions(
                    this, Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }

            startLiveCamera(cameraProviderFuture.get())

            val actionbar = binding.myToolbar
            actionbar!!.title = "Detect Text"
            setSupportActionBar(actionbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            binding.PhotoButton.setOnClickListener {
                binding.imageText.text = translatedText
            }

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

    private fun checkData() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val language = prefs.getString(getString(com.example.translation_app.R.string.cam_language), "")
        alphabet = prefs.getString(getString(com.example.translation_app.R.string.alphabet_language_1), "").toString()
        targetLanguage = language.toString()
        val textRecognition = TextRecognition()
        if (targetLanguage.contains(" ")) {
            targetLanguage = targetLanguage.split(" ")[0]
        }
        if (alphabet.contains(" ")) {
            alphabet = alphabet.split(" ")[0]
        }
        textRecognition.identifyLanguage(targetLanguage) {
            targetLanguage = it
        }
    }

    private fun checkAlphabet(source: String) {
        when(source) {
            "Japanese" -> alphabet = "Japanese"
            "Chinese" -> alphabet = "Chinese"
            "Korean" -> alphabet = "Korean"
            "Devanagari" -> alphabet = "Devanagari"
            "Russian" -> alphabet = "Russian"
            "Arabic" -> alphabet = "Arabic"
            else -> alphabet = "Latin"
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
        private  fun allPermissionGranted() =
            Constants.REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    baseContext, it
                ) == PackageManager.PERMISSION_GRANTED
            }

        private fun getOutputDirectory(): File {
            val mediaDir = externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply {
                    mkdirs()
                }
            }

            return if (mediaDir != null && mediaDir.exists())
                mediaDir else filesDir

        }


    private fun startLiveCamera(cameraProvider: ProcessCameraProvider) {

        //set up preview to display camera feed
        val preview : Preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.preview.surfaceProvider)

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        //set up image capture to capture images
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(binding.preview.x.toInt(), binding.preview.y.toInt())) //set size equal to bounding box
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // use only last image, without processing the previous ones
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            @SuppressLint("UnsafeOptInUsageError")
            val image = imageProxy.image
            if (image != null) {
                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
//                checkAlphabet(targetLanguage)
                TextRecognition().initTextRec(inputImage, alphabet) { _it ->
                    binding.preview.setBackgroundResource(R.drawable.camera_border_2)
                    val inputText = _it
                    val rec = TextRecognition()
                    rec.identifyLanguage(inputText) {
                        if (it == "und") {
                            translatedText = "Cannot identify language"
                            binding.preview.setBackgroundResource(R.drawable.camera_border)
                        }
                        else {
                            binding.preview.setBackgroundResource(R.drawable.camera_border_2)

                            rec.initTranslator(inputText, it, targetLanguage) { _it ->
                                translatedText = _it
                            }
                        }
                    }

                    imageProxy.close()
                    }
                }
        }

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
    }

    private fun processTextBlock(result: Text) {
        // [START mlkit_process_text_block]
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox

            val rect = blockFrame?.let { Rect(it.left, blockFrame.top, blockFrame.right, blockFrame.bottom) }
            if (rect != null) {
                drawRectangle(rect)
            }
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (i in line.elements) {
                    val elementText = i.text
                    val elementCornerPoints = i.cornerPoints
                    val elementFrame = i.boundingBox
                    // draw the text block on the image
//                    val rect = elementFrame?.let { Rect(it.left, elementFrame.top, elementFrame.right, elementFrame.bottom) }
//                    if (rect != null) {
//                        drawRectangle(rect)
//                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == AppCompatActivity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
//            binding.previewImage.setImageURI(imageUri)
            imageToBitmap(imageUri!!)
        }
    }

    fun imageFromUrl(url: String) {

        Picasso.get()
            .load(url)
            .into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    binding.previewImage.setImageBitmap(bitmap)
                    val inputImage = InputImage.fromBitmap(bitmap!!, 0)
                    val textRecognition = TextRecognition()
                    textRecognition.initTextRec(inputImage, alphabet) { text ->
                        textRecognition.identifyLanguage(text) {
                            textRecognition.initTranslator(text, it, targetLanguage) {
                                translatedText = it
                                binding.imageText.text = translatedText
                            }
                        }
                        binding.imageText.text = translatedText
                    }
                }
                override fun onBitmapFailed(e: Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                    Toast.makeText(this@CameraActivity, "Failed to load image ${e.toString()}", Toast.LENGTH_SHORT).show()
                }

                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                    Toast.makeText(this@CameraActivity, "Loading image...", Toast.LENGTH_SHORT).show()
                }
            })



    }


    private fun imageToBitmap(uri: Uri) {
        try {
            val parcelFileDescriptor = this.contentResolver.openFileDescriptor(imageUri!!, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()

            val inputImage = InputImage.fromBitmap(image, 0)
            val textRecognition = TextRecognition()
            binding.previewImage.setImageBitmap(image)



            textRecognition.initTextRec(inputImage, alphabet) { text ->
                textRecognition.identifyLanguage(text) {
                    textRecognition.initTranslator(text, it, targetLanguage) {
                        translatedText = it
                        binding.imageText.text = translatedText
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun drawRectangle(rect: Rect) {
        customView = CustomView(this, null, rect)
        binding.preview.addView(customView)
    }

    private fun clearCanvas() {
        customView.clearCanvas()
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
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
//


}