package com.dronami.brightcitylights

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.AttributeSet
import android.view.*
import java.text.SimpleDateFormat
import java.util.*

class MissionsActivity : AppCompatActivity() {

    lateinit var missionsScreen: MissionsScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_missions)

        missionsScreen = findViewById(R.id.missionsmain)
        var transitionType: Int = -1
        var missionNumber: Int = -1
        var missionBeat: Boolean = false
        var lastSet: Int = 0
        if (intent != null) {
            transitionType = intent.getIntExtra("transitionType", 0)
            missionNumber = intent.getIntExtra("missionNumber", -1)
            missionBeat = intent.getBooleanExtra("missionBeat", false)
            lastSet = intent.getIntExtra("lastSet", 0)
        }
        missionsScreen.initMissionsScreen(this, transitionType, missionNumber, missionBeat, lastSet)
    }

    override fun onPause() {
        super.onPause()
        missionsScreen.pause()
    }

    override fun onResume() {
        super.onResume()
        missionsScreen.resume()
    }
}

class MissionsScreen : SurfaceView, Runnable {
    private lateinit var mContext: Context
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mPath: Path
    private lateinit var mGameThread: Thread
    private var mRunning: Boolean = false
    private lateinit var mCanvas: Canvas
    private var missionsStatus: Int = 0
    private var transitionType = 0

    private var missionsList: MutableList<Mission> = mutableListOf()
    private lateinit var selectedMission: Mission
    private var lastMission: Int = 0

    private lateinit var missionSignBitmap: Bitmap
    private val missionSignOffsetX: Int = 40
    private val missionSignOffsetY: Int = -30
    private var missionSignXs: MutableList<Float> = mutableListOf()
    private var missionSignY: Float = 0f
    private var missionSignTextXOffset: Float = 0f
    private var missionSignTextY: Float = 0f
    private var missionSignTexts: List<String> = listOf("4x4", "5x5", "2 Color", "6x6")
    private var missionSignPaint: Paint = Paint()

    private val numBuildings: Int = 4
    private var curBuilding: Int = 0
    private var currentX: Float = 0f
    private var lastX: Float = 0f
    private var moveCounter: Int = 0
    private val moveDuration: Float = 30f
    private var moveStart: Float = 0f
    private var moveEnd: Float = 0f
    private var moveRatio: Float = 0f

    private var mViewHeight: Int = 0
    private var mViewWidth: Int = 0
    private var baseWidth: Float = 1080f
    private val sideMarginBase: Int = 60

    private lateinit var playArea: Rect
    private var windowSize: Int = 0
    private var allWindowsList: MutableList<MutableList<CityWindow>> = mutableListOf()
    private var windowMarginPercent: Float = 0.04f
    private lateinit var lastWindow: CityWindow
    private val numRows: Int = 5
    private val skyColor: Int = ColorManager.skyColor

    private var buildings: MutableList<Building> = mutableListOf()
    private var buildingColorLists: MutableList<List<Int>> = mutableListOf()
    private lateinit var sidewalk: Sidewalk
    private var buildingOffset: Float = 0f

    private lateinit var signLeft: Sign
    private lateinit var signRight: Sign
    private lateinit var signStop: Sign
    private var signMoving: Int = 0
    private var signStartY: Float = 0f
    private var signEndY: Float = 0f

    private lateinit var dialogBox: DialogBox
    private var dialogReturn: Int = 0

    private lateinit var transition: Transition

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    fun initMissionsScreen(_context: Context, tType: Int, missionNumber: Int, missionBeat: Boolean, lastSet: Int) {
        mContext = _context
        mSurfaceHolder = holder
        mPath = Path()

        transitionType = tType

        val dbHelper: PuzzlesDBHelper = PuzzlesDBHelper(mContext)
        val db = dbHelper.readableDatabase

        val selectQuery = "SELECT ${MissionsContract.MissionEntry.MOVE_LIMIT}, ${MissionsContract.MissionEntry.TIME_LIMIT}," +
                "${MissionsContract.MissionEntry.COMPLETED}, ${PuzzlesContract.PuzzleEntry.PUZZLE_SIZE}," +
                "${PuzzlesContract.PuzzleEntry.PUZZLE_TYPE}, ${PuzzlesContract.PuzzleEntry.PUZZLE_MAP}, ${PuzzlesContract.PuzzleEntry.PUZZLE_MAP2} " +
                "FROM ${MissionsContract.MissionEntry.TABLE_NAME} " +
                "INNER JOIN ${PuzzlesContract.PuzzleEntry.TABLE_NAME} " +
                "ON ${MissionsContract.MissionEntry.TABLE_NAME}.${MissionsContract.MissionEntry.PUZZLE_ID} = " +
                "${PuzzlesContract.PuzzleEntry.TABLE_NAME}.${BaseColumns._ID}"
        val cursor = db.rawQuery(
            selectQuery, null
        )

        var missionIndex: Int = 1
        var isComplete: Boolean = false
        with(cursor){
            while (moveToNext()) {
                isComplete = getInt(2) != 0
                missionsList.add(Mission(missionIndex, getInt(3), getInt(4),
                    getLong(5), getLong(6), getInt(0), getLong(1), isComplete))
                missionIndex++
            }
        }

        lastMission = missionNumber
        if (lastMission > -1 && missionBeat) {
            missionsList[missionNumber-1].isComplete = true

            val dbHelper: PuzzlesDBHelper = PuzzlesDBHelper(mContext)
            val db = dbHelper.writableDatabase

            val updateQuery = "UPDATE ${MissionsContract.MissionEntry.TABLE_NAME} SET ${MissionsContract.MissionEntry.COMPLETED} = 1 " +
                    "WHERE ${BaseColumns._ID} = $lastMission"
            db.execSQL(updateQuery)
        }

        if (lastSet > 0) {
            curBuilding = lastSet
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewHeight = h
        mViewWidth = w
        val widthRatio: Float = mViewWidth.toFloat() / baseWidth
        val sideMargin = (sideMarginBase * widthRatio).toInt()

        val playAreaSize: Int = mViewWidth - sideMargin * 2
        playArea = Rect(sideMargin, ((mViewHeight - playAreaSize)/2.0f).toInt(),
            mViewWidth-sideMargin, ((mViewHeight - playAreaSize)/2.0f).toInt()+playAreaSize)

        // Setup background BG
        val colorIndices:List<Int> = listOf(0,1,2)

        buildingOffset = mViewWidth*0.89f
        val buildingColors: List<Int> = listOf(0, 2, 4, 3)
        val frameColorList: MutableList<Int> = mutableListOf()
        val sillColorList: MutableList<Int> = mutableListOf()
        for (b in 0 until buildingColors.count()) {
            val curBuilding: Building = Building(buildingColors[b], playArea, mViewWidth, mViewHeight, widthRatio, buildingOffset * b, 0f)
            buildings.add(curBuilding)
            buildingColorLists.add(ColorManager.getColorList(buildingColors[b]))
            val awningBitmap: Bitmap = when(buildingColors[b]) {
                0 -> BitmapFactory.decodeResource(resources, R.drawable.awning_blue)
                1 -> BitmapFactory.decodeResource(resources, R.drawable.awning_red)
                2 -> BitmapFactory.decodeResource(resources, R.drawable.awning_green)
                3 -> BitmapFactory.decodeResource(resources, R.drawable.awning_purple)
                4 -> BitmapFactory.decodeResource(resources, R.drawable.awning_salmon)
                else -> BitmapFactory.decodeResource(resources, R.drawable.awning_gray)
            }

            val awningScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(awningBitmap, widthRatio)
            curBuilding.initAwnings(awningScaledBitmap)
        }

        sidewalk = Sidewalk(buildings[0].buildingFrontArea.bottom, mViewWidth, mViewHeight, widthRatio)

        for (c in 0 until buildingColorLists.count()) {
            frameColorList.add(buildingColorLists[c][2])
            sillColorList.add(buildingColorLists[c][3])
        }

        // Setup mission signs
        val mSign: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.missionsign)
        missionSignBitmap = BitmapScaler.scaleBitmap(mSign, widthRatio)

        for (m in 0 until missionSignTexts.count()) {
            missionSignXs.add((m * buildingOffset + playArea.left + missionSignOffsetX * widthRatio))
        }
        missionSignTextXOffset = missionSignBitmap.width * 0.06f + (playArea.left + missionSignOffsetX * widthRatio) - missionSignXs[0]
        missionSignY = playArea.top - missionSignBitmap.height + (missionSignOffsetY * widthRatio)
        missionSignTextY = (playArea.top - missionSignBitmap.height) + (missionSignBitmap.height * 0.4f)

        val signFont = Typeface.createFromAsset(context.assets, "futur.otf")

        missionSignPaint.setTypeface(signFont)
        missionSignPaint.color = ColorManager.missionTextColor
        missionSignPaint.textSize = missionSignBitmap.width / 7f
        missionSignPaint.flags = Paint.ANTI_ALIAS_FLAG

        // Setup windows
        initWindows(numRows, numBuildings, frameColorList.toList(), sillColorList.toList())

        for (mission in missionsList) {
            if (mission.isComplete) {
                getWindow(mission.missionNumber-1).toggle()
            }
        }

        // Setup signs
        val sLeftBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.arrowsign_left)
        val sRightBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.arrowsign_right)
        val ssBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.stopsign)
        val signLeftBitmap = BitmapScaler.scaleBitmap(sLeftBitmap, widthRatio)
        val signSize = signLeftBitmap.width
        var signLeftRect = Rect(0, mViewHeight, signSize, mViewHeight+signSize)
        if (curBuilding > 0) {
            signLeftRect = Rect(0, mViewHeight-signSize, signSize, mViewHeight)
        }
        signLeft = Sign(signLeftBitmap, null, signLeftRect)
        val signRightBitmap = BitmapScaler.scaleBitmap(sRightBitmap, widthRatio)
        var signRightRect = Rect(mViewWidth-signSize, mViewHeight-signSize, mViewWidth, mViewHeight)
        if (curBuilding == numBuildings-1) {
            signRightRect = Rect(mViewWidth-signSize, mViewHeight, mViewWidth, mViewHeight+signSize)
        }
        signRight = Sign(signRightBitmap, null, signRightRect)
        var signStopBitmap = BitmapScaler.scaleBitmap(ssBitmap, widthRatio)
        signStopBitmap = Bitmap.createScaledBitmap(signStopBitmap, signStopBitmap.width, -signStopBitmap.height, true)
        val signStopRect = Rect(mViewWidth-signSize, 0, mViewWidth, signSize)
        signStop = Sign(signStopBitmap, null, signStopRect)

        // Setup dialog box
        dialogBox = DialogBox(mContext, widthRatio, mViewWidth, mViewHeight)

        // Setup transition
        transition = Transition(mViewWidth, mViewHeight, transitionType)
        transition.initTransition(transitionType)
        transition.startTransition(false)

        currentX = -buildingOffset * curBuilding
        moveScreen()

        SoundManager.resumeMusic(0)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (missionsStatus == 0) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val windowList: List<CityWindow> = allWindowsList[curBuilding]
                    for (window in 0 until windowList.count()) {
                        if (windowList[window].frameRect.contains(event.x.toInt(), event.y.toInt())) {
                            lastWindow = windowList[window]
                            val missionNumber: Int = window + (numRows * numRows * curBuilding)
                            if (!missionsList[missionNumber].isComplete) {
                                windowList[window].toggle()
                            }
                            SoundManager.playSound(SoundManager.Sounds.SELECT_MISSION.value)

                            selectedMission = missionsList[missionNumber]
                            dialogBox.initDialog("Mission ${1+missionNumber}",
                                MissionStringHandler.getMissionStrings(selectedMission.missionSize, selectedMission.missionType,
                                    selectedMission.missionMax, selectedMission.missionLimit), true)
                            dialogBox.startTransition(true, false)
                            missionsStatus = 5
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {

                }

                if (signLeft.handleTouchEvent(event) == 1) {
                    startMove(false)
                    SoundManager.playSound(SoundManager.Sounds.OKAY.value)
                } else if (signRight.handleTouchEvent(event) == 1) {
                    startMove(true)
                    SoundManager.playSound(SoundManager.Sounds.OKAY.value)
                } else if (signStop.handleTouchEvent(event) == 1) {
                    dialogBox.initDialog("Quit to Title?", listOf(), true)
                    dialogBox.startTransition(true, true)
                    missionsStatus = 3
                    SoundManager.playSound(SoundManager.Sounds.CANCEL.value)
                }
            } else if (missionsStatus == 3) {
                dialogReturn = dialogBox.handleTouchEvent(event)
                if (dialogReturn == 1) {
                    transition.startTransition(true)
                    dialogBox.startTransition(false, true)
                    missionsStatus = 4
                } else if (dialogReturn == -1) {
                    dialogBox.startTransition(false)
                }
            } else if (missionsStatus == 5) {
                dialogReturn = dialogBox.handleTouchEvent(event)
                if (dialogReturn == 1) {
                    transition.startTransition(true)
                    dialogBox.startTransition(false, false)
                    missionsStatus = 6
                } else if (dialogReturn == -1) {
                    dialogBox.startTransition(false, false)
                    if (!selectedMission.isComplete) {
                        getWindow(selectedMission.missionNumber-1).toggle()
                    }
                }
            }

            return true
        }

        return false
    }

    private fun initWindows(numRows: Int, numSets: Int, fColors: List<Int>, sColors: List<Int>) {
        CityWindow.initSavedColors()
        val windowMarginSize = (playArea.width() * windowMarginPercent).toInt()
        windowSize = (playArea.width() - (windowMarginSize * (numRows+1))) / numRows


        for (s in 0 until numSets) {
            val windowsList: MutableList<CityWindow> = mutableListOf()
            for (r in 0 until numRows) {
                for (c in 0 until numRows) {
                    val x = playArea.left + (buildingOffset * s) + ((1 + c) * windowMarginSize) + (windowSize * c)
                    val y = playArea.top + ((1 + r) * windowMarginSize) + (windowSize * r)
                    val window: CityWindow = CityWindow((r * numRows) + c, x.toInt(), y, windowSize, fColors[s], sColors[s], false)
                    windowsList.add(window)
                }
            }
            allWindowsList.add(windowsList)
        }
    }

    private fun getWindow(windowIndex: Int): CityWindow {
        var curSet: Int = 0
        var curMax: Int = 25

        while (windowIndex >= curMax) {
            curSet++
            curMax += 25
        }

        return allWindowsList[curSet][windowIndex % 25]
    }

    private fun startMove(toRight: Boolean) {
        moveStart = currentX
        if (toRight) {
            if (curBuilding == numBuildings-1) {
                return
            } else {
                curBuilding++
            }
            moveEnd = moveStart - buildingOffset
            missionsStatus = 1
        } else {
            if (curBuilding == 0) {
                return
            } else {
                curBuilding--
            }
            moveEnd = moveStart + buildingOffset
            missionsStatus = 2
        }

        if (curBuilding == 1 && toRight) {
            signMoving = 1
            signStartY = mViewHeight.toFloat()
            signEndY = mViewHeight.toFloat() - signLeft.signHeight
        } else if (curBuilding == 0) {
            signMoving = 1
            signStartY = mViewHeight.toFloat() - signLeft.signHeight
            signEndY = mViewHeight.toFloat()
        } else if (curBuilding == 3) {
            signMoving = 2
            signStartY = mViewHeight.toFloat() - signRight.signHeight
            signEndY = mViewHeight.toFloat()
        } else if (curBuilding == 2 && !toRight) {
            signMoving = 2
            signStartY = mViewHeight.toFloat()
            signEndY = mViewHeight.toFloat() - signRight.signHeight
        } else {
            signMoving = 0
        }

        moveCounter = 0
    }

    private fun moveScreen() {
        for (b in 0 until buildings.count()) {
            buildings[b].updatePosition(currentX + b*buildingOffset, 0f)
        }

        for (s in 0 until allWindowsList.count()) {
            val curList: MutableList<CityWindow> = allWindowsList[s]
            for (w in 0 until curList.count()) {
                curList[w].updatePosition(currentX, 0f)
            }
        }

        for (m in 0 until missionSignXs.count()) {
            missionSignXs[m] += (currentX - lastX)
        }

        lastX = currentX
    }

    override fun run() {
        while(mRunning) {
            if (holder.surface.isValid) {
                mCanvas = holder.lockCanvas()
                mCanvas.save()

                mCanvas.drawColor(skyColor)

                for (b in 0 until buildings.count()) {
                    buildings[b].draw(mCanvas)
                }
                for (s in 0 until allWindowsList.count()) {
                    val curList: MutableList<CityWindow> = allWindowsList[s]
                    for (window in curList) {
                        window.update()
                        window.drawWindow(mCanvas)
                    }
                }
                for (m in 0 until missionSignTexts.count()) {
                    mCanvas.drawBitmap(missionSignBitmap, missionSignXs[m], missionSignY, null)
                    mCanvas.drawText(missionSignTexts[m], missionSignXs[m] + missionSignTextXOffset, missionSignTextY, missionSignPaint)
                }

                sidewalk.draw(mCanvas)

                signLeft.update()
                signLeft.draw(mCanvas)
                signRight.update()
                signRight.draw(mCanvas)
                signStop.update()
                signStop.draw(mCanvas)

                if (dialogBox.dialogActive) {
                    dialogBox.updateDialog()
                    dialogBox.draw(mCanvas)
                }

                if (missionsStatus == 1 || missionsStatus == 2) {
                    if (moveCounter > moveDuration) {
                        missionsStatus = 0
                    } else {
                        moveRatio = moveCounter / moveDuration
                        currentX = moveStart + (moveEnd - moveStart) * moveRatio

                        if (signMoving == 1) {
                            val signY = signStartY + (signEndY - signStartY) * moveRatio
                            signLeft.updatePosition(signLeft.posX, signY)
                        } else if (signMoving == 2) {
                            val signY = signStartY + (signEndY - signStartY) * moveRatio
                            signRight.updatePosition(signRight.posX, signY)
                        }

                        moveCounter++
                    }

                    moveScreen()
                }

                if ((missionsStatus == 3 || missionsStatus == 5) && !dialogBox.dialogActive) {
                    missionsStatus = 0
                } else if (missionsStatus == 4 && !transition.transitionActive) {
                    val intent: Intent = Intent(mContext, TitleScreenActivity::class.java)
                    intent.putExtra("transitionType", transition.transitionType)
                    mContext.startActivity(intent)
                } else if (missionsStatus == 6 && !transition.transitionActive) {
                    val intent: Intent = Intent(mContext, MainActivity::class.java)
                    intent.putExtra("transitionType", transition.transitionType)
                    intent.putExtra("numRows", selectedMission.missionSize)
                    intent.putExtra("lightMap", selectedMission.missionMap)
                    intent.putExtra("lightMap2", selectedMission.missionMap2)
                    intent.putExtra("missionNumber", selectedMission.missionNumber)
                    intent.putExtra("gameType", selectedMission.missionType)
                    intent.putExtra("missionMax", selectedMission.missionMax)
                    intent.putExtra("missionLimit", selectedMission.missionLimit)
                    intent.putExtra("returnScreen", 1)
                    intent.putExtra("lastSet", curBuilding)
                    mContext.startActivity(intent)
                }

                if (transition.transitionActive || missionsStatus >= 3) {
                    transition.updateTransition()
                    transition.draw(mCanvas)
                }

                mPath.rewind()
                mCanvas.restore()
                mSurfaceHolder.unlockCanvasAndPost(mCanvas)
            }
        }
    }

    fun pause() {
        mRunning = false
        SoundManager.pauseMusic(0)
        try {
            mGameThread.join()
        } catch (e: InterruptedException) {
        }
    }

    fun resume() {
        mRunning = true
        SoundManager.resumeMusic(0)
        mGameThread = Thread(this)
        mGameThread.start()
    }
}

data class Mission (val missionNumber: Int, val missionSize: Int, val missionType: Int, val missionMap: Long, val missionMap2: Long,
                    val missionMax: Int, val missionLimit: Long, var isComplete: Boolean)
object MissionStringHandler {
    fun getMissionStrings(missionSize: Int, missionType: Int, missionMax: Int, missionLimit: Long): List<String> {
        var stringList: MutableList<String> = mutableListOf()
        if (missionType == 2) {
            stringList.add("Kill the lights!")
            stringList.add("Two Colors")
        } else {
            stringList.add("Light it up!")
        }

        if (missionSize == 4) {
            stringList.add("4x4")
        } else if (missionSize == 5) {
            stringList.add("5x5")
        } else {
            stringList.add("6x6")
        }

        if (missionType == 1) {
            stringList.add("X-pattern")
        } else if (missionType == 2) {

        } else if (missionType == 3) {
            stringList.add("Unlit only")
        } else if (missionType == 4) {
            stringList.add("X-Pattern")
            stringList.add("Unlit only")
        }

        if (missionMax > 0) {
            stringList.add("$missionMax moves max")
        }
        if (missionLimit > 0) {
            stringList.add("${getTimeString(missionLimit)} time limit")
        }
        return stringList.toList()
    }

    fun getTimeString(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("mm:ss")
        return format.format(date)

    }
}