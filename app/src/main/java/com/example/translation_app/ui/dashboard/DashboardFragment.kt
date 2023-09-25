package com.example.translation_app.ui.dashboard

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
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.net.URL
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
        val viewModel =
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

        binding.fab.setOnClickListener {
            if(binding.fab2.visibility == View.VISIBLE) {
                binding.fab2.visibility = View.INVISIBLE
                binding.fab3.visibility = View.INVISIBLE
                binding.fab2.isClickable = false
                binding.fab3.isClickable = false
                binding.previewImage.setImageBitmap(null)
            } else {
                binding.fab2.visibility = View.VISIBLE
                binding.fab3.visibility = View.VISIBLE
                binding.fab2.isClickable = true
                binding.fab3.isClickable = true
            }
        }

        binding.fab2.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, pickImage)
        }

        binding.fab3.setOnClickListener {
            imageFromUrl(URL("https://shop.signbox.co.uk/uploads/assets/2018%20-%20Assets/STATUTORY/Safety%20%26%20Pictogram%20Signs/Action/Action%20Prohibition/Prohibition%20FOAM-02.jpg"))
        }

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
            imageToBitmap(imageUri!!)
        }
    }

    fun imageFromUrl(url: URL) {
        try {
            val image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            binding.previewImage.setImageBitmap(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun imageToBitmap(uri: Uri) {
        try {
            val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(imageUri!!, "r")
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
                        binding.outputText.text = translatedText
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

}

