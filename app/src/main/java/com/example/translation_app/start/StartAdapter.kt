package com.example.translation_app.start

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.example.translation_app.Models.ModelsViewModel
import com.example.translation_app.R

class StartAdapter(private val itemList: List<ItemsViewModel>): RecyclerView.Adapter<StartViewHolder>()
{
    interface OnSelectedListener {
        fun onClick(pos: Int, item: ItemsViewModel)
    }

    interface OnClickListener {
        fun onClick(pos: Int, item: ItemsViewModel)
    }

    private var inputClicklistener: AdapterView.OnItemSelectedListener? = null
    private var outputClicklistener: AdapterView.OnItemSelectedListener? = null
    private var onButtonClicklistener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.start_view_design, parent, false)
        return StartViewHolder(view)
    }

    override fun onBindViewHolder(holder: StartViewHolder, position: Int) {
//        holder.bind(itemList[position])
        val itemsViewHolder = itemList[position]
        holder.bind(itemsViewHolder)
        val index = holder.adapterPosition

        holder.button.setOnClickListener(View.OnClickListener {
            onButtonClicklistener?.onClick(index, itemsViewHolder)
        })
//        val image = R.drawable.start_speech
//        holder.image.setImageResource(image)
        when (position) {
            0 -> {
                holder.image.setImageResource(R.drawable.start_speech)
            }
            1 -> {
                holder.image.setImageResource(R.drawable.start_camera)
            }
            2 -> {
                holder.image.setImageResource(R.drawable.start_text)
            }
        }
    }

    fun onInputItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        this.inputClicklistener = listener
    }

    fun onOutputItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        this.outputClicklistener = listener
    }

    fun onClickListener(listener: OnClickListener?) {
        this.onButtonClicklistener = listener
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}