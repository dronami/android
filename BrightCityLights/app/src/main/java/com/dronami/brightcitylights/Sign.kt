package com.dronami.brightcitylights

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import kotlin.math.sin

class Sign {
    private var bitmapOn: Bitmap
    private lateinit var bitmapOff: Bitmap

    private var signRect: Rect
    var signOn: Boolean = true

    var signHeight: Int = 0
    private var shaking: Boolean = false
    private val shakeDuration: Int = 20
    private var shakeCounter: Int = 0
    private val shakeIntensity: Float = 6f
    private var shakeAmount: Float = 0f

    var posX: Float = 0f
    private var posY: Float = 0f
    private var lastPosX: Float = 0f
    private var lastPosY: Float = 0f

    constructor(bOn: Bitmap, bOff: Bitmap?, sR: Rect) {
        bitmapOn = bOn
        if (bOff != null) {
            bitmapOff = bOff
        }
        signRect = sR
        posX = signRect.left.toFloat()
        lastPosX = posX
        posY = signRect.top.toFloat()
        lastPosY = posY

        signHeight = bitmapOn.height
    }

    fun startShake() {
        shaking = true
        shakeCounter = 0
    }

    fun updatePosition(pX: Float, pY: Float) {
        posX = pX.toInt().toFloat()
        posY = pY.toInt().toFloat()
        val offsetX: Float = (posX - lastPosX).toInt().toFloat()
        val offsetY: Float = (posY - lastPosY).toInt().toFloat()
        lastPosX = posX.toInt().toFloat()
        lastPosY = posY.toInt().toFloat()

        signRect.offset(offsetX.toInt(), offsetY.toInt())
    }

    fun handleTouchEvent(event: MotionEvent?): Int {
        if (event != null) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (signRect.contains(event.x.toInt(), event.y.toInt())) {
                    startShake()
                    return 1
                }
            }
        }
        return 0
    }

    fun update() {
        if (shaking) {
            if (shakeCounter > shakeDuration) {
                shaking = false
                shakeAmount = 0f
            } else {
                shakeAmount = (sin(shakeCounter.toDouble() * 1.8f) * shakeIntensity).toFloat()
                shakeCounter++
            }
        }
    }

    fun draw(canvas: Canvas) {
        if (signOn) {
            canvas.drawBitmap(bitmapOn, signRect.left.toFloat() + shakeAmount, signRect.top.toFloat(), null)
        } else {
            canvas.drawBitmap(bitmapOff, signRect.left.toFloat() + shakeAmount, signRect.top.toFloat(), null)
        }
    }
}