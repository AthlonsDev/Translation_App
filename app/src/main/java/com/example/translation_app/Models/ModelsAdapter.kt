package com.example.translation_app.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.translation_app.Models.ModelsViewHolder
import com.example.translation_app.R
import com.example.translation_app.start.ItemsViewModel
import com.example.translation_app.start.StartAdapter

class ModelsAdapter(private val itemList: List<ModelsViewModel>): RecyclerView.Adapter<ModelsViewHolder>() {

    interface OnClickListener {
        fun onClicks(pos: Int, text: CharSequence, item: ModelsViewModel)
    }


    private var onButtonClicklistener: OnClickListener? = null
    var condition = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.model_view_design, parent, false)
        return ModelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModelsViewHolder, position: Int) {
        val modelsViewHolder = itemList[position] // get the item at the position
        holder.bind(modelsViewHolder)
        var index = holder.adapterPosition
        val button = holder.model_button


        if (condition == "model downloaded") {
            button.text = "Remove"
        } else {
            button.text = "Download"
            holder.progress_bar.visibility = View.INVISIBLE
        }


        holder.model_button.setOnClickListener {
            onButtonClicklistener?.onClicks(index, button.text, modelsViewHolder)
            if (condition == "model downloaded") {
                button.text = "Remove"
                holder.progress_bar.visibility = View.INVISIBLE
            } else if (condition == "model downloading") {
                button.text = "Downloading..."
                button.isClickable = false
                holder.progress_bar.visibility = View.VISIBLE
            } else {
                button.text = "Download"
                holder.progress_bar.visibility = View.INVISIBLE
            }
        }
    }

    fun onClickListener(listener: OnClickListener?) {
        this.onButtonClicklistener = listener
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}