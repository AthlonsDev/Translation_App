package com.example.translation_app.start

//import android.R
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.translation_app.MainActivity
import com.example.translation_app.Models.ModelActivity
import com.example.translation_app.R
import com.example.translation_app.SettingsFragment
import com.example.translation_app.databinding.ActivityStartBinding
import java.util.stream.IntStream.range


class StartActivity: AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private var data = ArrayList<ItemsViewModel>()
    private var adapter: StartAdapter? = null

    private var input_speech = ""
    private var output_speech = ""
    private var input_camera = ""
    private var output_camera = ""
    private var input_text = ""
    private var output_text = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.translation_app.R.layout.activity_start)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)

        val listView = binding.recyclerView

        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val helper = LinearSnapHelper()
        helper.attachToRecyclerView(listView)
        adapter = StartAdapter(data)
        listView.adapter = adapter

        checkData()

//        data.add(ItemsViewModel("Translator Can Listen to a Voice and Translate it Back to You", "Select you language that you want to translate to and from" , R.drawable.start_speech))
        adapter?.onClickListener(object : StartAdapter.OnClickListener {
            override fun onClick(pos: Int, item: ItemsViewModel) {
                when(pos) {
                    0 -> {
//                        slide to next page
                        listView.smoothScrollToPosition(1)
//                        Toast.makeText(applicationContext, "Speech Input: $input_speech, Speech Output: $output_speech", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        listView.smoothScrollToPosition(2)
                    }
                    2 -> {
                        saveData()
                    }
                }
            }
        })


        range(0, 3).forEach { i ->
            when (i) {
                0 -> data.add(ItemsViewModel("Translate Voice to Voice", "Get Input Voice \nTranslate it \nListen to Translated Version", com.example.translation_app.R.drawable.start_speech))
                1 -> data.add(ItemsViewModel("Capture Text from Image", "Use Camera or Select an Image \nDetect Text \nGet a Translation" , com.example.translation_app.R.drawable.start_camera))
                2 -> data.add(ItemsViewModel("Text translate", "Enter Text \nTranslate it" , com.example.translation_app.R.drawable.start_text))
            }
        }

    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(com.example.translation_app.R.menu.start_toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//

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

    private fun saveData() {
        val setAct = SettingsFragment()
        if (setAct.saveUserPref(input_speech, output_speech, input_camera, output_camera, input_text)) {
            val intent = Intent(this,ModelActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Please select all languages", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkData() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val previouslyStarted = prefs.getBoolean(getString(com.example.translation_app.R.string.prev_started), false)
        if (previouslyStarted) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}