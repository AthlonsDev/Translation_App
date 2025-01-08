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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceManager
import com.example.translation_app.Models.ModelActivity
import com.example.translation_app.databinding.ActivityGalleryBinding
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
                startActivityForResult(gallery, pickImage)
            }

            if (!allPermissionGranted()) {
                ActivityCompat.requestPermissions(
            this, Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }

//            readData()
            checkData()

            val translator = Translator()
            translator.setFlag(targetLanguage) {
                binding.camFlagOut.text = it
            }

//            binding.camFlagOut.setOnClickListener {
//                Toast.makeText(this, "Source language: $targetLanguage", Toast.LENGTH_SHORT).show()
//            }

//            val spinner = binding.spinner2
//            if (spinner != null) {
//                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constants.LANGUAGES)
//                spinner.adapter = adapter
//                spinner.setSelection(1, false)
//                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
//                {
//                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//                        targetLanguage = Constants.LANGUAGES[position]
//                        val translator = Translator()
//                        translator.setFlag(targetLanguage) {
//                            binding.camFlagOut.text = it
//                        }
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>) {
//                        // write code to perform some action
//                    }
//
//                }
//            }

        }

        private fun checkData() {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
            targetLanguage = prefs.getString(getString(com.example.translation_app.R.string.cam_language), "").toString()
            alphabet = prefs.getString(getString(com.example.translation_app.R.string.alphabet_language_1), "").toString()
            val textRecognition = TextRecognition()
            textRecognition.identifyLanguage(targetLanguage) {
                targetLanguage = it
            }

            checkAlphabet(alphabet)
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


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if(resultCode == RESULT_OK && requestCode == pickImage) {
                imageUri = data?.data
                binding.imageView.setImageURI(imageUri)
                imageToBitmap(imageUri!!)
            }
        }

        fun imageToBitmap(imageUri: Uri) {
            try {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(imageUri, "r")
                val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()

                if (image != null) {
                    val inputImage = InputImage.fromBitmap(image, 0);

//                    val textRecognition = TextRecognition()
//                    val text = textRecognition.recognizeText(inputImage) {
//                        if (it != null) {
//                            processTextBlock(it)
//                        }
//                    }

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

        fun goToImageViewer(bmp: Bitmap) {
            try {
                //Write file
                val filename = "bitmap.png"
                val stream = openFileOutput(filename, MODE_PRIVATE)
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)

                //Cleanup
                stream.close()
                bmp.recycle()

                val inputImage = InputImage.fromFilePath(this, Uri.parse(filename))

                val textRecognition = TextRecognition()
                textRecognition.initTextRec(inputImage, alphabet) { text ->
                    textRecognition.identifyLanguage(text) {
                        textRecognition.initTranslator(text, it, targetLanguage) {
                            binding.galleryText.text = it
                        }
                    }
                }

                //Pop intent
//                val in1 = Intent(this, ImageActivity::class.java)
//                in1.putExtra("image", filename)
//                startActivity(in1)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

//    fun imageFromUrl(url: String) {
//
//        Picasso.get()
//            .load(url)
//            .into(object : com.squareup.picasso.Target {
//                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                    binding.previewImage.setImageBitmap(bitmap)
//                    val inputImage = InputImage.fromBitmap(bitmap!!, 0)
//                    val textRecognition = TextRecognition()
//                    textRecognition.initTextRec(inputImage, alphabet) { text ->
//                        textRecognition.identifyLanguage(text) {
//                            textRecognition.initTranslator(text, it, targetLanguage) {
//                                translatedText = it
//                                binding.outputText.text = translatedText
//                            }
//                        }
//                    }
//                }
//                override fun onBitmapFailed(e: Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
//                    Toast.makeText(requireContext(), "Failed to load image ${e.toString()}", Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
//                    Toast.makeText(requireContext(), "Loading image...", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }

    private fun processTextBlock(result: Text) {
        // [START mlkit_process_text_block]
        val resultText = result.text
        for (block in result.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (i in line.elements) {
                    val elementText = i.text
                    val elementCornerPoints = i.cornerPoints
                    val elementFrame = i.boundingBox

                    // draw the text block on the image
                    val rect = elementFrame?.let { Rect(it.left, elementFrame.top, elementFrame.right, elementFrame.bottom) }
                    if (rect != null) {
                        drawRectangle(rect)
                    }


                }
            }

        }

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