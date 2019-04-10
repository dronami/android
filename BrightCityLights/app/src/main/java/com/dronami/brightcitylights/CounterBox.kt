package com.dronami.brightcitylights

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.min

class CounterBox(x: Int, y: Int, bmp: Bitmap): NumberDisplay(x, y, bmp) {
    var doubleDigitDestRects: MutableList<Rect> = mutableListOf()
    var tripleDigitDestRects: MutableList<Rect> = mutableListOf()
    var tripleDigitWidth: Int = 0
    var tripleDigitHeight: Int = 0

    override fun initDigits(width: Int, height: Int, sheet: Bitmap) {
        super.initDigits(width, height, sheet)
        tripleDigitWidth = (digitWidth.toFloat() * 0.75f).toInt()
        tripleDigitHeight = (digitHeight.toFloat() * 0.75f).toInt()
        repeat(3) {
            digitValues.add(0)
        }

        val dLeft: Int = posX + (boxBitmap.width * 0.2555f).toInt()
        val dTop: Int = posY + (boxBitmap.height * 0.39f).toInt()
        doubleDigitDestRects.add(Rect(dLeft, dTop, dLeft + digitWidth, dTop + digitHeight))
        doubleDigitDestRects.add(Rect(dLeft + (digitWidth * 0.85f).toInt(), dTop, (dLeft + digitWidth * 1.85f).toInt(), dTop + digitHeight))

        val dTLeft: Int = posX + (boxBitmap.width * 0.247f).toInt()
        val dTTop: Int = posY + (boxBitmap.height * 0.485f).toInt()
        tripleDigitDestRects.add(Rect(dTLeft, dTop, dTLeft + tripleDigitWidth, dTTop + tripleDigitHeight))
        tripleDigitDestRects.add(Rect(dTLeft + (tripleDigitWidth * 0.8f).toInt(), dTop, (dTLeft + tripleDigitWidth * 1.8f).toInt(), dTTop + tripleDigitHeight))
        tripleDigitDestRects.add(Rect(dTLeft + (tripleDigitWidth * 1.6f).toInt(), dTop, (dTLeft + tripleDigitWidth * 2.6f).toInt(), dTTop + tripleDigitHeight))

        digitDestRects = doubleDigitDestRects
    }

    fun updateCounter(count: Int) {
        // Just stop at 99 if user somehow goes over three digits
        val displayCount = min(count, 999)



        if (displayCount > 99) {
            digitValues[2] = displayCount % 10
            digitValues[1] = (displayCount % 100) / 10
            digitValues[0] = displayCount / 100

            digitDestRects = tripleDigitDestRects
        } else {
            digitValues[1] = displayCount % 10
            digitValues[0] = displayCount / 10

            digitDestRects = doubleDigitDestRects
        }
    }
}