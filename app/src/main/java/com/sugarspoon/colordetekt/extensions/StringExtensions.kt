package com.sugarspoon.housebook.extensions

import android.text.TextUtils
import android.util.Patterns


fun String.isValidEmail(): Boolean {
    return if (TextUtils.isEmpty(this)) {
        false
    } else {
        Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}