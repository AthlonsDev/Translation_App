package com.example.translation_app.Models

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.example.translation_app.MainActivity
import com.example.translation_app.R
import com.example.translation_app.databinding.ActivityModelBinding
import com.example.translation_app.start.ItemsViewModel
import com.example.translation_app.start.StartActivity
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

        checkModels()

        addRec()

        checkData()

        binding.finishButton.setOnClickListener {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
            val previouslyStarted = prefs.getBoolean(getString(com.example.translation_app.R.string.prev_started), false)
            if (!previouslyStarted) {
                val edit = prefs.edit()
                edit.putBoolean(getString(com.example.translation_app.R.string.prev_started), java.lang.Boolean.TRUE)
                edit.commit()
                startApp()
            }
        }

        adapter?.onClickListener(object : ModelsAdapter.OnClickListener {
            override fun onClicks(pos: Int, text: CharSequence, item: ModelsViewModel) {
                when (pos) {
                    0 -> {
                        val englishModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
                        manageModels(englishModel, text, pos)
                    }
                    1 -> {
                        val frenchModel = TranslateRemoteModel.Builder(TranslateLanguage.FRENCH).build()
                        manageModels(frenchModel, text, pos)
                    }
                    2 -> {
                        val germanModel = TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build()
                        manageModels(germanModel, text, pos)
                    }
                    3 -> {
                        val spanishModel = TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build()
                        manageModels(spanishModel, text, pos)
                    }
                    4 -> {
                        val chineseModel = TranslateRemoteModel.Builder(TranslateLanguage.CHINESE).build()
                        manageModels(chineseModel, text, pos)
                    }
                    5 -> {
                        val hindiModel = TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build()
                        manageModels(hindiModel, text, pos)
                    }
                    6 -> {
                        val arabicModel = TranslateRemoteModel.Builder(TranslateLanguage.ARABIC).build()
                        manageModels(arabicModel, text, pos)
                    }
                    7 -> {
                        val russianModel = TranslateRemoteModel.Builder(TranslateLanguage.RUSSIAN).build()
                        manageModels(russianModel, text, pos)
                    }
                    8 -> {
                        val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
                        manageModels(japaneseModel, text, pos)
                    }
                    9 -> {
                        val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()
                        manageModels(koreanModel, text, pos)
                    }
                    10 -> {
                        val italianModel = TranslateRemoteModel.Builder(TranslateLanguage.ITALIAN).build()
                        manageModels(italianModel, text, pos)
                    }
                }
            }
        })
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

    private fun manageModels(model: TranslateRemoteModel, text: CharSequence, index: Int) {
        val modelManager = RemoteModelManager.getInstance()
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        if (text == "Remove") {
            modelManager.deleteDownloadedModel(model)
                .addOnSuccessListener {
                    // Model deleted.
                    adapter?.condition = "Download"
                    adapter?.notifyItemChanged(index)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
        } else {
            adapter?.condition = "model downloading"
//            adapter?.notifyDataSetChanged()
            modelManager.download(model, conditions)
                .addOnSuccessListener {
                    // Model downloaded.
                    adapter?.condition = "model downloaded"
                    adapter?.notifyItemChanged(index)
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
                for (model in models) {
                    when (model) {
                        englishModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(0)
                        }
                        frenchModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(1)
                        }
                        germanModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(2)
                        }
                        spanishModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(3)
                        }
                        chineseModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(4)
                        }
                        hindiModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(5)
                        }
                        arabicModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(6)
                        }
                        russianModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(7)
                        }
                        japaneseModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(8)
                        }
                        koreanModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(9)
                        }
                        italianModel -> {
                            adapter?.condition = "model downloaded"
                            adapter?.notifyItemChanged(10)
                        }
                    }
                }

            }
            .addOnFailureListener {
                // Error.
            }

    }

        private fun addRec() {
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

    private fun checkData() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val previouslyStarted = prefs.getBoolean(getString(com.example.translation_app.R.string.prev_started), false)
        if (previouslyStarted) {
            binding.finishButton.isEnabled = false
            binding.finishButton.isVisible = false
        }
    }

    private fun startApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}