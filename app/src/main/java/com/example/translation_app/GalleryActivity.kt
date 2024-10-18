package com.example.translation_app

import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.translation_app.databinding.ActivityGalleryBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import java.io.FileDescriptor
import java.io.IOException

class GalleryActivity: AppCompatActivity() {

        private val pickImage = 100
        private var imageUri: Uri? = null
        private lateinit var binding: ActivityGalleryBinding
        lateinit var alphabet: String
        var targetLanguage: String = "it"
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

            if (allPermissionGranted()) {
                Toast.makeText(this, "Permission Granted",
                    Toast.LENGTH_SHORT).show()
                } else {
                ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
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
                    val inputImage = InputImage.fromBitmap(image, 0)
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
                            Toast.makeText(this, "Failed to recognize text ${e.message}", Toast.LENGTH_SHORT).show()
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