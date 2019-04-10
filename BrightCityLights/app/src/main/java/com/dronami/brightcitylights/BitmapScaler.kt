package com.dronami.brightcitylights

import android.graphics.Bitmap

object BitmapScaler {
    fun scaleBitmap(source: Bitmap, widthRatio: Float, customSize: Float = 1.0f): Bitmap {
        val scaledWidth: Int = (source.width * widthRatio * customSize).toInt()
        val scaledHeight: Int = (source.height * (scaledWidth / source.width.toFloat())).toInt()

        return Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
    }

    fun scaleBitmap(source: Bitmap, width: Int): Bitmap {
        val scaledHeight: Int = (source.height * (width / source.width.toFloat())).toInt()

        return Bitmap.createScaledBitmap(source, width, scaledHeight, true)
    }
}