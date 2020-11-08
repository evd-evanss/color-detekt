package com.sugarspoon.housebook.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.coroutines.*

// TOAST METHODS
fun Context.showToast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}


fun Activity.hideKeyboard() {
    currentFocus?.let {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }
}

fun Activity.hideKeyboard(isVisible: Boolean) {
    if (isVisible) {
        currentFocus?.let {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        }
    }
}

fun Context.showKeyboard() {
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Context.isNetworkConnected(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    return activeNetwork != null && activeNetwork.isConnectedOrConnecting
}

fun <T> Context.splash(
    time: Long = 2000,
    origin: Activity,
    destiny: Class<T>,
    finishThen: Boolean = true
) {
    CoroutineScope(Dispatchers.Main).launch {
        delay(time)
        startActivity(Intent(this@splash, destiny))
        origin.finish()
        this.cancel()
    }
}