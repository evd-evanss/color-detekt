package com.sugarspoon.colordetekt.model

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.sugarspoon.housebook.extensions.toHex
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.nio.ByteBuffer

typealias ColorResult = (color: Flow<ColorChosen>) -> Unit

class ColorAnalyzer(val result: ColorResult) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    private fun getColorFromYUV(image: ImageProxy): ColorChosen {
        val planes = image.planes
        val height = image.height
        val width = image.width

        // Y
        val yArr = planes[0].buffer
        val yArrByteArray = yArr.toByteArray()
        val yPixelStride = planes[0].pixelStride
        val yRowStride = planes[0].rowStride

        // U
        val uArr = planes[1].buffer
        val uArrByteArray = uArr.toByteArray()
        val uPixelStride = planes[1].pixelStride
        val uRowStride = planes[1].rowStride

        // V
        val vArr = planes[2].buffer
        val vArrByteArray = vArr.toByteArray()
        val vPixelStride = planes[2].pixelStride
        val vRowStride = planes[2].rowStride

        val y = yArrByteArray[(height * yRowStride + width * yPixelStride) / 2].toInt() and 255
        val u =
            (uArrByteArray[(height * uRowStride + width * uPixelStride) / 4].toInt() and 255) - 128
        val v =
            (vArrByteArray[(height * vRowStride + width * vPixelStride) / 4].toInt() and 255) - 128

        val r = (y + (1.370705 * v)).toInt()
        val g = (y - (0.698001 * v) - (0.337633 * u)).toInt()
        val b = (y + (1.732446 * u)).toInt()

        return ColorChosen(
            y = y,
            u = u,
            v = v,
            r = r,
            g = g,
            b = b,
            hex = Triple(r, g, b).toHex()
        )
    }

    @ExperimentalCoroutinesApi
    override fun analyze(image: ImageProxy) {
        val colors = getColorFromYUV(image)
        val hexColor = flow {
            emit(colors)
        }.flowOn(IO)
        result(hexColor)
        image.close()
    }
}