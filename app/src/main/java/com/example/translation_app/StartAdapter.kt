package com.example.translation_app

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView

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
    var input = ""
    var output = ""

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



        holder.input.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (holder.input.selectedItem.toString() != null) {
//                    input = holder.input.selectedItem.toString()
                    inputClicklistener?.onItemSelected(parent, view, position, index.toLong())

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                input = ""
            }
        }
        holder.output.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (holder.input.selectedItem.toString() != null) {
//                    input = holder.input.selectedItem.toString()
                    outputClicklistener?.onItemSelected(parent, view, position, index.toLong())
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                output = ""
            }
        }

        holder.button.setOnClickListener(View.OnClickListener {
            onButtonClicklistener?.onClick(index, itemsViewHolder)
        })


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