package com.dronami.brightcitylights

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

open class NumberDisplay(x:Int, y: Int, bmp: Bitmap) {
    var boxBitmap: Bitmap = bmp
    var posX: Int = x
    var posY: Int = y

    var digitValues: MutableList<Int> = mutableListOf(0,0,0,0)
    lateinit var digitSheet: Bitmap
    var digitSourceRects: MutableList<Rect> = mutableListOf()
    var digitDestRects: MutableList<Rect> = mutableListOf()
    var digitWidth: Int = 0
    var digitHeight: Int = 0
    val sheetRows: Int = 2
    val sheetColumns: Int = 5

    open fun drawBox(canvas: Canvas) {
        canvas.drawBitmap(boxBitmap, posX.toFloat(), posY.toFloat(), null)

        for ((digitIndex, r) in digitDestRects.withIndex()) {
            canvas.drawBitmap(digitSheet, digitSourceRects[digitValues[digitIndex]], r, null)
        }
    }

    open fun initDigits(width: Int, height: Int, sheet: Bitmap) {
        digitWidth = width
        digitHeight = height
        digitSheet = sheet

        for (r in 0 until sheetRows) {
            for (c in 0 until sheetColumns) {
                digitSourceRects.add(Rect(c*digitWidth, r*digitHeight, (c*digitWidth)+digitWidth, (r*digitHeight)+digitHeight))
            }
        }
    }
}