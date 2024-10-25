package com.example.translation_app.start

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.translation_app.R

class StartViewHolder(private val itemView: View): ViewHolder(itemView) {

    val headline = itemView.findViewById<TextView>(R.id.text_mod)
    private val text = itemView.findViewById<TextView>(R.id.recycler_text)
    var pos: Int = 0
    val button = itemView.findViewById<Button>(R.id.next)
    val image = itemView.findViewById<ImageView>(R.id.start_imageview)


//    fun setOnClickListener(listener: StartAdapter.OnSelectedListener) {
//        itemView.setOnClickListener {
//            listener.onClick(pos, ItemsViewModel(headline.text.toString(), text.text.toString()))
//        }
//    }

    fun bind(item: ItemsViewModel) {
        headline.text = item.headline
        text.text = item.text
        button.text = "Next"

    }

}