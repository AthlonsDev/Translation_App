package com.example.translation_app.ui.dashboard

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
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
            if (binding.urlButton.visibility == View.VISIBLE) {
                binding.urlButton.visibility = View.INVISIBLE
                binding.urlEditText.visibility = View.INVISIBLE
                binding.urlButton.isClickable = false
                binding.urlEditText.isClickable = false
                binding.previewImage.setImageBitmap(null)
            }
            binding.urlEditText.visibility = View.VISIBLE
            binding.urlEditText.isClickable = true
            binding.urlEditText.isSelected = true

            binding.urlButton.visibility = View.VISIBLE
            binding.urlButton.isClickable = true
            binding.urlEditText.isFocusableInTouchMode = true
            binding.urlEditText.isFocusable = true

            binding.urlEditText.requestFocus()

            // open keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.urlEditText, InputMethodManager.SHOW_IMPLICIT)

//            imageFromUrl("https://shop.signbox.co.uk/uploads/assets/2018%20-%20Assets/STATUTORY/Safety%20%26%20Pictogram%20Signs/Action/Action%20Prohibition/Prohibition%20FOAM-02.jpg")
        }

        binding.urlButton.setOnClickListener {
            val url = binding.urlEditText.text.toString()
//            imageFromUrl(url)
            //hide button and edittext and keyboard
            binding.urlButton.visibility = View.INVISIBLE
            binding.urlButton.isClickable = false
            binding.urlEditText.visibility = View.INVISIBLE
            binding.urlEditText.isClickable = false
            binding.urlEditText.isSelected = false
            binding.urlEditText.clearFocus()

            hideKeyboardFrom(requireContext(), binding.urlEditText)

            imageFromUrl(url)
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

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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

    private var target: Target? = null
//    fun getPosterBitmap(): Bitmap? {
//        var posterBitmap: Bitmap? = null
//        target = object : Target() {
//            fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom?) {
//                posterBitmap = bitmap
//            }
//
//            fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
//            fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
//        }
//        return posterBitmap
//    }

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
                                binding.outputText.text = translatedText
                            }
                        }
                    }
                }
                override fun onBitmapFailed(e: Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                    Toast.makeText(requireContext(), "Failed to load image ${e.toString()}", Toast.LENGTH_SHORT).show()
                }

                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                    Toast.makeText(requireContext(), "Loading image...", Toast.LENGTH_SHORT).show()
                }
            })



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
