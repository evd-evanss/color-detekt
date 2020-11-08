package com.sugarspoon.colordetekt.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.sugarspoon.colordetekt.R

class SimpleDividerItemDecoration(context: Context) :
    ItemDecoration() {
    private val mDivider: Drawable? = ContextCompat.getDrawable(context, R.drawable.divider_line)
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.marginLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }

}