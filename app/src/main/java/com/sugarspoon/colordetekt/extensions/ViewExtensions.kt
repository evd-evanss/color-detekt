package com.sugarspoon.housebook.extensions

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.TextView
import android.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sugarspoon.colordetekt.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

fun EditText.afterTextChanged(onTextChanged: ((String) -> Unit)) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            onTextChanged(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(query: CharSequence?, start: Int, before: Int, count: Int) {
            // Do nothing
        }
    })
}

fun EditText.limitLength(maxLength: Int) {
    this.filters += InputFilter.LengthFilter(maxLength)
}

fun snack(
    coordinator: androidx.coordinatorlayout.widget.CoordinatorLayout,
    message: String,
    retryText: String,
    action: (v: View) -> Unit?,
    indefinite: Boolean = true
) {
    Snackbar.make(
        coordinator,
        message,
        if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
    )
        .setAction(retryText) { v -> action(v) }
        .show()
}

fun snack(
    coordinator: CoordinatorLayout,
    message: String,
    indefinite: Boolean
) {
    val snake = Snackbar.make(
        coordinator,
        message,
        if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
    )

//    val params = snake.view.layoutParams as CoordinatorLayout.LayoutParams
//    params.gravity = Gravity.BOTTOM
//    snake.view.layoutParams = params
    snake.duration = 1000
    snake.show()
}

fun Toolbar.changeToolbarFont(){
    for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view is TextView && view.text == title) {
            view.typeface = Typeface.createFromAsset(view.context.assets, "fonts/google_sans")
            break
        }
    }
}

fun View.setVisible(visible: Boolean, useInvisible: Boolean = false) {
    visibility = when {
        visible -> View.VISIBLE
        useInvisible -> View.INVISIBLE
        else -> View.GONE
    }
}

inline fun View.afterMeasured(crossinline block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}

fun RecyclerView.setup(
    adapter: Adapter<in ViewHolder>,
    layoutManager: LayoutManager? = LinearLayoutManager(this.context),
    decoration: ItemDecoration? = null,
    hasFixedSize: Boolean = true) {
    this.adapter = adapter
    this.layoutManager = layoutManager
    this.setHasFixedSize(hasFixedSize)
    decoration?.let { this.addItemDecoration(it) }
}
