package com.example.translation_app.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
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
//            val intent = Intent(activity, CameraActivity::class.java)
//            startActivity(intent)
            takePhoto()
        }

//        binding.inputText.movementMethod = ScrollingMovementMethod()
        binding.outputText.movementMethod = ScrollingMovementMethod()

//        Camera Permissions
        if (allPermissionGranted(requireContext())) {
//          start camera
            cameraExecutor = Executors.newSingleThreadExecutor()
            outputDirectory = getOutputDirectory()
            cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()
            startCamera(requireContext(), requireActivity(), binding.preview)
            Toast.makeText(requireContext(), "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

//        binding.previewImage.setImageBitmap(bmp)

        readData()

        binding.outputText.visibility = View.INVISIBLE

        return root
    }

    fun allPermissionGranted(context: Context) =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                context, it
            ) == PackageManager.PERMISSION_GRANTED
        }


    fun startCamera(context: Context, lifecycle: LifecycleOwner, preview: PreviewView) {
//        listening for data from the camera
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

//            connect preview use case to the preview on xml file
            val preview = Preview.Builder().build().also{
                it.setSurfaceProvider(preview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val camerasSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycle, cameraSelector, preview, imageCapture
                )

            } catch (e: Exception) {

            }
        }, ContextCompat.getMainExecutor(context))
    }

        private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(com.example.translation_app.R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir

    }

    private fun takePhoto() {
        val imageCapture = imageCapture?:return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(Constants.FILE_NAME_FORMAT,
                Locale.getDefault()).format(System.currentTimeMillis()) + ".jpg")

        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()), // Defines where the callbacks are run
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    @SuppressLint("UnsafeOptInUsageError")
                    val image = imageProxy.image // Do what you want with the image
                    //imageProxy.close() // Make sure to close the image
                    val savedUri = Uri.fromFile(photoFile)
                    imageProxyToBitmap(imageProxy)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle exception
                    Toast.makeText(requireContext(), "Error: $exception", Toast.LENGTH_LONG)
                }
            }
        )

    }



        private fun processImage(bitmap: Bitmap) {
//            binding.previewImage.setImageBitmap(bitmap)
            val tr = TextRecognition()
            var inputText = ""
            tr.initTextRec(bitmap,alphabet) { text ->
                inputText = text.toString()
//                binding.inputText.text = text.toString()
                tr.identifyLanguage(inputText) {
                    tr.initTranslator(inputText, it, targetLanguage) { translatedText ->
                        binding.outputText.visibility = View.VISIBLE
                        binding.outputText.text = translatedText
                        startCamera(requireContext(), requireActivity(), binding.preview)
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

    fun imageToBitmap(imageUri: Uri) {
        try {
            val parcelFileDescriptor = requireActivity().contentResolver.openFileDescriptor(imageUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
//                goToImageViewer(image)
            cameraProvider.unbindAll()
            processImage(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun imageProxyToBitmap(imageProxy: ImageProxy) {
        @SuppressLint("UnsafeOptInUsageError")
        val image = imageProxy.image
        val buffer = image!!.planes[0].buffer
        val bytes = ByteArray(buffer.capacity()).also { buffer.get(it) }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        processImage(bitmap)
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