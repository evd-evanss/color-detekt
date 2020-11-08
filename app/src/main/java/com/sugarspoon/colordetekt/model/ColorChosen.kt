package com.sugarspoon.colordetekt.model

data class ColorChosen(
    val y: Int,
    val u: Int,
    val v: Int,
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String
) {
    companion object {
        fun createEmpty() = ColorChosen(0, 0, 0, 0, 0, 0, "")
    }
}