package com.dronami.brightcitylights

import android.graphics.*
import kotlin.random.Random

class Transition {
    private val screenWidth: Float
    private val screenHeight: Float
    var transitionType: Int
    private val numTransitionsTypes: Int = 4
    var transitionActive: Boolean = false
    var transitionIn: Boolean = false

    private var pathStyle: Boolean = true
    private var drawPaths: MutableList<Path> = mutableListOf()
    private var drawRects: MutableList<Rect> = mutableListOf()
    private var transitionPaint: Paint = Paint()
    private var transitionCounter: Int = 0
    private val transitionDuration: Float = 30f
    private var transitionRatio: Float = 0f

    private val transitionColor: Int = Color.rgb(20, 20, 20)

    constructor(sWidth: Int, sHeight: Int, tType: Int) {
        screenWidth = sWidth.toFloat()
        screenHeight = sHeight.toFloat()
        transitionPaint.color = transitionColor
        transitionType = tType
        if (transitionType == -1) {
            randomizeType()
        }

        initTransition(tType)
    }

    fun initTransition(tType: Int) {
        if (tType == -1) {
            randomizeType()
        } else {
            transitionType = tType
        }

        drawPaths.clear()
        drawRects.clear()
        if (transitionType == 0) {
            pathStyle = true
            repeat(2) {
                drawPaths.add(Path())
            }
        } else if (transitionType == 1) {
            pathStyle = true
            repeat(4) {
                drawPaths.add(Path())
            }
        } else if (transitionType == 2) {
            pathStyle = false
            val numRects: Int = 9
            val rectWidth: Int = (screenWidth * 1.1f / numRects).toInt()
            for (r in 0 until numRects) {
                var rectY: Int = screenHeight.toInt()
                if (r % 2 == 0) {
                    rectY = 0
                }
                drawRects.add(Rect(rectWidth * r, rectY, rectWidth + rectWidth * r, rectY))
            }
        } else if (transitionType == 3) {
            pathStyle = false
            val numRects: Int = 19
            val rectHeight: Int = (screenHeight * 1.1f / (numRects)).toInt()
            for (r in 0 until numRects) {
                var rectX: Int = screenWidth.toInt()
                if (r % 2 == 0) {
                    rectX = 0
                }
                drawRects.add(Rect(rectX, rectHeight * r, rectX, rectHeight + rectHeight * r))
            }
        }
    }

    fun randomizeType() {
        transitionType = Random.nextInt(0, numTransitionsTypes)
    }

    fun startTransition(tIn: Boolean) {
        transitionIn = tIn
        transitionActive = true
        transitionCounter = 0

        SoundManager.playSound(SoundManager.Sounds.WHOOSH.value)
    }

    fun updateTransition() {
        if (!transitionActive) {
            return
        }
        if (transitionCounter > transitionDuration) {
            transitionActive = false
        } else {
            if (!transitionIn) {
                transitionRatio = 1.0f - (transitionCounter / transitionDuration)
            } else {
                transitionRatio = transitionCounter / transitionDuration
            }


            if (transitionType == 0 || transitionType == 1) {
                drawPaths[0].rewind()
                drawPaths[0].moveTo(0f, 0f)
                drawPaths[0].lineTo(screenWidth * transitionRatio, 0f)
                drawPaths[0].lineTo(0f, screenHeight * transitionRatio)
                drawPaths[0].lineTo(0f, 0f)
                drawPaths[0].close()

                drawPaths[1].rewind()
                drawPaths[1].moveTo(screenWidth, screenHeight)
                drawPaths[1].lineTo(screenWidth - screenWidth * transitionRatio, screenHeight)
                drawPaths[1].lineTo(screenWidth, screenHeight - screenHeight * transitionRatio)
                drawPaths[1].lineTo(screenWidth, screenHeight)
                drawPaths[1].close()

                if (transitionType == 1) {
                    drawPaths[2].rewind()
                    drawPaths[2].moveTo(screenWidth, 0f)
                    drawPaths[2].lineTo(screenWidth - screenWidth * transitionRatio, 0f)
                    drawPaths[2].lineTo(screenWidth, screenHeight * transitionRatio)
                    drawPaths[2].lineTo(screenWidth, 0f)
                    drawPaths[2].close()

                    drawPaths[3].rewind()
                    drawPaths[3].moveTo(0f, screenHeight)
                    drawPaths[3].lineTo(screenWidth * transitionRatio, screenHeight)
                    drawPaths[3].lineTo(0f, screenHeight - screenHeight * transitionRatio)
                    drawPaths[3].lineTo(0f, screenHeight)
                    drawPaths[3].close()
                }
            } else if (transitionType == 2) {
                for (r in 0 until drawRects.count()) {
                    var rectTop: Int = (screenHeight - (transitionRatio * screenHeight)).toInt()
                    var rectBottom : Int = screenHeight.toInt()
                    if (r % 2 == 0) {
                        rectTop = 0
                        rectBottom = (transitionRatio * screenHeight).toInt()
                    }

                    drawRects[r].set(drawRects[r].left, rectTop, drawRects[r].right, rectBottom)
                }
            } else if (transitionType == 3) {
                for (r in 0 until drawRects.count()) {
                    var rectLeft: Int = (screenWidth - (transitionRatio * screenWidth)).toInt()
                    var rectRight : Int = screenWidth.toInt()
                    if (r % 2 == 0) {
                        rectLeft = 0
                        rectRight = (transitionRatio * screenWidth).toInt()
                    }

                    drawRects[r].set(rectLeft, drawRects[r].top, rectRight, drawRects[r].bottom)
                }
            }

            transitionCounter++
        }
    }

    fun draw(canvas: Canvas) {
        if (pathStyle) {
            for (p in drawPaths) {
                canvas.drawPath(p, transitionPaint)
            }
        } else {
            for (r in drawRects) {
                canvas.drawRect(r, transitionPaint)
            }
        }
    }
}