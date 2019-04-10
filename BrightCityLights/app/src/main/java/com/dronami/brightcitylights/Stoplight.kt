package com.dronami.brightcitylights

import android.graphics.*

class Stoplight {
    var stoplightActive: Boolean = false
    private val stoplightBitmap: Bitmap
    private val redRect: Rect
    private val yellowRect: Rect
    private val greenRect: Rect
    private val poleRect: Rect

    private var lastPosY: Float
    private var posX: Float
    private var posY: Float

    private val topY: Float
    private val centerY: Float

    private val redPaint: Paint = Paint()
    private val yellowPaint: Paint = Paint()
    private val greenPaint: Paint = Paint()
    private val polePaint: Paint = Paint()

    private val redStart: Int = Color.rgb(30, 0, 0)
    private val redEnd: Int = Color.rgb(216, 0, 0)
    private val yellowStart: Int = Color.rgb(64, 50, 0)
    private val yellowEnd: Int = Color.rgb(240, 200, 0)
    private val greenStart: Int = Color.rgb(0, 30, 0)
    private val greenEnd: Int = Color.rgb(0, 216, 0)
    private val poleColor: Int = Color.rgb(120,120,120)

    private var stoplightStatus: Int = 0
    private var lightCounter: Int = 0
    private var counterRatio: Float = 0f
    private val dropDuration: Float = 30f
    private val lightDuration: Float = 20f
    private val midDuration: Float = 10f


    constructor(sBitmap: Bitmap, screenWidth: Int, screenHeight: Int) {
        stoplightBitmap = sBitmap
        topY = 0f - sBitmap.height
        centerY = ((screenHeight / 2.0f) - (sBitmap.height / 2.0f))

        posX = ((screenWidth / 2.0f) - (sBitmap.width / 2.0f))
        posY = topY
        lastPosY = topY

        val rectSize: Int = (sBitmap.width / 4.0f).toInt()
        val rectX: Int = ((sBitmap.width / 2.0f) - (rectSize/2.0f)).toInt()

        redRect = Rect(rectX, (sBitmap.height * 0.15f).toInt(),
            rectX + rectSize, ((sBitmap.height * 0.15f) + rectSize).toInt())
        yellowRect = Rect(rectX, (sBitmap.height * 0.41f).toInt(),
            rectX + rectSize, ((sBitmap.height * 0.41f) + rectSize).toInt())
        greenRect = Rect(rectX, (sBitmap.height * 0.67f).toInt(),
            rectX + rectSize, ((sBitmap.height * 0.67f) + rectSize).toInt())
        val poleWidth: Int = (sBitmap.width * 0.12f).toInt()
        poleRect = Rect((posX+(sBitmap.width / 2.0f)-poleWidth/2.0f).toInt(), (posY - screenHeight / 2.0f).toInt(),
            (posX+(sBitmap.width / 2.0f)+poleWidth/2.0f).toInt(), (posY * 0.9f).toInt())
        polePaint.color = poleColor

        redRect.offset(posX.toInt(), posY.toInt())
        yellowRect.offset(posX.toInt(), posY.toInt())
        greenRect.offset(posX.toInt(), posY.toInt())
    }

    fun draw(canvas: Canvas) {
        if (stoplightStatus == 0) {
            return
        }
        canvas.drawRect(poleRect, polePaint)
        canvas.drawRect(redRect, redPaint)
        canvas.drawRect(yellowRect, yellowPaint)
        canvas.drawRect(greenRect, greenPaint)
        canvas.drawBitmap(stoplightBitmap, posX, posY, null)
    }

    fun startCountdown() {
        posY = topY
        stoplightStatus = 1
        lightCounter = 0

        redPaint.color = redStart
        yellowPaint.color = yellowStart
        greenPaint.color = greenStart

        stoplightActive = true
    }

    fun updateStoplight() {
        if (stoplightStatus == 1) {
            if (lightCounter > dropDuration) {
                stoplightStatus = 2
                lightCounter = 0
                SoundManager.playSound(SoundManager.Sounds.STOPLIGHT.value)
            } else {
                counterRatio = lightCounter / dropDuration
                updatePosition(topY + (centerY - topY) * counterRatio)
                lightCounter++
            }
        } else if (stoplightStatus == 2) {
            if (lightCounter > lightDuration) {
                stoplightStatus = 3
                lightCounter = 0
            } else {
                counterRatio = lightCounter / lightDuration
                redPaint.color = ColorBlender.blendColors(redStart, redEnd, counterRatio)
                lightCounter++
            }
        } else if (stoplightStatus == 3 || stoplightStatus == 5 || stoplightStatus == 7) {
            if (lightCounter > midDuration) {
                stoplightStatus++
                lightCounter = 0
                if (stoplightStatus != 8 ) {
                    SoundManager.playSound(SoundManager.Sounds.STOPLIGHT.value)
                } else {
                    SoundManager.playSound(SoundManager.Sounds.WHOOSH.value)
                }
            } else {
                lightCounter++
            }
        } else if (stoplightStatus == 4) {
            if (lightCounter > lightDuration) {
                stoplightStatus = 5
                lightCounter = 0
            } else {
                counterRatio = lightCounter / lightDuration
                yellowPaint.color = ColorBlender.blendColors(yellowStart, yellowEnd, counterRatio)
                lightCounter++
            }
        } else if (stoplightStatus == 6) {
            if (lightCounter > lightDuration) {
                stoplightStatus = 7
                lightCounter = 0
            } else {
                counterRatio = lightCounter / lightDuration
                greenPaint.color = ColorBlender.blendColors(greenStart, greenEnd, counterRatio)
                lightCounter++
            }
        } else if (stoplightStatus == 8) {
            if (lightCounter > (dropDuration/2.0f)) {
                stoplightStatus = 0
                stoplightActive = false
            } else {
                counterRatio = lightCounter / (dropDuration/2.0f)
                updatePosition(centerY + (topY - centerY) * counterRatio)
                lightCounter++
            }
        }
    }

    private fun updatePosition(newY: Float) {
        lastPosY = posY.toInt().toFloat()
        posY = newY.toInt().toFloat()
        val offsetAmount = (posY-lastPosY).toInt()
        poleRect.offset(0, offsetAmount)
        redRect.offset(0, offsetAmount)
        yellowRect.offset(0, offsetAmount)
        greenRect.offset(0, offsetAmount)
    }
}