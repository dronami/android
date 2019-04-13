package com.dronami.brightcitylights

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.AttributeSet
import android.view.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    lateinit var mainGameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Fullscreen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        mainGameView = findViewById(R.id.maingameview)
        val gameType: Int = intent.getIntExtra("gameType", 0)
        val transitionType: Int = intent.getIntExtra("transitionType", 0)
        val numRows: Int = intent.getIntExtra("numRows", 0)
        val lightMap: Long = intent.getLongExtra("lightMap", 0)
        val lightMap2: Long = intent.getLongExtra("lightMap2", 0)
        val returnScreen: Int = intent.getIntExtra("returnScreen", 0)
        val missionNumber: Int = intent.getIntExtra("missionNumber", 0)
        val missionMax: Int = intent.getIntExtra("missionMax", 0)
        val missionLimit: Long = intent.getLongExtra("missionLimit", 0)
        val lastSet: Int = intent.getIntExtra("lastSet", 0)
        mainGameView.initGameView(this, gameType, transitionType, numRows, lightMap, lightMap2, returnScreen, missionNumber,
            missionMax, missionLimit, lastSet)
    }

    override fun onPause() {
        super.onPause()
        mainGameView.pause()
    }

    override fun onResume() {
        super.onResume()
        mainGameView.resume()
    }
}

class GameView : SurfaceView, Runnable {
    private lateinit var mContext: Context
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mPath: Path
    private lateinit var mGameThread: Thread
    private lateinit var mCanvas: Canvas
    private var gameType: Int = 0
    private var transitionType: Int = 0
    private var numRows: Int = 5
    private var lightMap: Long = 0
    private var lightMap2: Long = 0
    private var returnScreen: Int = 0
    private var missionNumber: Int = 0
    private var missionMax: Int = 0
    private var missionLimit: Long = 0
    private var lastSet: Int = 0

    private lateinit var transition: Transition

    private var lastTouched: Point = Point()

    private lateinit var playArea: Rect

    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    private val baseWidth: Float = 1080F
    private val sideMarginBase: Int = 60
    private val stoplightMargin: Float = 0.1f

    private val timerBoxRatio: Float = 0.55f
    private val awningRatio: Float = 0.4f

    private var windowsList: MutableList<CityWindow> = mutableListOf()
    private var windowMarginPercent: Float = 0.04f
    private var windowSize: Int = 0
    private var windowMarginSize: Int = 0

    private lateinit var timerBox: TimerBox
    private var timerStart: Long = 0
    private var timerSaved: Long = 0
    private var currentTime: Long = 0
    private lateinit var counterBox: CounterBox
    private var moveCounter: Int = 0

    private lateinit var background: Background

    private val signWidthRatio = 0.28f
    private var soundOn: Boolean = true
    private lateinit var soundSign: Sign
    private lateinit var stopSign: Sign

    private lateinit var dialogBox: DialogBox
    private var dialogReturn: Int = 0

    private lateinit var stoplight: Stoplight
    private var stoplightSize: Float = 1.5f

    // Actual thread is running
    private var mRunning: Boolean = false
    // Soft pause (showing dialog)
    private var gameStatus: Int = -2

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        //initGameView(context)
        timerStart = System.currentTimeMillis()
    }

    fun initGameView(_context: Context, gType: Int, tType: Int, nRows: Int, lMap: Long, lMap2: Long, rScreen: Int, mNumber: Int,
                     mMax: Int, mLimit: Long, lSet: Int) {
        mContext = _context
        mSurfaceHolder = holder
        mPath = Path()
        gameType = gType
        transitionType = tType
        numRows = nRows
        lightMap = lMap
        lightMap2 = lMap2
        returnScreen = rScreen
        missionNumber = mNumber
        missionMax = mMax
        missionLimit = mLimit
        lastSet = lSet
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

        val colorIndices: List<Int> = randomizeColors(-1)
        background = Background(colorIndices)

        val awningBitmap: Bitmap = when(colorIndices[0]) {
            0 -> BitmapFactory.decodeResource(resources, R.drawable.awning_blue)
            1 -> BitmapFactory.decodeResource(resources, R.drawable.awning_red)
            2 -> BitmapFactory.decodeResource(resources, R.drawable.awning_green)
            3 -> BitmapFactory.decodeResource(resources, R.drawable.awning_purple)
            4 -> BitmapFactory.decodeResource(resources, R.drawable.awning_salmon)
            else -> BitmapFactory.decodeResource(resources, R.drawable.awning_gray)
        }

        val awningWidth: Int = (playAreaSize * awningRatio).toInt()
        val awningScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(awningBitmap, awningWidth)
        background.initBackground(playArea, mViewWidth, mViewHeight, widthRatio)
        background.mainBuilding.initAwnings(awningScaledBitmap)

        // Setup and scale TimerBox
        val timerWidth = (playAreaSize * timerBoxRatio).toInt()
        val tbBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.timerbox)
        val tbScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(tbBitmap, timerWidth)
        val tbX: Int = (playArea.left + (playArea.width()*0.05f) * widthRatio).toInt()
        val tbY: Int = ((playArea.top - tbScaledBitmap.height) - (tbScaledBitmap.height * 0.1f)).toInt()
        timerBox = TimerBox(tbX, tbY, tbScaledBitmap)

        val dsBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.digitsheet)
        val dsScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(dsBitmap, timerWidth)
        val digitWidth: Int = (dsScaledBitmap.width / 8.0f).toInt()
        timerBox.initDigits(digitWidth, digitWidth * 2, dsScaledBitmap)

        // Setup and scale CounterBox
        val mcBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.counterbox)
        val mcScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(mcBitmap, timerWidth)
        val mcX: Int = (playArea.left + (playArea.width()*0.65f)).toInt()
        val mcY: Int = tbY
        counterBox = CounterBox(mcX, mcY, mcScaledBitmap)

        val cdsBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cdigitsheet)
        val cdsScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(cdsBitmap, timerWidth)
        counterBox.initDigits(digitWidth, digitWidth * 2, cdsScaledBitmap)

        // Setup signs
        val signWidth: Int = (mViewWidth * signWidthRatio).toInt()
        val sOffBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.soundsign_off)
        val sOnBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.soundsign_on)
        val ssBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.stopsign)
        val soundOffBitmap = BitmapScaler.scaleBitmap(sOffBitmap, signWidth)
        val soundOnBitmap = BitmapScaler.scaleBitmap(sOnBitmap, signWidth)
        val signSize = soundOffBitmap.width
        val soundRect = Rect(0, mViewHeight-signSize, signSize, mViewHeight)
        soundSign = Sign(soundOnBitmap, soundOffBitmap, soundRect)

        val dbHelper: PuzzlesDBHelper = PuzzlesDBHelper(mContext)
        val db = dbHelper.readableDatabase

        val selectQuery = "SELECT ${SettingsContract.SettingsEntry.SETTINGS_SOUND} " +
                " FROM ${SettingsContract.SettingsEntry.TABLE_NAME} WHERE ${BaseColumns._ID} = 1"
        val cursor = db.rawQuery(
            selectQuery, null
        )
        cursor.moveToNext()

        soundOn = cursor.getInt(0) == 0
        soundSign.signOn = soundOn

        val stopBitmap = BitmapScaler.scaleBitmap(ssBitmap, signWidth)
        val stopRect = Rect(mViewWidth-signSize, mViewHeight-signSize, mViewWidth, mViewHeight)
        stopSign = Sign(stopBitmap, null, stopRect)

        // Setup windows
        initWindows(numRows, lightMap, lightMap2, ColorManager.getSpecificColor(colorIndices[0], 2), ColorManager.getSpecificColor(colorIndices[0], 3))

        // Setup stoplight
        val stoplightWidth: Int = (mViewWidth - (mViewWidth * stoplightMargin) * 2.0f).toInt()
        val stoplightBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.stoplight)
        val stoplightScaledBitmap: Bitmap = BitmapScaler.scaleBitmap(stoplightBitmap, stoplightWidth)
        stoplight = Stoplight(stoplightScaledBitmap, mViewWidth, mViewHeight)

        // Setup dialog box
        var header: String = "Mission $missionNumber"
        if (returnScreen == 0) {
            header = "Quick Play"
        }
        dialogBox = DialogBox(mContext, widthRatio, mViewWidth, mViewHeight)
        dialogBox.initDialog(header,
            MissionStringHandler.getMissionStrings(numRows, gameType,
                missionMax, missionLimit), false)
        dialogBox.startTransition(true, false)

        // Setup transition
        transition = Transition(mViewWidth, mViewHeight, transitionType)
        transition.startTransition(false)

        SoundManager.pauseMusic(0)
        SoundManager.resumeMusic(1)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            lastTouched = Point(event.x.toInt(), event.y.toInt())
            dialogReturn = dialogBox.handleTouchEvent(event)
            if (dialogReturn == 1) {
                if (gameStatus == -2) {
                    stoplight.startCountdown()
                    gameStatus = -1
                } else if (gameStatus >= 1) {
                    transition.startTransition(true)
                    gameStatus++
                }
                dialogBox.startTransition(false, false)
            } else if (dialogReturn == -1) {
                dialogBox.startTransition(false)
            }

            if (gameStatus == 0) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    for (window in windowsList) {
                        if (window.frameRect.contains(lastTouched.x, lastTouched.y)) {
                            if (gameType < 3 || window.litState == 0) {
                                moveCounter++
                                toggleWindows(window.index)
                                if (checkLights(gameType != PuzzleTypes.TWOCOLOR.value)) {
                                    SoundManager.playSound(SoundManager.Sounds.GAME_WIN.value)
                                    showDialog(
                                        "You Win!",
                                        listOf("Moves: $moveCounter", "Time: ${timerBox.getTimeString(currentTime)}"),
                                        false,
                                        false
                                    )
                                    gameStatus = 4
                                }

                                if (missionMax > 0 && moveCounter > missionMax) {
                                    SoundManager.playSound(SoundManager.Sounds.GAME_LOSE.value)
                                    showDialog("Game Over!",
                                        listOf("Too many", "moves! ($moveCounter)"),
                                        false,
                                        false)
                                    gameStatus = 6
                                }

                            }
                        }
                    }
                }

                if (soundSign.handleTouchEvent(event) == 1) {
                    soundSign.signOn = !soundSign.signOn
                    soundOn = soundSign.signOn

                    val dbHelper: PuzzlesDBHelper = PuzzlesDBHelper(mContext)
                    val dbW = dbHelper.writableDatabase
                    var soundVal: Int = 1
                    SoundManager.silenced = !soundOn
                    if (soundOn) {
                        soundVal = 0
                        SoundManager.playSound(SoundManager.Sounds.OKAY.value)
                    }

                    val updateQuery = "UPDATE ${SettingsContract.SettingsEntry.TABLE_NAME} SET " +
                            "${SettingsContract.SettingsEntry.SETTINGS_SOUND} = $soundVal " +
                            "WHERE ${BaseColumns._ID} = 1"
                    dbW.execSQL(updateQuery)
                    if (soundOn) {
                        SoundManager.resumeMusic(1)
                    } else {
                        SoundManager.pauseMusic(1)
                    }
                }
                if (stopSign.handleTouchEvent(event) == 1) {
                    showDialog("Quit Game?", listOf(), true, true)
                    SoundManager.playSound(SoundManager.Sounds.CANCEL.value)
                }
            }

            return true
        }

        return false
    }

    private fun showDialog(header: String, bodyTexts: List<String>, isYesNo: Boolean, horizontal: Boolean) {
        dialogBox.initDialog(header, bodyTexts, isYesNo)
        dialogBox.startTransition(true, horizontal)
        pause(true)
    }

    private fun randomizeColors(mainColor: Int = -1) : List<Int> {
        val colorCount = ColorManager.getColorCount()
        var randList: MutableList<Int> = mutableListOf()
        var mColor: Int = 0
        var lColor: Int = 0
        var rColor: Int = 0
        for (c in 0 until colorCount) {
            randList.add(c)
        }
        if (mainColor == -1) {
            mColor = randList[Random.nextInt(randList.count())]
        } else {
            mColor = mainColor
        }
        randList.remove(mColor)
        lColor = randList[Random.nextInt(randList.count())]
        randList.remove(lColor)
        rColor = randList[Random.nextInt(randList.count())]

        return listOf(mColor, lColor, rColor)
    }

    private fun toggleWindows(index: Int) {
        windowsList[index].toggle()
        SoundManager.playSound(SoundManager.Sounds.LIGHT_SELECT.value)
        if (gameType == PuzzleTypes.X.value || gameType == PuzzleTypes.LITONLYX.value) {
            // Up-Left
            if (index % numRows > 0 && index > numRows-1) {
                windowsList[index-numRows-1].toggle()
            }
            // Up-Right
            if (index % numRows < numRows-1 && index > numRows-1) {
                windowsList[index-numRows+1].toggle()
            }
            // Down-Left
            if (index % numRows > 0 && index < numRows*(numRows-1)) {
                windowsList[index+numRows-1].toggle()
            }
            // Down-Right
            if (index % numRows < numRows-1 && index < numRows*(numRows-1)) {
                windowsList[index+numRows+1].toggle()
            }
        } else {
            // Up
            if (index-numRows >= 0) {
                windowsList[index-numRows].toggle()
            } else if (numRows == 4) {
                windowsList[index+(numRows*3)].toggle()
            }
            // Right
            if (index % numRows != numRows-1 && index + 1 < (numRows*numRows)) {
                windowsList[index+1].toggle()
            } else if (numRows == 4) {
                windowsList[index-(numRows-1)].toggle()
            }
            // Down
            if (index + numRows < (numRows*numRows)) {
                windowsList[index+numRows].toggle()
            } else if (numRows == 4) {
                windowsList[index-(numRows*3)].toggle()
            }
            // Left
            if (index % numRows != 0 && index-1 >= 0) {
                windowsList[index-1].toggle()
            } else if (numRows == 4) {
                windowsList[index+(numRows-1)].toggle()
            }
        }
    }

    private fun checkLights(lightsOn: Boolean) : Boolean {
        for (window in windowsList) {
            if ((window.litState != 1 && lightsOn) || (window.litState != 0 && !lightsOn)) {
                return false
            }
        }

        return true
    }

    override fun run() {
        while(mRunning) {
            if (holder.surface.isValid) {
                mCanvas = holder.lockCanvas()
                mCanvas.save()
                //mCanvas.drawBitmap(gameBG, sideMargin.toFloat(), ((mViewHeight-gameBG.height).toFloat())/2.0f, null)
                background.draw(mCanvas)
                for (window in windowsList) {
                    window.update()
                    window.drawWindow(mCanvas)
                }
                if (gameStatus == 0) {
                    currentTime = timerSaved + System.currentTimeMillis() - timerStart

                    if (missionLimit > 0 && currentTime > missionLimit) {
                        SoundManager.playSound(SoundManager.Sounds.GAME_LOSE.value)
                        showDialog("Game Over!",
                            listOf("Too slow!", "${MissionStringHandler.getTimeString(currentTime)}"),
                            false,
                            false)
                        gameStatus = 6
                    }
                }
                timerBox.updateTime(currentTime)
                timerBox.drawBox(mCanvas)

                counterBox.updateCounter(moveCounter)
                counterBox.drawBox(mCanvas)

                soundSign.update()
                soundSign.draw(mCanvas)
                stopSign.update()
                stopSign.draw(mCanvas)

                if (dialogBox.dialogActive || gameStatus == 2 || gameStatus == 5 || gameStatus == 7) {
                    dialogBox.updateDialog()
                    dialogBox.draw(mCanvas)
                }
                if (gameStatus == -1) {
                    stoplight.updateStoplight()
                    stoplight.draw(mCanvas)
                    if (!stoplight.stoplightActive) {
                        resume(true)
                    }
                }
                if (gameStatus == 1) {
                    if (!dialogBox.dialogActive) {
                        resume(true)
                    }
                }

                if (transition.transitionActive || gameStatus == 2 || gameStatus == 5 || gameStatus == 7) {
                    transition.updateTransition()
                    transition.draw(mCanvas)
                }
                if (gameStatus == 2 && !transition.transitionActive) {
                    if (returnScreen == 0) {
                        val intent: Intent = Intent(mContext, TitleScreenActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("lastSize", numRows)
                        SoundManager.pauseMusic(1)
                        mContext.startActivity(intent)
                    } else {
                        val intent: Intent = Intent(mContext, MissionsActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("missionBeat", false)
                        intent.putExtra("lastSet", lastSet)
                        SoundManager.pauseMusic(1)
                        mContext.startActivity(intent)
                    }
                } else if (gameStatus == 5 && !transition.transitionActive) {
                    if (returnScreen == 0) {
                        val intent: Intent = Intent(mContext, TitleScreenActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("lastSize", numRows)
                        SoundManager.pauseMusic(1)
                        mContext.startActivity(intent)
                    } else {
                        val intent: Intent = Intent(mContext, MissionsActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("missionNumber", missionNumber)
                        intent.putExtra("missionBeat", true)
                        intent.putExtra("lastSet", lastSet)
                        SoundManager.pauseMusic(1)
                        mContext.startActivity(intent)
                    }
                } else if (gameStatus == 7 && !transition.transitionActive) {
                    if (returnScreen == 0) {
                        val intent: Intent = Intent(mContext, TitleScreenActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("lastSize", numRows)
                        SoundManager.pauseMusic(1)
                        mContext.startActivity(intent)
                    } else {
                        val intent: Intent = Intent(mContext, MissionsActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("missionNumber", missionNumber)
                        intent.putExtra("missionBeat", false)
                        intent.putExtra("lastSet", lastSet)
                        SoundManager.pauseMusic(1)
                        mContext.startActivity(intent)
                    }
                }

                mPath.rewind()
                mCanvas.restore()
                mSurfaceHolder.unlockCanvasAndPost(mCanvas)
            }
        }
    }

    fun pause(softPause: Boolean = false) {
        timerSaved = currentTime
        if (softPause) {
            gameStatus = 1
        } else {
            SoundManager.pauseMusic(1)
            mRunning = false
            try {
                mGameThread.join()
            } catch (e: InterruptedException) {

            }
        }
    }

    fun resume(softPause: Boolean = false) {
        if (softPause) {
            gameStatus = 0
        } else {
            SoundManager.resumeMusic(1)
            mRunning = true
            mGameThread = Thread(this)
            mGameThread.start()
        }
        timerStart = System.currentTimeMillis()
    }

    private fun initWindows(numRows: Int, windowsMap: Long, windowsMap2: Long, fColor: Int, sColor: Int) {
        CityWindow.initSavedColors()
        windowMarginSize = (playArea.width() * windowMarginPercent).toInt()
        windowSize = (playArea.width() - (windowMarginSize * (numRows+1))) / numRows

        val silhouetteSheet: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.silhouettesheet)
        CityWindow.initSilhouettes(silhouetteSheet, windowSize)

        var curBit: Int = 0
        for (r in 0 until numRows) {
            for (c in 0 until numRows) {
                val x = playArea.left + ((1+c) * windowMarginSize) + (windowSize * c)
                val y = playArea.top + ((1+r) * windowMarginSize) + (windowSize * r)
                val curWindow = (r*numRows)+c
                val curBitValue: Long = (windowsMap and (1 shl curBit).toLong()) shr curBit
                val window: CityWindow = CityWindow( curWindow, x, y, windowSize, fColor, sColor, true, gameType == 2)
                if (curBitValue == 1.toLong()) {
                    window.toggle()
                }
                curBit++

                windowsList.add(window)
            }
        }

        if (gameType == PuzzleTypes.TWOCOLOR.value) {
            curBit = 0
            for (r in 0 until numRows) {
                for (c in 0 until numRows) {
                    val window = windowsList[(r*numRows)+c]
                    val curBitValue: Long = (windowsMap2 and (1 shl curBit).toLong()) shr curBit
                    if (curBitValue == 1.toLong()) {
                        window.litState = 2
                        window.lightUpState = 3
                    }
                    curBit++
                }
            }
        }
    }
}
