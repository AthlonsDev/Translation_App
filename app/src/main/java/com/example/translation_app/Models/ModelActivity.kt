package com.example.translation_app.Models

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.translation_app.R
import com.example.translation_app.databinding.ActivityModelBinding
import com.example.translation_app.start.ItemsViewModel
import com.example.translation_app.start.StartAdapter
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel

class ModelActivity: AppCompatActivity() {

    private lateinit var binding: ActivityModelBinding
    private var data = ArrayList<ModelsViewModel>()
    private var adapter: ModelsAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model)

        binding = ActivityModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionbar = binding.myToolbar
        actionbar!!.title = "Select Models"
        setSupportActionBar(actionbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listView = binding.recMod
        listView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = ModelsAdapter(data)
        listView.adapter = adapter
//        adapter = ModelsAdapter(data)

        checkModels()

//        data.add(ModelsViewModel("English"))
        addRec()

        adapter?.onClickListener(object : ModelsAdapter.OnClickListener {
            override fun onClicks(pos: Int, text: CharSequence, item: ModelsViewModel) {
                when (pos) {
                    0 -> {
                        val englishModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
//                        manageModels(englishModel, text)

                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        })

//        binding.esButton.setOnClickListener {
//            val spanishModel = TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build()
//            val modelManager = RemoteModelManager.getInstance()
//            val conditions = DownloadConditions.Builder()
//                .requireWifi()
//                .build()
//            val dTime: Long = System.currentTimeMillis()
//            binding.progressBar.isIndeterminate = true
//            binding.progressBar.visibility = android.view.View.VISIBLE
//            binding.progressBar.progress = 50
//
//            if (binding.esButton.text == "Download") {
//                modelManager.download(spanishModel, conditions)
//                    .addOnSuccessListener {
////                        get time taken to download model in seconds
//                        val eTime: Long = (System.currentTimeMillis() - dTime) / 1000
//                        binding.progressBar.progress = 100
//                        binding.progressBar.visibility = android.view.View.GONE
//                        Toast.makeText(this, "Model downloaded in $eTime", Toast.LENGTH_SHORT).show()
//                        binding.esButton.background.setTint(Color.BLACK)
//                        binding.esButton.text = "Delete"
//                    }
//                    .addOnFailureListener {
//                        // Error.
//                    }
//                binding.esButton.text = "Downloading..."
//            }
//            else {
//                binding.esButton.isEnabled = true
//                binding.esButton.background.setTint(Color.RED)
//                modelManager.deleteDownloadedModel(spanishModel)
//                    .addOnSuccessListener {
//                        binding.esButton.isEnabled = false
//                        binding.esButton.text = "Download"
//                        binding.esButton.background.setTint(Color.BLACK)
//                    }
//                    .addOnFailureListener {
//                        // Error.
//                    }
//            }
//
//        }
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

    private fun manageModels(model: TranslateRemoteModel, text: CharSequence) {
        val modelManager = RemoteModelManager.getInstance()
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        if (text == "Download") {
            modelManager.deleteDownloadedModel(model)
                .addOnSuccessListener {
                    // Model deleted.

                }
                .addOnFailureListener {
                    // Error.
                }
        } else {
            modelManager.download(model, conditions)
                .addOnSuccessListener {
                    // Model downloaded.
                }
                .addOnFailureListener {
                    // Error.
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
//                if (models.contains(englishModel)) {
//                    binding.enButton.isEnabled = false
//                    binding.enButton.text = "Downloaded"
//                }
//
//                if (models.contains(frenchModel)) {
//                    binding.frButton.isEnabled = false
//                    binding.frButton.text = "Downloaded"
//                }
//
//                if (models.contains(germanModel)) {
//                    binding.deButton.isEnabled = false
//                    binding.deButton.text = "Downloaded"
//                }
//
//                if (models.contains(spanishModel)) {
//                    binding.esButton.text = "Delete"
//                    binding.esButton.isEnabled = true
//                    binding.esButton.background.setTint(Color.RED)
//                }
//
//                if (models.contains(italianModel)) {
//                    binding.itButton.isEnabled = false
//                    binding.itButton.text = "Downloaded"
//                }

            }
            .addOnFailureListener {
                // Error.
            }

    }

        private fun addRec() {
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



            for (i in 0..10) {
                when (i) {
                    0 -> data.add(ModelsViewModel("English"))
                    1 -> data.add(ModelsViewModel("French"))
                    2 -> data.add(ModelsViewModel("German"))
                    3 -> data.add(ModelsViewModel("Spanish"))
                    4 -> data.add(ModelsViewModel("Chinese"))
                    5 -> data.add(ModelsViewModel("Hindi"))
                    6 -> data.add(ModelsViewModel("Arabic"))
                    7 -> data.add(ModelsViewModel("Russian"))
                    8 -> data.add(ModelsViewModel("Japanese"))
                    9 -> data.add(ModelsViewModel("Korean"))
                    10 -> data.add(ModelsViewModel("Italian"))
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }