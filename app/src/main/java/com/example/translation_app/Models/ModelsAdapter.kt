package com.example.translation_app.Models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.translation_app.Models.ModelsViewHolder
import com.example.translation_app.R
import com.example.translation_app.start.ItemsViewModel
import com.example.translation_app.start.StartAdapter

class ModelsAdapter(private val itemList: List<ModelsViewModel>): RecyclerView.Adapter<ModelsViewHolder>() {

    interface OnClickListener {
        fun onClick(pos: Int, item: ModelsViewModel)
    }

    private var onButtonClicklistener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.model_view_design, parent, false)
        return ModelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModelsViewHolder, position: Int) {
        val modelsViewModel = itemList[position]
        holder.bind(modelsViewModel)
        val index = holder.adapterPosition

        holder.model_button.setOnClickListener(View.OnClickListener {
            onButtonClicklistener?.onClick(index, modelsViewModel)
        })
    }

    fun onClickListener(listener: OnClickListener?) {
        this.onButtonClicklistener = listener
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}