package com.sugarspoon.colordetekt.ui.color

import com.sugarspoon.colordetekt.model.ColorChosen

sealed class ColorIntent {
    class ReadColor(val color: ColorChosen) : ColorIntent()
    class ChosenColor(val color: ColorChosen?) : ColorIntent()
    class Clear() : ColorIntent()
    class ToggleFlash(val isEnable: Boolean) : ColorIntent()
}
