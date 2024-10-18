package com.example.translation_app.start

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.translation_app.MainActivity
import com.example.translation_app.Models.ModelActivity
import com.example.translation_app.Models.ModelsViewModel
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
        setContentView(R.layout.activity_start)

        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)

        val listView = binding.recyclerView

        listView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val helper = LinearSnapHelper()
        helper.attachToRecyclerView(listView)
        adapter = StartAdapter(data)
        listView.adapter = adapter

        data.add(ItemsViewModel("Translator Can Listen to a Voice and Translate it Back to You", "Select you language that you want to translate to and from"))

//        get input and output language from adapter
        adapter?.onInputItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val input = parent?.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Input: $input", Toast.LENGTH_SHORT).show()
                when(id) {
                    0L -> {
                        input_speech = input
                    }
                    1L -> {
                        input_camera = input
                    }
                    2L -> {
                        input_text = input
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "Nothing Selected", Toast.LENGTH_SHORT).show()
            }
        })

        adapter?.onOutputItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val output = parent?.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Output: $output", Toast.LENGTH_SHORT).show()
                when(id) {
                    0L -> {
                        output_speech = output
                    }
                    1L -> {
                        output_camera = output
                    }
                    2L -> {
                        output_text = output
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "Nothing Selected", Toast.LENGTH_SHORT).show()
            }
        })

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
                0 -> data.add(ItemsViewModel("Translator Can Listen to a Voice and Translate it Back to You", "Select you language that you want to translate to and from"))
                1 -> data.add(ItemsViewModel("Translator Can Detect Text from the Camera or Image", "Select the language you want to translate to and from"))
                2 -> data.add(ItemsViewModel("Translator Can Translate text from Any Language", "Please select the language you want to translate to and from"))
            }
        }

    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//

        R.id.models -> {
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "Please select all languages", Toast.LENGTH_SHORT).show()
        }

    }

}