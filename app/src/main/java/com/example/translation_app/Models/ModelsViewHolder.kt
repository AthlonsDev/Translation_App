package com.example.translation_app.Models

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.translation_app.R

class ModelsViewHolder(private val itemView: View): ViewHolder(itemView) {
    private val text_model = itemView.findViewById<TextView>(R.id.text_mod)
    val model_button = itemView.findViewById<Button>(R.id.button_mod)
    private val progress_bar = itemView.findViewById< ProgressBar>(R.id.progress_bar)

    fun bind(item: ModelsViewModel) {
        text_model.text = item.text_model
    }

}