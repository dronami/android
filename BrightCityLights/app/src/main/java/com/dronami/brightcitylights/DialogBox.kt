package com.dronami.brightcitylights

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import kotlin.math.max

class DialogBox {
    private val boxWidth: Int
    private var boxHeight: Int = 0
    private val screenWidth: Int
    private val screenHeight: Int
    private var boxBitmaps: MutableList<Bitmap>
    private val buttonBitmaps: List<Bitmap>
    private val savedMidBitmap: Bitmap
    private lateinit var headerText: String
    private var isYesNo: Boolean = false
    private var isSelection: Boolean = false
    var dialogActive: Boolean = false

    private var radialSelection: Int = 0
    private var radialSelectionLast: Int = -1
    private var radialX: Float = 0f
    private var radialYOffset: Float = 0f
    private val radialBitmap: Bitmap
    private val radialMarginRatio: Float = 0.12f
    private var radialMargin: Float = 0f
    private var radialRects: MutableList<Rect> = mutableListOf()
    private var radialTouchRects: MutableList<Rect> = mutableListOf()
    private var radialPaintCurrent: Paint = Paint()
    private var radialPaintEmpty: Paint = Paint()

    private var checkboxSelections: MutableList<Boolean> = mutableListOf()
    private var numCheckboxTexts: Int = 0
    private var checkboxBitmap: Bitmap
    private var checkboxRects: MutableList<Rect> = mutableListOf()

    private var lastBoxPosX: Float = 0f
    private var lastBoxPosY: Float = 0f
    private var boxPosX: Float = 0f
    private var boxPosY: Float = 0f
    private var headerX: Float = 0f
    private var headerY: Float = 0f

    private var bodyTextLines: MutableList<String> = mutableListOf()
    private var bodyTextXs: MutableList<Int> = mutableListOf()
    private var bodyTextYs: MutableList<Int> = mutableListOf()
    private val bodyTextYMarginRatio: Float = 0.1f
    private val bodyTextXMarginRatio: Float = 0.025f
    private val bodyTextLineHeightRatio: Float = 0.11f
    private var shadowOffset: Int = 0
    private val shadowOffsetRatio: Float = 0.015f

    private var okButtonDown: Boolean = false
    private var noButtonDown: Boolean = false

    private lateinit var okButtonRect: Rect
    private lateinit var noButtonRect: Rect

    private val dialogBoxSize: Float = 1.4f
    private var dialogFont: Typeface
    private var headerPaint: Paint = Paint()
    private var shadowPaint: Paint = Paint()

    private var centerX: Float = 0f
    private var leftX: Float = 0f
    private var rightX: Float = 0f
    private var topY: Float = 0f
    private var centerY: Float = 0f
    private var bottomY: Float = 0f
    private var boxStatus: Int = 0
    private var transitionStartX: Float = 0f
    private var transitionStartY: Float = 0f
    private var transitionEndX: Float = 0f
    private var transitionEndY: Float = 0f

    private val transitionDuration: Float = 30f
    private var transitionCounter: Int = 0
    private var horizontalTransition: Boolean = true

    private val darkShadowColor: Int = Color.argb(255,40,40,40)
    private val lightShadowColor: Int = Color.argb(255,60,60,60)

    constructor(context: Context, widthRatio: Float, sWidth: Int, sHeight: Int) {
        val dialogHeader: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.menutop)
        val dialogHeaderScaled: Bitmap = BitmapScaler.scaleBitmap(dialogHeader, widthRatio, dialogBoxSize)

        val dialogMid: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.menumid)
        val dialogMidScaled: Bitmap = BitmapScaler.scaleBitmap(dialogMid, widthRatio, dialogBoxSize)

        val dialogBottom: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.menubottom)
        val dialogBottomScaled: Bitmap = BitmapScaler.scaleBitmap(dialogBottom, widthRatio, dialogBoxSize)

        val okUp: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.button_ok_up)
        val okDown: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.button_ok_down)
        val noUp: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.button_no_up)
        val noDown: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.button_no_down)

        val okUpScaled: Bitmap = BitmapScaler.scaleBitmap(okUp, widthRatio, dialogBoxSize)
        val okDownScaled: Bitmap = BitmapScaler.scaleBitmap(okDown, widthRatio, dialogBoxSize)
        val noUpScaled: Bitmap = BitmapScaler.scaleBitmap(noUp, widthRatio, dialogBoxSize)
        val noDownScaled: Bitmap = BitmapScaler.scaleBitmap(noDown, widthRatio, dialogBoxSize)

        boxBitmaps = mutableListOf(dialogHeaderScaled, dialogMidScaled, dialogBottomScaled)
        savedMidBitmap = boxBitmaps[1]
        buttonBitmaps = mutableListOf(okUpScaled, okDownScaled, noUpScaled, noDownScaled)
        screenWidth = sWidth
        screenHeight = sHeight
        boxWidth = boxBitmaps[0].width

        val rBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.radialbox)
        radialBitmap = BitmapScaler.scaleBitmap(rBitmap, widthRatio, dialogBoxSize)
        radialMargin = radialMarginRatio * boxWidth

        val cBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.checkbox)
        checkboxBitmap = BitmapScaler.scaleBitmap(cBitmap, widthRatio, dialogBoxSize)

        headerX = (boxWidth * 0.05f)
        headerY = (boxWidth * 0.12f)

        dialogFont = Typeface.createFromAsset(context.assets, "futur.otf")

        headerPaint.setTypeface(dialogFont)
        headerPaint.setARGB(255, 240, 240, 240)
        headerPaint.textSize = boxWidth / 11f
        headerPaint.flags = Paint.ANTI_ALIAS_FLAG

        shadowPaint.setTypeface(dialogFont)
        shadowPaint.textSize = boxWidth / 11f
        shadowPaint.flags = Paint.ANTI_ALIAS_FLAG
    }

    fun initDialog(hText: String, bTexts: List<String>, yesNo: Boolean) {
        lastBoxPosX = 0f
        lastBoxPosY = 0f
        boxPosX = 0f
        boxPosY = 0f

        headerText = hText
        bodyTextLines = bTexts.toMutableList()
        isYesNo = yesNo
        isSelection = false
        dialogActive = false

        setupBodyText()

        boxHeight = boxBitmaps[0].height + boxBitmaps[1].height + boxBitmaps[2].height

        setupButtons()

        centerX = screenWidth/2.0f - boxWidth/2.0f
        leftX = 0f - boxWidth
        rightX = screenWidth.toFloat()
        centerY = screenHeight/2.0f - boxHeight/2.0f
        topY = 0f - boxHeight
        bottomY = screenHeight.toFloat()
    }

    fun initSelectionDialog(hText: String, bTexts: List<String>, cTexts: List<String>, yesNo: Boolean, selection: Int = 0) {
        lastBoxPosX = 0f
        lastBoxPosY = 0f
        boxPosX = 0f
        boxPosY = 0f

        numCheckboxTexts = cTexts.count()
        headerText = hText
        bodyTextLines = bTexts.toMutableList()
        for (text in cTexts) {
            bodyTextLines.add(text)
        }
        isYesNo = yesNo
        isSelection = true
        radialRects.clear()
        radialTouchRects.clear()
        checkboxRects.clear()

        setupBodyText(2.0f, (radialBitmap.width + radialMargin).toInt())

        // Setup radial buttons
        radialSelection = selection
        radialSelectionLast = -1
        radialX = boxPosX + radialMargin
        radialYOffset = boxPosY - (radialBitmap.height/1.5f)

        var r: Int = 0
        while (r < bodyTextYs.count() - numCheckboxTexts) {
            radialRects.add(Rect((radialX + radialBitmap.width*0.18f).toInt(), (bodyTextYs[r] + radialYOffset + radialBitmap.height*0.18f).toInt(),
            (radialX + radialBitmap.width * 0.82f).toInt(), (bodyTextYs[r] + radialYOffset + radialBitmap.height * 0.82f).toInt()))
            radialTouchRects.add(Rect((radialX.toInt()), (bodyTextYs[r] + radialYOffset).toInt(),
                (radialX + radialBitmap.width).toInt(), (bodyTextYs[r] + radialYOffset + radialBitmap.height).toInt()))
            r++
        }
        while (r < bodyTextYs.count()) {
            checkboxRects.add(Rect((radialX + radialBitmap.width*0.18f).toInt(), (bodyTextYs[r] + radialYOffset + radialBitmap.height*0.18f).toInt(),
                (radialX + radialBitmap.width * 0.82f).toInt(), (bodyTextYs[r] + radialYOffset + radialBitmap.height * 0.82f).toInt()))
            checkboxSelections.add(false)
            r++
        }
        radialPaintCurrent.color = ColorManager.lightBrightColor
        radialPaintEmpty.color = ColorManager.lightDarkColor

        boxHeight = boxBitmaps[0].height + boxBitmaps[1].height + boxBitmaps[2].height

        setupButtons()

        centerX = screenWidth/2.0f - boxWidth/2.0f
        leftX = 0f - boxWidth
        rightX = screenWidth.toFloat()
        centerY = screenHeight/2.0f - boxHeight/2.0f
        topY = 0f - boxHeight
        bottomY = screenHeight.toFloat()
    }

    private fun setupBodyText(lineHeightScale: Float = 1.0f, textPosX: Int = -1) {
        val bodyTextXMargin: Int = (boxWidth * bodyTextXMarginRatio).toInt()
        val bodyTextYMargin: Int = (boxWidth * bodyTextYMarginRatio).toInt()
        val bodyTextLineHeight: Int = (boxWidth * bodyTextLineHeightRatio * lineHeightScale).toInt()
        shadowOffset = (boxWidth * shadowOffsetRatio).toInt()
        bodyTextXs.clear()
        bodyTextYs.clear()

        for (l in 0 until bodyTextLines.count()) {
            if (textPosX == -1) {
                bodyTextXs.add(
                    (boxWidth / 2.0f).toInt() - bodyTextXMargin
                            - (headerPaint.measureText(bodyTextLines[l]) / 2.0f).toInt()
                )
            } else {
                bodyTextXs.add((textPosX * 1.2f).toInt())
            }
            bodyTextYs.add(boxBitmaps[0].height + bodyTextYMargin + bodyTextLineHeight * l)
        }

        if (bodyTextLines.count() > 0) {
            boxBitmaps[1] = Bitmap.createScaledBitmap(savedMidBitmap, boxWidth,
                savedMidBitmap.height + bodyTextLineHeight * (max(bodyTextLines.count()-1, 0)) + (bodyTextYMargin * 2.6f).toInt(), true)
        } else {
            boxBitmaps[1] = savedMidBitmap
        }
    }

    private fun setupButtons() {
        val buttonTop: Int = (boxHeight - (boxWidth * 0.238f)).toInt()
        var okX: Int = ((boxBitmaps[0].width / 3.25f) - (buttonBitmaps[0].width / 2.0f)).toInt()
        if (isYesNo) {
            val noX: Int = (boxBitmaps[0].width - (boxBitmaps[0].width / 3.25f) - (buttonBitmaps[0].width / 2.0f)).toInt()
            okButtonRect = Rect(okX, buttonTop, okX + buttonBitmaps[0].width,
                buttonTop + buttonBitmaps[0].height)
            noButtonRect = Rect(noX, buttonTop, noX + buttonBitmaps[0].width,
                buttonTop + buttonBitmaps[0].height)
        } else {
            okX = ((boxBitmaps[0].width / 2.0f) - (buttonBitmaps[0].width / 2.0f)).toInt()
            okButtonRect = Rect(okX, buttonTop, okX + buttonBitmaps[0].width, buttonTop + buttonBitmaps[0].height)
        }
    }

    fun startTransition(transitionIn: Boolean, horizontal: Boolean = true) {
        dialogActive = true
        if (transitionIn) {
            lastBoxPosX = 0f
            lastBoxPosY = 0f
            boxPosX = 0f
            boxPosY = 0f
        }

        horizontalTransition = horizontal
        if (transitionIn) {
            boxStatus = 1
            transitionEndX = centerX
            transitionEndY = centerY
            if (horizontal) {
                transitionStartX = leftX
                transitionStartY = centerY
            } else {
                transitionStartX = centerX
                transitionStartY = topY
            }
        } else {
            boxStatus = 3
            transitionStartX = centerX
            transitionStartY = centerY
            if (horizontal) {
                transitionEndX = rightX
                transitionEndY = centerY
            } else {
                transitionEndX = centerX
                transitionEndY = bottomY
            }
        }

        transitionCounter = 0
    }

    fun updateDialog() {
        if (boxStatus == 1 || boxStatus == 3) {
            if (transitionCounter > transitionDuration) {
                if (boxStatus == 1) {
                     boxStatus = 2
                } else {
                    boxStatus = 0
                    dialogActive = false
                }
            } else {
                val transitionRatio: Float = transitionCounter / transitionDuration
                updatePosition(transitionStartX + (transitionEndX - transitionStartX) * transitionRatio,
                    transitionStartY + (transitionEndY - transitionStartY) * transitionRatio)

                transitionCounter++
            }
        }
    }

    private fun updatePosition(posX: Float, posY: Float) {
        lastBoxPosX = boxPosX.toInt().toFloat()
        lastBoxPosY = boxPosY.toInt().toFloat()
        boxPosX = posX.toInt().toFloat()
        boxPosY = posY.toInt().toFloat()
        okButtonRect.offset((boxPosX - lastBoxPosX).toInt(), (boxPosY - lastBoxPosY).toInt())
        for (r in radialRects) {
            r.offset((boxPosX - lastBoxPosX).toInt(), (boxPosY - lastBoxPosY).toInt())
        }
        for (t in radialTouchRects) {
            t.offset((boxPosX - lastBoxPosX).toInt(), (boxPosY - lastBoxPosY).toInt())
        }
        for (c in checkboxRects) {
            c.offset((boxPosX - lastBoxPosX).toInt(), (boxPosY - lastBoxPosY).toInt())
        }
        if (isYesNo) {
            noButtonRect.offset((boxPosX - lastBoxPosX).toInt(), (boxPosY - lastBoxPosY).toInt())
        }

    }

    fun setSelection(sel: Int) {
        if (sel != radialSelection) {
            SoundManager.playSound(SoundManager.Sounds.SELECT.value)
            radialSelectionLast = radialSelection
            radialSelection = sel
        }
    }

    fun setCheckbox(index: Int, value: Boolean) {
        SoundManager.playSound(SoundManager.Sounds.SELECT.value)
        checkboxSelections[index] = value
    }

    fun handleTouchEvent(event: MotionEvent?): Int {
        if (event != null && boxStatus == 2) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                for (r in 0 until radialTouchRects.count()) {
                    if (r != radialSelection && radialTouchRects[r].contains(event.x.toInt(), event.y.toInt())) {
                        setSelection(r)
                    }
                }
                for (c in 0 until checkboxRects.count()) {
                    if (checkboxRects[c].contains(event.x.toInt(), event.y.toInt())) {
                        setCheckbox(c, !checkboxSelections[c])
                    }
                }
            }

            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {

                if(!okButtonDown && okButtonRect.contains(event.x.toInt(), event.y.toInt())) {
                    okButtonDown = true
                    SoundManager.playSound(SoundManager.Sounds.SWITCH2.value)
                } else if (okButtonDown && !okButtonRect.contains(event.x.toInt(), event.y.toInt())) {
                    okButtonDown = false
                    SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                }

                if (isYesNo) {
                    if(!noButtonDown && noButtonRect.contains(event.x.toInt(), event.y.toInt())) {
                        noButtonDown = true
                        SoundManager.playSound(SoundManager.Sounds.SWITCH2.value)
                    } else if (noButtonDown && !noButtonRect.contains(event.x.toInt(), event.y.toInt())) {
                        noButtonDown = false
                        SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    }
                }
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (okButtonRect.contains(event.x.toInt(), event.y.toInt())) {
                    okButtonDown = false
                    SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    return 1
                } else if (isYesNo && noButtonRect.contains(event.x.toInt(), event.y.toInt())) {
                    noButtonDown = false
                    SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    return -1
                }
            }
        }

        return 0
    }

    fun getRadialSelection(): Int {
        return radialSelection
    }

    fun getCheckboxValues(): MutableList<Boolean> {
        return checkboxSelections
    }

    fun draw (canvas: Canvas) {
        if (boxStatus == 0 || !dialogActive) {
            return
        }
        canvas.drawBitmap(boxBitmaps[0], boxPosX, boxPosY, null)
        canvas.drawBitmap(boxBitmaps[1], boxPosX, boxPosY + boxBitmaps[0].height, null)
        canvas.drawBitmap(boxBitmaps[2], boxPosX, boxPosY + boxBitmaps[0].height + boxBitmaps[1].height, null)

        if (okButtonDown) {
            canvas.drawBitmap(buttonBitmaps[1], okButtonRect.left.toFloat(), okButtonRect.top.toFloat(), null)
        } else {
            canvas.drawBitmap(buttonBitmaps[0], okButtonRect.left.toFloat(), okButtonRect.top.toFloat(), null)
        }
        if (isYesNo) {
            if (noButtonDown) {
                canvas.drawBitmap(buttonBitmaps[3], noButtonRect.left.toFloat(), noButtonRect.top.toFloat(), null)
            } else {
                canvas.drawBitmap(buttonBitmaps[2], noButtonRect.left.toFloat(), noButtonRect.top.toFloat(), null)
            }
        }

        shadowPaint.color = darkShadowColor
        canvas.drawText(headerText, boxPosX + headerX + shadowOffset, boxPosY + headerY + shadowOffset, shadowPaint)
        canvas.drawText(headerText, boxPosX + headerX, boxPosY + headerY, headerPaint)
        shadowPaint.color = lightShadowColor
        var t: Int = 0
        while (t < bodyTextLines.count() - numCheckboxTexts) {
            canvas.drawText(bodyTextLines[t], boxPosX + bodyTextXs[t].toFloat() + shadowOffset,
                boxPosY + bodyTextYs[t].toFloat() + shadowOffset, shadowPaint)
            canvas.drawText(bodyTextLines[t], boxPosX + bodyTextXs[t].toFloat(), boxPosY + bodyTextYs[t].toFloat(), headerPaint)
            if (isSelection) {
                if (t == radialSelection) {
                    canvas.drawRect(radialRects[t], radialPaintCurrent)
                } else if (t == radialSelectionLast) {
                    canvas.drawRect(radialRects[t], radialPaintEmpty)
                } else {
                    canvas.drawRect(radialRects[t], radialPaintEmpty)
                }
                canvas.drawBitmap(radialBitmap, boxPosX + radialX, boxPosY + bodyTextYs[t] + radialYOffset, null)
            }
            t++
        }
        while (t < bodyTextLines.count()) {
            canvas.drawText(bodyTextLines[t], boxPosX + bodyTextXs[t].toFloat() + shadowOffset,
                boxPosY + bodyTextYs[t].toFloat() + shadowOffset, shadowPaint)
            canvas.drawText(bodyTextLines[t], boxPosX + bodyTextXs[t].toFloat(), boxPosY + bodyTextYs[t].toFloat(), headerPaint)
            if (isSelection) {
                val cbIndex = t - (bodyTextLines.count()-numCheckboxTexts)
                if (checkboxSelections[cbIndex]) {
                    canvas.drawRect(checkboxRects[cbIndex], radialPaintCurrent)
                } else {
                    canvas.drawRect(checkboxRects[cbIndex], radialPaintEmpty)
                }
                canvas.drawBitmap(checkboxBitmap, boxPosX + radialX, boxPosY + bodyTextYs[t] + radialYOffset, null)
            }
            t++
        }
    }
}