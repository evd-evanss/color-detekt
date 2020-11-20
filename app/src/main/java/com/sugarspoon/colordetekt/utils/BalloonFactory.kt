package com.sugarspoon.colordetekt.utils

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.sugarspoon.colordetekt.R

class BalloonFactory(
    val context: Context,
    private val fragmentLifeCycle: LifecycleOwner) {

    fun create(textRes: Int): Balloon {
        return createBalloon(context) {
            setArrowSize(ARROW_DEFAULT)
            setWidthRatio(WIDTH_MEDIUM)
            setHeight(HEIGHT_MEDIUM)
            setArrowPosition(INITIAL)
            setCornerRadius(CORNER_DEFAULT)
            setAlpha(ALPHA_DEFAULT)
            setText(context.getString(textRes))
            setTextSize(TEXT_SIZE_DEFAULT)
            setTextColorResource(R.color.white)
            setBackgroundColorResource(R.color.colorOrange)
            onBalloonClickListener?.let { setOnBalloonClickListener(it) }
            setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            setLifecycleOwner(fragmentLifeCycle)
        }
    }

    companion object {
        private const val INITIAL = 0.5f
        private const val ARROW_DEFAULT = 10
        private const val CORNER_DEFAULT = 4f
        private const val ALPHA_DEFAULT = 0.9f
        private const val TEXT_SIZE_DEFAULT = 10f
        private const val WIDTH_MEDIUM = 0.5f
        private const val HEIGHT_MEDIUM = 70
    }
}