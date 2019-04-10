package com.dronami.brightcitylights

import android.graphics.*
import kotlin.random.Random

class CityWindow {
    var litState: Int = 0
    var lightUpCounter: Int = 0
    var lightUpState: Int = 0

    var index: Int = 0
    var twoColor: Boolean = false
    var frameRect: Rect
    var windowRect: Rect
    lateinit var sillRect: Rect
    var framePercent: Float = 0.15f
    var framePaint: Paint = Paint()
    var lightPaint: Paint = Paint()
    lateinit var sillPaint: Paint

    var hasSill: Boolean = false
    val sillChance: Float = 0.2f
    val sillMarginRatio: Float = 0.1f

    private var posX: Float = 0f
    private var posY: Float = 0f
    private var lastPosX: Float = 0f
    private var lastPosY: Float = 0f

    val silhouetteChance: Float = 0.8f
    val mooseChance: Float = 0.01f
    var hasSilhouette: Boolean = false
    var silhouetteFlipped: Boolean = false
    lateinit var silhouetteFrameList: List<Int>
    lateinit var silhouetteFrameDurations: List<Int>
    var silhouetteIndex: Int = 0
    var silhouetteCounter: Int = 0
    var silhouetteDuration: Int = 0
    val silhouetteWaitMin: Int = 40
    val silhouetteWaitMax: Int = 480

    // Pre-calculate light colors for better efficiency
    companion object WindowContents {
        lateinit var silhouetteSheet: Bitmap
        lateinit var flippedSilhouetteSheet: Bitmap
        var frameLists: List<List<Int>> = listOf(
            listOf(0,2,1,2,1,2),
            listOf(3, 4),
            listOf(5),
            listOf(6),
            listOf(8,7,8,7,8,7,8,7,8),
            listOf(9, 10, 11, 10, 11, 10, 11, 10),
            listOf(12, 13),
            listOf(16, 15, 14, 15),
            listOf(17, 18),
            listOf(19),
            listOf(20, 21, 20, 22, 20, 21, 20, 22),
            listOf(23, 24, 25, 24, 23, 24, 25, 24),
            listOf(26, 27, 28, 27),
            listOf(29),
            listOf(31, 30, 31, 30),
            listOf(32),
            listOf(33),
            listOf(34, 35, 34, 35, 34, 35, 34, 35),
            listOf(36, 37, 36, 37),
            listOf(38, 39, 38, 39, 38, 39, 38, 39),
            listOf(40, 41, 40, 41),
            listOf(42)
        )
        var frameDurations: List<List<Int>> = listOf(
            listOf(-1, 10, 15, 10, 25, 5),
            listOf(-1, -1),
            listOf(0),
            listOf(0),
            listOf(-1, 10, 10, 10, 10, 10, 10, 10, 40),
            listOf(-1, 30, 10, 10, 10, 10, 10, 15),
            listOf(5, 5),
            listOf(-1, 10, 30, 10),
            listOf(-1, 40),
            listOf(0),
            listOf(-1, 15, 10, 15, 10, 15, 10, 15),
            listOf(-1, 10, 20, 10, 10, 10, 30, 10),
            listOf(-1, 10, 40, 10),
            listOf(0),
            listOf(-1, 30, 10, 30),
            listOf(0),
            listOf(0),
            listOf(-1, 5, 10, 5, 10, 5, 10, 5),
            listOf(-1, 10, 15, 10),
            listOf(-1, 5, 10, 5, 10, 5, 10, 5),
            listOf(-1, 10, 15, 10),
            listOf(0)
        )
        var frameRects: MutableList<Rect>  = mutableListOf()
        var flippedFrameRects: MutableList<Rect>  = mutableListOf()
        val numSheetRows: Int = 8

        var savedColors: MutableList<Int> = mutableListOf()
        var savedColorsB: MutableList<Int> = mutableListOf()
        var savedColorsC: MutableList<Int> = mutableListOf()
        val lightUpDuration = 20

        fun initSavedColors() {
            for (i in 0..CityWindow.lightUpDuration) {
                val ratio: Float = i/lightUpDuration.toFloat()
                savedColors.add(ColorBlender.blendColors(ColorManager.lightDarkColor, ColorManager.lightBrightColor, ratio))
                savedColorsB.add(ColorBlender.blendColors(ColorManager.lightBrightColor, ColorManager.lightBrightColorB, ratio))
                savedColorsC.add(ColorBlender.blendColors(ColorManager.lightBrightColorB, ColorManager.lightDarkColor, ratio))
            }
        }

        fun initSilhouettes(sSheet: Bitmap, windowSize: Int) {
            silhouetteSheet = Bitmap.createScaledBitmap(sSheet, windowSize * numSheetRows, windowSize * numSheetRows, true)
            flippedSilhouetteSheet = Bitmap.createScaledBitmap(silhouetteSheet, silhouetteSheet.width * -1, silhouetteSheet.height, true)
            val sheetWidth = silhouetteSheet.width
            val rectSize = sheetWidth / numSheetRows
            frameRects.clear()
            flippedFrameRects.clear()
            for (r in 0 until numSheetRows) {
                for (c in 0 until numSheetRows) {
                    frameRects.add(
                        Rect(
                            c * rectSize, r * rectSize,
                            (c * rectSize) + rectSize, (r * rectSize) + rectSize
                        )
                    )

                    flippedFrameRects.add(
                        Rect(
                            sheetWidth - (c * rectSize) - rectSize, r * rectSize,
                            sheetWidth - (c * rectSize), (r * rectSize) + rectSize
                        )
                    )
                }
            }
        }

        fun drawSilhouette(canvas: Canvas, destRect: Rect, frame: Int, flipped: Boolean) {
            if (flipped) {
                canvas.drawBitmap(flippedSilhouetteSheet, flippedFrameRects[frame], destRect, null)
            } else {
                canvas.drawBitmap(silhouetteSheet, frameRects[frame], destRect, null)
            }
        }

    }

    constructor(idx: Int, xPos: Int, yPos: Int, size: Int, frameColor: Int, sillColor: Int, hasS: Boolean = true, tC: Boolean = false) {
        val frameSize: Int = (size * framePercent).toInt()
        twoColor = tC
        index = idx
        frameRect = Rect(xPos, yPos, xPos+size, yPos+size)
        windowRect = Rect(xPos+(frameSize), yPos+frameSize, xPos+size-frameSize, yPos+size-frameSize)
        framePaint.color = frameColor
        lightPaint.color = ColorManager.lightDarkColor

        if (Random.nextFloat() <= sillChance) {
            hasSill = true
            val sillMargin: Int = (frameRect.width() * sillMarginRatio).toInt()
            sillRect = Rect(frameRect.left - sillMargin, windowRect.bottom,
                frameRect.right + sillMargin, frameRect.bottom + (sillMargin * 1.5f).toInt())
            sillPaint = Paint()
            sillPaint.color = sillColor
        }

        if (hasS && Random.nextFloat() <= silhouetteChance) {
            hasSilhouette = true
            var silhouetteType: Int = Random.nextInt(0, frameLists.count()-1)
            // Moose is super rare
            if (Random.nextFloat() <= mooseChance) {
                silhouetteType = frameLists.count()-1
            }
            silhouetteFrameList = WindowContents.frameLists[silhouetteType]
            silhouetteFrameDurations = WindowContents.frameDurations[silhouetteType]
            silhouetteIndex = 0
            silhouetteCounter = 0
            silhouetteDuration = Random.nextInt(silhouetteWaitMin, silhouetteWaitMax)

            // Randomly flip horizontally
            if (Random.nextFloat() < 0.5f) {
                silhouetteFlipped = true
            }
        }
    }

    fun drawWindow(canvas: Canvas) {
        canvas.drawRect(frameRect, framePaint)
        canvas.drawRect(windowRect, lightPaint)
        if (hasSill) {
            canvas.drawRect(sillRect, sillPaint)
        }
        if (hasSilhouette) {
            drawSilhouette(canvas, windowRect, silhouetteFrameList[silhouetteIndex], silhouetteFlipped)
        }
    }

    fun getCenter(): Point {
        return Point(frameRect.left + frameRect.width()/2, frameRect.top + frameRect.height()/2)
    }

    fun updatePosition(pX: Float, pY: Float) {
        posX = pX.toInt().toFloat()
        posY = pY.toInt().toFloat()
        val offsetX: Float = (posX - lastPosX).toInt().toFloat()
        val offsetY: Float = (posY - lastPosY).toInt().toFloat()
        lastPosX = posX.toInt().toFloat()
        lastPosY = posY.toInt().toFloat()

        frameRect.offset(offsetX.toInt(), offsetY.toInt())
        windowRect.offset(offsetX.toInt(), offsetY.toInt())
        if (hasSill) {
            sillRect.offset(offsetX.toInt(), offsetY.toInt())
        }
    }

    fun toggle() {
        lightUpCounter = 0
        if (!twoColor) {
            if (litState == 0) {
                litState = 1
                lightUpState = 1
            } else if (litState == 1) {
                litState = 0
                lightUpState = 3
            }
        } else {
            if (litState == 0) {
                litState = 1
                lightUpState = 1
            } else if (litState == 1) {
                litState = 2
                lightUpState = 3
            } else if (litState == 2) {
                litState = 0
                lightUpState = 5
            }
        }
    }

    fun update() {
        if (!twoColor) {
            if (lightUpState == 1) {
                if (lightUpCounter > lightUpDuration) {
                    lightUpState = 2
                } else {
                    lightPaint.color = savedColors[lightUpCounter]
                    lightUpCounter++
                }
            } else if (lightUpState == 3) {
                if (lightUpCounter > lightUpDuration) {
                    lightUpState = 0
                } else {
                    lightPaint.color = savedColors[lightUpDuration - lightUpCounter]
                    lightUpCounter++
                }
            }
        } else {
            if (lightUpState == 1) {
                if (lightUpCounter > lightUpDuration) {
                    lightUpState = 2
                } else {
                    lightPaint.color = savedColors[lightUpCounter]
                    lightUpCounter++
                }
            } else if (lightUpState == 3) {
                if (lightUpCounter > lightUpDuration) {
                    lightUpState = 4
                } else {
                    lightPaint.color = savedColorsB[lightUpCounter]
                    lightUpCounter++
                }
            } else if (lightUpState == 5) {
                if (lightUpCounter > lightUpDuration) {
                    lightUpState = 0
                } else {
                    lightPaint.color = savedColorsC[lightUpCounter]
                    lightUpCounter++
                }
            }
        }

        if (hasSilhouette && silhouetteFrameList.count() > 1) {
            if (silhouetteCounter > silhouetteDuration) {
                silhouetteIndex++
                if (silhouetteIndex >= silhouetteFrameList.count()) {
                    silhouetteIndex = 0
                }
                silhouetteCounter = 0
                if (silhouetteFrameDurations[silhouetteIndex] == -1) {
                    silhouetteDuration = Random.nextInt(silhouetteWaitMin, silhouetteWaitMax)
                } else {
                    silhouetteDuration = silhouetteFrameDurations[silhouetteIndex]
                }
            } else {
                silhouetteCounter++
            }
        }
    }
}