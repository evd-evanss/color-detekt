package com.sugarspoon.colordetekt.ui.color

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sugarspoon.colordetekt.model.ColorChosen

class ColorViewModel : ViewModel() {

    private val _state = MutableLiveData<ColorState>()
    val state: LiveData<ColorState> = _state

    fun handle(intent: ColorIntent) {
        when (intent) {
            is ColorIntent.ReadColor -> handleOnReadColor(intent.color)
            is ColorIntent.ChosenColor -> intent.color?.let { handleOnChoseColor(it) }
            is ColorIntent.Clear -> handleOnClear()
            is ColorIntent.ToggleFlash -> handleToggleFlash(intent.isEnable)
        }
    }

    private fun handleOnReadColor(color: ColorChosen) {
        _state.value = ColorState.onRead(color)
    }

    private fun handleOnChoseColor(color: ColorChosen) {
        _state.value = ColorState.onColorChose(color)
    }

    private fun handleOnClear() {
        _state.value = ColorState.onClearState()
    }

    private fun handleToggleFlash(isEnable: Boolean) {
        _state.value = ColorState.onToggleFlash(isEnable)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ColorViewModel() as T
        }
    }
}

data class ColorState(
    val colorRead: ColorChosen? = null,
    val colorChosen: ColorChosen? = null,
    val isEnable: Boolean? = null
    ) {

    companion object {
        fun onRead(color: ColorChosen) = ColorState(colorRead = color)
        fun onColorChose(color: ColorChosen) = ColorState(colorChosen = color)
        fun onToggleFlash(isEnable: Boolean) = ColorState(isEnable = isEnable)
        fun onClearState() = ColorState()
    }
}
