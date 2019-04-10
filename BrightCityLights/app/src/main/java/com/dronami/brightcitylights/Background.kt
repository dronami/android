package com.dronami.brightcitylights

import android.graphics.*
import kotlin.random.Random

class Background(cIndices: List<Int>) {
    private val colorIndices: List<Int> = cIndices
    private val leftColorsList: List<Int> = ColorManager.getColorList(colorIndices[1])
    private val rightColorsList: List<Int> = ColorManager.getColorList(colorIndices[2])

    lateinit var mainBuilding: Building

    private lateinit var leftBuildingFrontRect: Rect
    private lateinit var leftBuildingRoofRect: Rect
    private lateinit var leftBuildingRoofTopRect: Rect
    private var leftBuildingFrontPaint: Paint = Paint()
    private var leftBuildingRoofPaint : Paint = Paint()
    private var leftBuildingRoofTopPaint: Paint = Paint()

    private lateinit var rightBuildingFrontRect: Rect
    private lateinit var rightBuildingRoofRect: Rect
    private lateinit var rightBuildingRoofTopPath: Path
    private var rightBuildingFrontPaint: Paint = Paint()
    private var rightBuildingRoofPaint : Paint = Paint()
    private var rightBuildingRoofTopPaint: Paint = Paint()

    private lateinit var skyRect: Rect
    private val skyPaint: Paint = Paint()
    private val skyColor: Int = ColorManager.skyColor

    private var drawSidewalk: Boolean = true
    private lateinit var sidewalk: Sidewalk

    fun initBackground(playArea: Rect, mViewWidth: Int, mViewHeight: Int, widthRatio: Float) {
        // Setup paints
        leftBuildingFrontPaint.color = leftColorsList[0]
        leftBuildingRoofPaint.color = leftColorsList[2]
        leftBuildingRoofTopPaint.color = leftColorsList[4]
        rightBuildingFrontPaint.color = rightColorsList[0]
        rightBuildingRoofPaint.color = rightColorsList[2]
        rightBuildingRoofTopPaint.color = rightColorsList[4]

        mainBuilding = Building(colorIndices[0], playArea, mViewWidth, mViewHeight, widthRatio)

        // Randomize left + right building heights
        val leftBuildingTopRatio : Float = 0.3f + 0.6f * Random.nextFloat()
        leftBuildingFrontRect = Rect(0, (mainBuilding.bottomAreaTop - mainBuilding.buildingFrontArea.width() * leftBuildingTopRatio).toInt(),
            mainBuilding.buildingFrontArea.left, mainBuilding.buildingFrontArea.bottom)
        leftBuildingRoofRect = Rect(0, leftBuildingFrontRect.top - mainBuilding.roofHeight,
            mainBuilding.buildingFrontArea.left, leftBuildingFrontRect.top)
        leftBuildingRoofTopRect = Rect(0, leftBuildingRoofRect.top - mainBuilding.roofTopY,
            mainBuilding.buildingFrontArea.left, leftBuildingRoofRect.top)

        val rightBuildingTopRatio : Float = 0.3f + 0.6f * Random.nextFloat()
        rightBuildingFrontRect = Rect(mainBuilding.buildingFrontArea.right, (mainBuilding.bottomAreaTop - mainBuilding.buildingFrontArea.width() * rightBuildingTopRatio).toInt(),
            mViewWidth, mainBuilding.buildingFrontArea.bottom)
        rightBuildingRoofRect = Rect(mainBuilding.buildingFrontArea.right, rightBuildingFrontRect.top - mainBuilding.roofHeight,
            mViewWidth, rightBuildingFrontRect.top)
        rightBuildingRoofTopPath = Path()
        rightBuildingRoofTopPath.moveTo(mainBuilding.buildingFrontArea.right.toFloat(), rightBuildingRoofRect.top.toFloat())
        rightBuildingRoofTopPath.lineTo((mainBuilding.buildingFrontArea.right + mainBuilding.roofTopX).toFloat(), (rightBuildingRoofRect.top - mainBuilding.roofTopY).toFloat())
        rightBuildingRoofTopPath.lineTo((mainBuilding.buildingFrontArea.right + mainBuilding.roofTopX).toFloat(), rightBuildingRoofRect.top.toFloat())
        rightBuildingRoofTopPath.lineTo(mainBuilding.buildingFrontArea.right.toFloat(), rightBuildingRoofRect.top.toFloat())
        rightBuildingRoofTopPath.close()

        // Setup sky
        skyRect = Rect(0, 0, mViewWidth, leftBuildingRoofRect.top)
        skyPaint.setColor(skyColor)

        // Sidewalk
        if (mainBuilding.buildingFrontArea.bottom > mViewHeight) {
            drawSidewalk = false
        } else {
            drawSidewalk = true
            sidewalk = Sidewalk(mainBuilding.buildingFrontArea.bottom, mViewWidth, mViewHeight, widthRatio)
        }
    }

    fun draw(canvas: Canvas) {
        if (drawSidewalk) {
            sidewalk.draw(canvas)
        }

        canvas.drawRect(skyRect, skyPaint)
        canvas.drawRect(leftBuildingFrontRect, leftBuildingFrontPaint)
        canvas.drawRect(leftBuildingRoofRect, leftBuildingRoofPaint)
        canvas.drawRect(leftBuildingRoofTopRect, leftBuildingRoofTopPaint)

        mainBuilding.draw(canvas)

        canvas.drawRect(rightBuildingFrontRect, rightBuildingFrontPaint)
        canvas.drawRect(rightBuildingRoofRect, rightBuildingRoofPaint)
        canvas.drawPath(rightBuildingRoofTopPath, rightBuildingRoofTopPaint)
    }
}

class Building {
    private lateinit var colorsList: List<Int>

    var buildingFrontArea: Rect
    var buildingSideArea: Path
    private var buildingFrontPaint: Paint = Paint()
    private var buildingSidePaint: Paint = Paint()

    var buildingX: Float = 0f
    var buildingY: Float = 0f
    var lastBuildingX: Float = 0f
    var lastBuildingY: Float = 0f

    var bottomAreaTop: Int = 0
    private val buildingBottomRatio: Float = 0.4f
    private var buildingBottomOffset: Float = 0f
    private val playAreaBottomMarginRatio = 0.1f
    private val roofMarginBase: Int = 20
    private val roofHeightBase: Int = 70
    var roofHeight: Int = 0
    var roofTopX: Int = 0
    var roofTopY: Int = 0
    private val roofTopXBase: Int = 100
    private val roofTopYBase: Int = 60

    private var roofFrontArea: Rect
    private var roofTopArea: Path
    private var roofTopSideArea: Path
    private var roofPaint: Paint = Paint()
    private var roofTopPaint: Paint = Paint()
    private var roofSidePaint: Paint = Paint()

    private var doorFrameRect: Rect
    private var doorLeftRect: Rect
    private var doorRightRect: Rect
    private var doorLeftHandleRect: Rect
    private var doorRightHandleRect: Rect
    private val doorHandleHeightRatio: Float = 0.1f
    private val doorHandleWidthRatio: Float = 0.25f
    private val doorHandleMarginRatio: Float = 0.05f
    private val doorWidthRatio: Float = 0.38f
    private val doorFrameMarginRatio: Float = 0.1f
    private var doorPaint: Paint = Paint()

    private var hasAwnings: Boolean = false
    private lateinit var awningBitmap: Bitmap
    private var awningLeftX: Float = 0f
    private var awningRightX: Float = 0f
    private var awningOffsetRatio: Float = 0.293f
    private var awningY: Float = 0f

    constructor(colorIndex: Int, playArea: Rect, mViewWidth: Int, mViewHeight: Int, widthRatio: Float, bX: Float = 0f, bY: Float = 0f) {
        colorsList = ColorManager.getColorList(colorIndex)
        buildingX = bX
        buildingY = bY
        buildingFrontPaint.color = colorsList[0]
        buildingSidePaint.color = colorsList[1]
        roofPaint.color = colorsList[2]
        roofSidePaint.color = colorsList[3]
        roofTopPaint.color = colorsList[4]
        doorPaint.color = colorsList[4]

        // Setup background BG
        bottomAreaTop = playArea.bottom
        buildingFrontArea = Rect(playArea)
        buildingBottomOffset = buildingBottomRatio * playArea.height()
        buildingFrontArea.bottom = (playArea.bottom + buildingBottomOffset).toInt()
        val roofMargin = (roofMarginBase * widthRatio).toInt()
        roofHeight = (roofHeightBase * widthRatio).toInt()
        roofTopX = (roofTopXBase * widthRatio).toInt()
        roofTopY = (roofTopYBase * widthRatio).toInt()
        val playAreaBottomMargin = (playArea.bottom + (playArea.height() * playAreaBottomMarginRatio)).toInt()

        // Setup building draw rects
        roofFrontArea = Rect(roofMargin, buildingFrontArea.top - roofHeight,
            mViewWidth-roofMargin, buildingFrontArea.top)
        buildingSideArea = Path()
        buildingSideArea.moveTo(buildingFrontArea.right.toFloat(), buildingFrontArea.top.toFloat())
        buildingSideArea.lineTo(buildingFrontArea.right.toFloat() + roofTopX, buildingFrontArea.top.toFloat() - roofTopY)
        buildingSideArea.lineTo(buildingFrontArea.right.toFloat() + roofTopX, buildingFrontArea.bottom.toFloat() - roofTopY)
        buildingSideArea.lineTo(buildingFrontArea.right.toFloat(), buildingFrontArea.bottom.toFloat())
        buildingSideArea.close()

        // Roof
        roofTopArea = Path()
        roofTopArea.moveTo(roofFrontArea.left.toFloat(), roofFrontArea.top.toFloat())
        roofTopArea.lineTo(roofFrontArea.right.toFloat(), roofFrontArea.top.toFloat())
        roofTopArea.lineTo(roofFrontArea.right.toFloat() + roofTopX, roofFrontArea.top.toFloat() - roofTopY)
        roofTopArea.lineTo(roofFrontArea.left.toFloat() + roofTopX, roofFrontArea.top.toFloat() - roofTopY)
        roofTopArea.close()

        roofTopSideArea = Path()
        roofTopSideArea.moveTo(roofFrontArea.right.toFloat(), roofFrontArea.top.toFloat())
        roofTopSideArea.lineTo(roofFrontArea.right.toFloat() + roofTopX, roofFrontArea.top.toFloat() - roofTopY)
        roofTopSideArea.lineTo(roofFrontArea.right.toFloat() + roofTopX, roofFrontArea.bottom.toFloat() - roofTopY)
        roofTopSideArea.lineTo(roofFrontArea.right.toFloat(), roofFrontArea.bottom.toFloat())
        roofTopSideArea.close()

        // Door
        val doorWidth: Int = (buildingFrontArea.width() * doorWidthRatio).toInt()
        val doorMargin: Int = ((buildingFrontArea.width() - doorWidth) / 2.0f).toInt()
        doorFrameRect = Rect(buildingFrontArea.left + doorMargin, playAreaBottomMargin,
            buildingFrontArea.right - doorMargin, buildingFrontArea.bottom)
        val doorFrameMargin: Int = (doorFrameRect.width() * doorFrameMarginRatio).toInt()
        doorLeftRect = Rect((doorFrameRect.left + doorFrameMargin), (doorFrameRect.top + doorFrameMargin),
            (doorFrameRect.left + doorFrameRect.width()/2.1f).toInt(), doorFrameRect.bottom)
        doorRightRect = Rect((doorFrameRect.right - doorFrameRect.width()/2.1f).toInt(), (doorFrameRect.top + doorFrameMargin),
            (doorFrameRect.right - doorFrameMargin), doorFrameRect.bottom)
        val doorHandleWidth: Int = (doorWidth * doorHandleWidthRatio).toInt()
        val doorHandleHeight: Int = (doorLeftRect.height() * doorHandleHeightRatio).toInt()
        val doorHandleMargin: Int = (doorWidth * doorHandleMarginRatio).toInt()
        doorLeftHandleRect = Rect(doorLeftRect.right - doorHandleMargin - doorHandleWidth, doorLeftRect.top + (doorLeftRect.height()/2.0f).toInt(),
            doorLeftRect.right - doorHandleMargin, doorLeftRect.top + (doorLeftRect.height()/2.0f).toInt() + doorHandleHeight)
        doorRightHandleRect = Rect(doorRightRect.left + doorHandleMargin, doorLeftRect.top + (doorLeftRect.height()/2.0f).toInt(),
            doorRightRect.left + doorHandleMargin + doorHandleWidth, doorLeftRect.top + (doorLeftRect.height()/2.0f).toInt() + doorHandleHeight)

        updatePosition(buildingX, buildingY)
    }

    fun initAwnings(aBitmap: Bitmap) {
        hasAwnings = true
        awningBitmap = aBitmap
        awningLeftX = buildingFrontArea.left + (buildingFrontArea.width() / 10.0f) - (awningBitmap.width * awningOffsetRatio)
        awningRightX = buildingFrontArea.right - awningBitmap.width - buildingFrontArea.width() / 10.0f
        awningY = bottomAreaTop.toFloat()
    }

    fun updatePosition(posX: Float, posY: Float) {
        buildingX = posX.toInt().toFloat()
        buildingY = posY.toInt().toFloat()
        val offsetX: Float = (buildingX - lastBuildingX).toInt().toFloat()
        val offsetY: Float = (buildingY - lastBuildingY).toInt().toFloat()
        lastBuildingX = buildingX.toInt().toFloat()
        lastBuildingY = buildingY.toInt().toFloat()

        buildingFrontArea.offset(offsetX.toInt(), offsetY.toInt())
        buildingSideArea.offset(offsetX, offsetY)
        roofFrontArea.offset(offsetX.toInt(), offsetY.toInt())
        roofTopSideArea.offset(offsetX, offsetY)
        roofTopArea.offset(offsetX, offsetY)
        doorFrameRect.offset(offsetX.toInt(), offsetY.toInt())
        doorLeftRect.offset(offsetX.toInt(), offsetY.toInt())
        doorRightRect.offset(offsetX.toInt(), offsetY.toInt())
        doorLeftHandleRect.offset(offsetX.toInt(), offsetY.toInt())
        doorRightHandleRect.offset(offsetX.toInt(), offsetY.toInt())

        awningLeftX += offsetX
        awningRightX += offsetX
        awningY += offsetY
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(buildingFrontArea, buildingFrontPaint)
        canvas.drawPath(buildingSideArea, buildingSidePaint)
        canvas.drawRect(roofFrontArea, roofPaint)
        canvas.drawPath(roofTopArea, roofTopPaint)
        canvas.drawPath(roofTopSideArea, roofSidePaint)

        canvas.drawRect(doorFrameRect, roofSidePaint)
        canvas.drawRect(doorLeftRect, doorPaint)
        canvas.drawRect(doorRightRect, doorPaint)
        canvas.drawRect(doorLeftHandleRect, roofSidePaint)
        canvas.drawRect(doorRightHandleRect, roofSidePaint)

        if (hasAwnings) {
            canvas.drawBitmap(awningBitmap, awningLeftX, awningY, null)
            canvas.drawBitmap(awningBitmap, awningRightX, awningY, null)
        }
    }
}

class Sidewalk {
    private lateinit var sidewalkRect: Rect
    private val sidewalkColor: Int = Color.rgb(100, 100, 100)
    private val sidewalkLineColor: Int = Color.rgb(40, 40, 40)
    private val sidewalkLineWidthRatio = 0.15f
    private val numSidewalkLines = 4
    private lateinit var sidewalkLineStarts: MutableList<Pair<Float, Float>>;
    private lateinit var sidewalkLineEnds: MutableList<Pair<Float, Float>>;
    private var sidewalkPaint: Paint = Paint()
    private var sidewalkLinePaint: Paint = Paint()

    constructor(buildingBottom: Int, mViewWidth: Int, mViewHeight: Int, widthRatio: Float) {
        sidewalkPaint.color = sidewalkColor
        sidewalkLinePaint.color = sidewalkLineColor

        sidewalkRect = Rect(0, buildingBottom, mViewWidth, mViewHeight)
        val sidewalkLineOffset: Float = mViewWidth / numSidewalkLines.toFloat()
        val sidewalkLineWidth: Float = mViewWidth * sidewalkLineWidthRatio
        sidewalkLineStarts = mutableListOf()
        sidewalkLineEnds = mutableListOf()
        for (l in 0 until numSidewalkLines) {
            sidewalkLineStarts.add(
                (Pair(
                    sidewalkRect.right - l * sidewalkLineOffset,
                    sidewalkRect.top.toFloat()
                ))
            )
            sidewalkLineEnds.add(
                (Pair(
                    sidewalkRect.right - (l * sidewalkLineOffset) - sidewalkLineWidth,
                    sidewalkRect.bottom.toFloat()
                ))
            )
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawRect(sidewalkRect, sidewalkPaint)
        for (l in 0 until numSidewalkLines) {
            canvas.drawLine(sidewalkLineStarts[l].first, sidewalkLineStarts[l].second,
                sidewalkLineEnds[l].first, sidewalkLineEnds[l].second, sidewalkLinePaint)
        }
    }
}