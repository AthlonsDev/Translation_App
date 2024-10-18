package com.example.translation_app

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.translation_app.databinding.ActivityModelBinding
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel

class ModelActivity: AppCompatActivity() {

    private lateinit var binding: ActivityModelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model)

        binding = ActivityModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = binding.myToolbar
        actionbar!!.title = "Select Models"
        setSupportActionBar(actionbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        checkModels()


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

    private fun checkModels() {
        val englishModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
        val frenchModel = TranslateRemoteModel.Builder(TranslateLanguage.FRENCH).build()
        val germanModel = TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build()
        val spanishModel = TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build()
        val chineseModel = TranslateRemoteModel.Builder(TranslateLanguage.CHINESE).build()
        val hindiModel = TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build()
        val arabicModel = TranslateRemoteModel.Builder(TranslateLanguage.ARABIC).build()
        val russianModel = TranslateRemoteModel.Builder(TranslateLanguage.RUSSIAN).build()
        val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
        val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()
        val italianModel = TranslateRemoteModel.Builder(TranslateLanguage.ITALIAN).build()

        val modelManager = RemoteModelManager.getInstance()

        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                if (models.contains(englishModel)) {
                    binding.enButton.isEnabled = false
                    binding.enButton.text = "Downloaded"
                }

                if (models.contains(frenchModel)) {
                    binding.frButton.isEnabled = false
                    binding.frButton.text = "Downloaded"
                }

                if (models.contains(germanModel)) {
                    binding.deButton.isEnabled = false
                    binding.deButton.text = "Downloaded"
                }

                if (models.contains(spanishModel)) {
                    binding.esButton.isEnabled = false
                    binding.esButton.text = "Downloaded"
                }

                if (models.contains(italianModel)) {
                    binding.itButton.isEnabled = false
                    binding.itButton.text = "Downloaded"
                }

            }
            .addOnFailureListener {
                // Error.
            }

    }
}