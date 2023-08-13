package com.example.translation_app

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.translation_app.databinding.ActivityGalleryBinding
import java.io.FileDescriptor
import java.io.IOException

class GalleryActivity: AppCompatActivity() {

        private val pickImage = 100
        private var imageUri: Uri? = null
        private lateinit var binding: ActivityGalleryBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = ActivityGalleryBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Toast.makeText(
                this, "Gallery Page", Toast.LENGTH_SHORT
            ).show()

            title = "Gallery"

            binding.button.setOnClickListener {
                val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(gallery, pickImage)
            }

            if (allPermissionGranted()) {
                Toast.makeText(this, "Permission Granted",
                    Toast.LENGTH_SHORT).show()
                } else {
                ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSION,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }
        }

        private fun allPermissionGranted() =
            Constants.REQUIRED_PERMISSION.all {
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
                goToImageViewer(image)
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

                //Pop intent
                val in1 = Intent(this, ImageActivity::class.java)
                in1.putExtra("image", filename)
                startActivity(in1)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

}