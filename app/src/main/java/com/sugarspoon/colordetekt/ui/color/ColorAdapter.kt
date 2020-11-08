package com.sugarspoon.colordetekt.ui.color

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sugarspoon.colordetekt.R
import com.sugarspoon.colordetekt.model.ColorChosen
import com.sugarspoon.colordetekt.model.ColorUtils
import kotlinx.android.synthetic.main.item_color.view.*

class ColorAdapter(context: Context, val onColorListener: OnColorListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val list: MutableList<ColorChosen> = mutableListOf()

    fun addColor(colorChosen: ColorChosen) {
        list.add(colorChosen)
        notifyDataSetChanged()
    }

    fun delete() {
        val lastPosition = list.size - 1
        if(lastPosition >= 0) {
            list.removeAt(lastPosition)
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ItemViewHolder) {
            holder.bind(list[position], onColorListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_color, parent, false)
        )
    }

    override fun getItemCount() = list.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(colorItem: ColorChosen, onColorListener: OnColorListener) {
            itemView.apply {
                colorItem.run {
                    itemColorHEXTv.text = "${ColorUtils().getColorNameFromRgb(r,g,b)} - ${this.hex}"
                }

                colorItem.hex.run {

                    itemColorIv.setBackgroundColor(Color.parseColor(this))
                    itemColorCopyTv.setOnClickListener {
                        onColorListener.onCopyClicked(this)
                    }

                }
            }
        }
    }

    interface OnColorListener {
        fun onCopyClicked(value: String)
    }

}

