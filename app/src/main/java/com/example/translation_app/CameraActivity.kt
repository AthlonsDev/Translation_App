package com.example.translation_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.LifecycleOwner
import com.example.translation_app.Constants
import com.example.translation_app.MainActivity
import com.example.translation_app.R
import com.example.translation_app.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity: AppCompatActivity() {
        //
        private var imageCapture: ImageCapture?= null

        private lateinit var binding: ActivityCameraBinding
        private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
        private lateinit var cameraSelector: CameraSelector
        private lateinit var outputDirectory: File
        private lateinit var cameraExecutor: ExecutorService
        private var imageUri: Uri? = null
        lateinit var alphabet: String
        private var targetLanguage = "it"
        private var translatedText = ""
        lateinit var customView: CustomView
        private val pickImage = 100
        //
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityCameraBinding.inflate(layoutInflater)
            setContentView(binding.root)

            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            if (allPermissionGranted()) {
                Toast.makeText(this, "Permission Granted",
                    Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(
                    this, Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }

            startLiveCamera(cameraProviderFuture.get())

//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
            val actionbar = supportActionBar
            actionbar!!.title = "Detect Text"
            actionbar.setDisplayHomeAsUpEnabled(true)

            binding.PhotoButton.setOnClickListener {
                binding.imageText.text = translatedText
            }

        }

        override fun onSupportNavigateUp(): Boolean {
            onBackPressed()
            return true
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
    }
        private  fun allPermissionGranted() =
            Constants.REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    baseContext, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        //
//
        private fun startCamera() {
//        listening for data from the camera
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

//            connect preview use case to the preview on xml file
                val preview = Preview.Builder().build().also{
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().build()

                val camerasSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                    )
                } catch (e: Exception) {

                }
            }, ContextCompat.getMainExecutor(this))
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

        val preview : Preview = Preview.Builder().build()
        preview.setSurfaceProvider(binding.preview.surfaceProvider)

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val point = Point()
//        val size = display?.getRealSize(point)
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(point.x, point.y))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            @SuppressLint("UnsafeOptInUsageError")
            val image = imageProxy.image
            if (image != null) {
                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
                var recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener {visionText ->

                        if(visionText.text != "") {
                            binding.preview.setBackgroundResource(R.drawable.camera_border_2)
                            val inputText = visionText.text
                            val rec = TextRecognition()
                            rec.identifyLanguage(inputText) {
                                if (it == "und") {
                                    translatedText = "Cannot identify language"
                                }
                                else {
                                    rec.initTranslator(inputText, it, targetLanguage) {
                                        translatedText = it
                                    }
                                }
                            }
                        } else {
                            binding.preview.setBackgroundResource(R.drawable.camera_border)
                        }

                        imageProxy.close()
                    }
                    .addOnFailureListener { e ->
//                        param("Failed to recognize text ${e.message}")
                        binding.preview.setBackgroundResource(R.drawable.camera_border)
                        imageProxy.close()
                    }

            }
        }

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
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
//        val bitmap = Bitmap.createBitmap(binding.preview.width, binding.preview.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        binding.preview.draw(canvas)
//        bmp = bitmap
//        val inputImage = InputImage.fromBitmap(bmp!!, 0)
//        val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//        recognizer.process(inputImage)
//            .addOnSuccessListener { visionText ->
//                val inputText = visionText.text
//                val rec = TextRecognition()
//                rec.identifyLanguage(targetLanguage) {
//                    rec.initTranslator(inputText, it, targetLanguage) {
//                        translatedText = it
//                        binding.outputText.text = translatedText
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                binding.outputText.text = "Failed to recognize text ${e.message}"
//            }
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



        override fun onDestroy() {
            super.onDestroy()
            cameraExecutor.shutdown()
        }
//


}