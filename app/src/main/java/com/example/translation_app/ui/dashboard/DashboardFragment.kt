package com.example.translation_app.ui.dashboard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.translation_app.ImageActivity
import com.example.translation_app.TextRecognition
import com.example.translation_app.databinding.FragmentDashboardBinding
import java.io.FileDescriptor
import java.io.IOException

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val pickImage = 100
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.galleryBtn.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }


        return root
    }



        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if(resultCode == AppCompatActivity.RESULT_OK && requestCode == pickImage) {
                imageUri = data?.data
                binding.previewImage.setImageURI(imageUri)
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
                processImage(image)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun processImage(bitmap: Bitmap) {
//            binding.previewImage.setImageBitmap(bitmap)
            val tr = TextRecognition()
            var inputText = ""
            tr.initTextRec(bitmap) { text ->
                inputText = text.toString()
                binding.inputText.text = text.toString()
                tr.identifyLanguage(inputText) {
                    tr.initTranslator(inputText, it) { translatedText ->
                        binding.outputText.text = translatedText
                    }
                }
            }
        }

        fun goToImageViewer(bmp: Bitmap) {
            try {
                //Write file
                val filename = "bitmap.png"
                val stream = requireActivity().openFileOutput(filename, AppCompatActivity.MODE_PRIVATE)
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)

                //Cleanup
                stream.close()
                bmp.recycle()

                //Pop intent
                val in1 = Intent(activity, ImageActivity::class.java)
                in1.putExtra("image", filename)
                startActivity(in1)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}