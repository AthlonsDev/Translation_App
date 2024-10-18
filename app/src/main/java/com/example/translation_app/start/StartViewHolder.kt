package com.example.translation_app

import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class StartViewHolder(private val itemView: View): ViewHolder(itemView) {

    val headline = itemView.findViewById<TextView>(R.id.recycler_headline)
    private val text = itemView.findViewById<TextView>(R.id.recycler_text)
    var pos: Int = 0
    val button = itemView.findViewById<Button>(R.id.next)


    fun setOnClickListener(listener: StartAdapter.OnSelectedListener) {
        itemView.setOnClickListener {
            listener.onClick(pos, ItemsViewModel(headline.text.toString(), text.text.toString()))
        }
    }

    fun bind(item: ItemsViewModel) {
        headline.text = item.headline
        text.text = item.text
        button.text = "Next"

    }

}