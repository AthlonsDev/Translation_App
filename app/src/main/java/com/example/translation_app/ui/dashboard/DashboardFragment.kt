package com.example.translation_app.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.Constants
import com.example.translation_app.R
import com.example.translation_app.TextRecognition
import com.example.translation_app.dataStore
import com.example.translation_app.databinding.FragmentDashboardBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private var imageCapture: ImageCapture?= null

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val pickImage = 100
    private var imageUri: Uri? = null
    var bmp: Bitmap? = null
    lateinit var alphabet: String
    lateinit var targetLanguage: String
    lateinit var translatedText: String
    lateinit var customView: CustomView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


//        binding.galleryBtn.setOnClickListener {
//            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivityForResult(gallery, pickImage)
//        }

        binding.cameraBtn.setOnClickListener {
            binding.outputText.text = translatedText
        }

//        binding.inputText.movementMethod = ScrollingMovementMethod()
        binding.outputText.movementMethod = ScrollingMovementMethod()

//        Camera Permissions
        if (allPermissionGranted(requireContext())) {
//          start camera
            cameraExecutor = Executors.newSingleThreadExecutor()
            cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        //        listening for data from the camera
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startLiveCamera(cameraProvider)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        readData()

//        binding.outputText.visibility = View.INVISIBLE

        return root
    }

    fun allPermissionGranted(context: Context) =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                context, it
            ) == PackageManager.PERMISSION_GRANTED
        }


    private fun drawRectangle(rect: Rect) {
        customView = CustomView(requireContext(), null, rect)
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

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            @SuppressLint("UnsafeOptInUsageError")
            val image = imageProxy.image
            if (image != null) {
                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
                var recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(inputImage)
                    .addOnSuccessListener {visionText ->

                        if(visionText.text != "") {
                            binding.preview.setBackgroundResource(R.drawable.camera_border_2)
                        } else {
                            binding.preview.setBackgroundResource(R.drawable.camera_border)
                        }

                        val inputText = visionText.text
                        val rec = TextRecognition()
                        rec.identifyLanguage(targetLanguage) {
                            rec.initTranslator(inputText, it, targetLanguage) {
                                translatedText = it
                            }
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
//            imageToBitmap(imageUri!!)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun readData() = runBlocking {
        launch {
            readUserPreferences()
        }
    }

    suspend fun readUserPreferences() {
        with(CoroutineScope(coroutineContext)) {
            val dataoutputKey = stringPreferencesKey("camera_language")
            val datainputKey = stringPreferencesKey("alphabet_language")
            val preferences = context?.dataStore?.data?.first()
            val cameraOutput = preferences?.get(dataoutputKey)
            val alphabetKey = preferences?.get(datainputKey)
            targetLanguage = cameraOutput.toString()
            alphabet = alphabetKey.toString()
            var cameraLang = getString(R.string.camera_label)
            binding.cameraLabel.text = "$cameraLang ${cameraOutput.toString()}"
            val textRecognition = TextRecognition()
            textRecognition.identifyLanguage(targetLanguage) {
                targetLanguage = it
            }
        }
    }

    fun drawDetectionResult(img: InputImage): Bitmap {
        val bitmap = img.bitmapInternal
        val tempBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(tempBitmap!!)
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4.0f

//        for (obj in detectedObjects) {
//            val box = obj.boundingBox
//            canvas.drawRect(box, paint)
//            val trackingId = obj.trackingId
//            for (label in obj.labels) {
//                val text = label.text
//                val index = label.index
//                val confidence = label.confidence
//                val textToShow = "$text $confidence"
//                canvas.drawText(textToShow, box.left.toFloat(), box.top.toFloat(), paint)
//            }
//        }
        return tempBitmap
    }
}

