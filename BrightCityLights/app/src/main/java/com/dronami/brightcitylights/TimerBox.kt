package com.dronami.brightcitylights

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.min

class TimerBox(x: Int, y: Int, bmp: Bitmap): NumberDisplay(x, y, bmp) {

    override fun initDigits(width: Int, height: Int, sheet: Bitmap) {
        super.initDigits(width, height, sheet)
        repeat(4) {
            digitValues.add(0)
        }

        val d0Left: Int = posX + (boxBitmap.width * 0.05f).toInt()
        val d2Left: Int = posX + (boxBitmap.width * 0.3f).toInt()
        val dTop: Int = posY + (boxBitmap.height * 0.335f).toInt()
        digitDestRects.add(Rect(d0Left, dTop, d0Left + digitWidth, dTop + digitHeight))
        digitDestRects.add(Rect(d0Left + (digitWidth * 0.85f).toInt(), dTop, (d0Left + digitWidth * 1.85f).toInt(), dTop + digitHeight))
        digitDestRects.add(Rect(d2Left, dTop, d2Left + digitWidth, dTop + digitHeight))
        digitDestRects.add(Rect(d2Left + (digitWidth * 0.85f).toInt(), dTop, (d2Left + digitWidth * 1.85f).toInt(), dTop + digitHeight))
    }

    fun updateTime(time: Long) {
        // If somehow greater than an hour, just stick at maximum
        val displayTime: Long = min(time, 3599999)

        val seconds: Long = displayTime / 1000
        val minutes: Long = displayTime / 60000
        val oneSecDigit: Int = (seconds % 10).toInt()
        val tenSecDigit: Int = ((seconds % 60) / 10).toInt()
        val oneMinDigit: Int = ((minutes % 60) % 10).toInt()
        val tenMinDigit: Int = ((minutes % 60) / 10).toInt()

        digitValues[3] = oneSecDigit
        digitValues[2] = tenSecDigit
        digitValues[1] = oneMinDigit
        digitValues[0] = tenMinDigit
    }

    fun getTimeString(time: Long): String {
        val displayTime: Long = min(time, 3599999)

        val seconds: Long = displayTime / 1000
        val minutes: Long = displayTime / 60000
        val oneSecDigit: Int = (seconds % 10).toInt()
        val tenSecDigit: Int = ((seconds % 60) / 10).toInt()
        val oneMinDigit: Int = ((minutes % 60) % 10).toInt()
        val tenMinDigit: Int = ((minutes % 60) / 10).toInt()

        return "$tenMinDigit$oneMinDigit:$tenSecDigit$oneSecDigit"
    }
}