package com.sugarspoon.housebook.extensions

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Build
import android.text.Html


fun Int.toStateList() = ColorStateList.valueOf(this)

fun <T> TypedArray.applyIfPresent(function: (TypedArray.() -> T?), applyBlock: (T) -> Unit) {
    val value = function()
    if (value != null && (value !is Number || (value as Number).toInt() != -1)) {
        applyBlock.invoke(value)
    }
}

fun CharSequence.cleaned() = replace("[^A-z0-9]".toRegex(), "")

fun CharSequence.fromHtml() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Html.fromHtml(this.toString(), Html.FROM_HTML_MODE_COMPACT)
} else {
    Html.fromHtml(this.toString())
}

fun CharSequence.isCpf(): Boolean {
    val cpf = this.cleaned()
    if (cpf.length != 11) return false
    var rest = cpf.take(9).mapIndexed { index, char -> char.toString().toInt() * (10 - index) }.sum() % 11
    if (cpf[9].toString().toInt() == if (rest < 2) 0 else 11 - rest) {
        rest = cpf.take(10).mapIndexed { index, char -> char.toString().toInt() * (11 - index) }.sum() % 11
        return cpf[10].toString().toInt() == if (rest < 2) 0 else 11 - rest
    }
    return false
}

fun CharSequence.isCnpj(): Boolean {
    val cnpj = this.cleaned()
    if (cnpj.length != 14) return false

    val weightCNPJ = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    val cnpjWithoutDigits = cnpj.substring(0, 12)

    val sum1 = 11 - cnpjWithoutDigits.mapIndexed { index, char ->
        char.toString().toInt() * weightCNPJ[1 + index]
    }.sum() % 11
    val digit1 = if (sum1 > 9) 0 else sum1

    val sum2 = 11 - (cnpjWithoutDigits + digit1).mapIndexed { index, char ->
        char.toString().toInt() * weightCNPJ[index]
    }.sum() % 11
    val digit2 = if (sum2 > 9) 0 else sum2

    return cnpj == cnpjWithoutDigits + digit1.toString() + digit2.toString()
}

fun <R, G, B> Triple<R, G, B>.toHex(): String {
    return String.format("#%02x%02x%02x", this.first, this.second, this.third).trim()
}
