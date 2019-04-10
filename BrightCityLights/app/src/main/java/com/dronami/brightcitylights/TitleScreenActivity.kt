package com.dronami.brightcitylights

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.AttributeSet
import android.view.*
import java.io.BufferedReader
import java.io.File
import kotlin.random.Random

class TitleScreenActivity : AppCompatActivity() {

    lateinit var titleScreen: TitleScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_title)

        titleScreen = findViewById(R.id.maintitle)
        var transitionType: Int = -1
        if (intent != null) {
            transitionType = intent.getIntExtra("transitionType", -1)
        }
        SoundManager.initSoundManager(this)
        titleScreen.initTitleScreen(this, transitionType)
    }

    override fun onPause() {
        super.onPause()
        titleScreen.pause()
    }

    override fun onResume() {
        super.onResume()
        titleScreen.resume()
    }
}

class TitleScreen : SurfaceView, Runnable {
    private lateinit var mContext: Context
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mPath: Path
    private lateinit var mGameThread: Thread
    private var mRunning:Boolean = false
    private lateinit var mCanvas: Canvas
    private var titleStatus: Int = 0
    private var settingsInitialized: Boolean = false

    private lateinit var logoBitmap: Bitmap
    private var logoX: Float = 0f
    private var logoY: Float = 0f
    private val logoMarginRatio: Float = 0.1f
    private lateinit var dronamiBitmap: Bitmap
    private var dronamiX: Float = 0f
    private var dronamiY: Float = 0f

    private lateinit var dialogBox: DialogBox
    private var dialogReturn: Int = 0

    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    private val baseWidth: Float = 1080F

    private val skylineBitmaps: MutableList<Bitmap> = mutableListOf()
    private var skylineX: Float = 0f
    private var skylineY: Float = 0f
    private val skylineVel: Float = -1.0f

    private val bridgeBitmaps: MutableList<Bitmap> = mutableListOf()
    private var numBridges: Int = 1
    private var bridgeX: Float = 0f
    private var bridgeY: Float = 0f
    private val bridgeVel: Float = -4.0f

    private lateinit var riverRect: Rect
    private var riverPaint: Paint = Paint()
    private val riverColor: Int = Color.rgb(0, 50, 152)

    private val skyColor: Int = Color.rgb(40, 40, 80)

    private val buttonBitmaps: MutableList<Bitmap> = mutableListOf()
    private var buttonDown: MutableList<Boolean> = mutableListOf(false, false, false)
    private var buttonMargin: Float = 0f
    private val buttonMarginRatio: Float = 0.05f
    private lateinit var buttonRects: List<Rect>
    private lateinit var quickRect: Rect
    private lateinit var missionsRect: Rect
    private lateinit var tutorialRect: Rect

    private lateinit var transition: Transition
    private var firstTime: Boolean = true
    private var firstTimeEver: Boolean = true
    private var transitionType: Int = -1

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        //initTitleScreen(context)
    }

    fun initTitleScreen(_context: Context, tType: Int) {
        mContext = _context
        mSurfaceHolder = holder
        mPath = Path()

        firstTime = tType == -1
        transitionType = tType

        val dbTest: File = mContext.getDatabasePath("Puzzles.db")
        if (dbTest.exists()) {
            firstTimeEver = false
        } else {
            firstTimeEver = true
        }
    }

    private fun initDB() {
        val dialogFont = Typeface.createFromAsset(context.assets, "futur.otf")

        val headerPaint = Paint()
        headerPaint.setTypeface(dialogFont)
        headerPaint.setARGB(255, 240, 240, 240)
        headerPaint.textSize = mViewWidth / 16f
        headerPaint.flags = Paint.ANTI_ALIAS_FLAG

        val shadowPaint = Paint()
        shadowPaint.setTypeface(dialogFont)
        shadowPaint.textSize = mViewWidth / 16f
        shadowPaint.flags = Paint.ANTI_ALIAS_FLAG
        val shadowOffset = shadowPaint.textSize / 4.0f
        val loadingTextA: String = "Initializing DB..."
        val loadingTextB: String = "(First time only)"
        val bounds: Rect = Rect()
        headerPaint.getTextBounds(loadingTextA, 0, loadingTextA.length, bounds)
        val textX: Float = (mViewWidth / 2.0f) - headerPaint.measureText(loadingTextA)/2.0f
        val textY: Float = (mViewHeight / 2.0f) - bounds.height()

        if (holder.surface.isValid) {
            mCanvas = holder.lockCanvas()
            mCanvas.save()

            mCanvas.drawColor(skyColor)
            mCanvas.drawText(loadingTextA, shadowOffset + textX, shadowOffset + textY, shadowPaint)
            mCanvas.drawText(loadingTextA, textX, textY, headerPaint)
            mCanvas.drawText(loadingTextB, shadowOffset + textX, shadowOffset + textY + bounds.height() * 1.1f, shadowPaint)
            mCanvas.drawText(loadingTextB, textX, textY + bounds.height() * 1.1f, headerPaint)

            mPath.rewind()
            mCanvas.restore()
            mSurfaceHolder.unlockCanvasAndPost(mCanvas)
        }

        val dbHelper = PuzzlesDBHelper(mContext)
        val db = dbHelper.writableDatabase

        // Init settings
        val settingsValues = ContentValues().apply {
            put(SettingsContract.SettingsEntry.SETTINGS_SOUND, 0)
            put(SettingsContract.SettingsEntry.SETTINGS_SIZE, 0)
            put(SettingsContract.SettingsEntry.SETTINGS_EXPERT, 0)
        }

        db?.insert(SettingsContract.SettingsEntry.TABLE_NAME, null, settingsValues)

        // Read from files and push to DB
        val filenameList: List<String> = listOf("puzzles-4x4.txt", "puzzles-4x4litonly.txt",
            "puzzles-5x5.txt", "puzzles-5x5b.txt", "puzzles-twocolor.txt",
            "puzzles-6x6.txt", "puzzles-6x6x.txt", "puzzles-litonly.txt",
            "puzzles-litonlyx.txt")
        val sizeList: List<Int> = listOf(4,4,5,5,5,6,6,6,6)
        val typeList: List<Int> = listOf(PuzzleTypes.NORMAL.value, PuzzleTypes.LITONLY.value,
            PuzzleTypes.NORMAL.value, PuzzleTypes.NORMAL.value, PuzzleTypes.TWOCOLOR.value,
            PuzzleTypes.NORMAL.value, PuzzleTypes.X.value, PuzzleTypes.LITONLY.value,
            PuzzleTypes.LITONLYX.value)
        var puzzleNumber: Int = 0
        for (x in 0 until filenameList.count()) {
            var curSize: Int = sizeList[x]
            var curType: Int = typeList[x]
            var puzzlesReader: BufferedReader = mContext.assets.open(filenameList[x]).bufferedReader()
            var lines: List<String> = puzzlesReader.readLines()
            for (line in lines) {
                if (line.isEmpty()) {
                    continue
                }
                if (curType != PuzzleTypes.TWOCOLOR.value) {
                    val values = ContentValues().apply {
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_SIZE, curSize)
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_TYPE, curType)
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_MAP, line.toLong())
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_MAP2, 0.toLong())
                    }

                    db?.insert(PuzzlesContract.PuzzleEntry.TABLE_NAME, null, values)

                    puzzleNumber++
                } else {
                    val maps = line.split(" ")
                    val values = ContentValues().apply {
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_SIZE, curSize)
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_TYPE, curType)
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_MAP, maps[0].toLong())
                        put(PuzzlesContract.PuzzleEntry.PUZZLE_MAP2, maps[1].toLong())
                    }

                    db?.insert(PuzzlesContract.PuzzleEntry.TABLE_NAME, null, values)
                    puzzleNumber++
                }
            }
        }

        var missionsReader: BufferedReader = mContext.assets.open("missions.txt").bufferedReader()
        var lines: List<String> = missionsReader.readLines()
        for (line in lines) {
            if (line.isEmpty()) {
                continue
            }
            val tokens: List<String> = line.split(" ")
            val values = ContentValues().apply {
                put(MissionsContract.MissionEntry.PUZZLE_ID, tokens[0].toInt())
                put(MissionsContract.MissionEntry.MOVE_LIMIT, tokens[1].toInt())
                put(MissionsContract.MissionEntry.TIME_LIMIT, tokens[2].toLong())
                put(MissionsContract.MissionEntry.COMPLETED, 0)
            }
            db?.insert(MissionsContract.MissionEntry.TABLE_NAME, null, values)
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mViewHeight = h
        mViewWidth = w
        val widthRatio: Float = mViewWidth / baseWidth
        buttonMargin = mViewWidth * buttonMarginRatio
        val buttonWidth: Int = (mViewWidth - (buttonMargin * 2)).toInt()

        // Setup BG
        val bgBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.title_skyline)
        skylineBitmaps.add(BitmapScaler.scaleBitmap(bgBitmap, widthRatio))
        skylineBitmaps.add(skylineBitmaps[0].copy(skylineBitmaps[0].config, true))
        skylineY = (mViewHeight * 3.0f / 4.0f) - (skylineBitmaps[0].height / 2.0f)

        val bridgeBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.title_bridge)
        numBridges = 1 + Math.ceil(mViewWidth / bridgeBitmap.width.toDouble()).toInt()
        bridgeBitmaps.add(BitmapScaler.scaleBitmap(bridgeBitmap, widthRatio))
        for (b in 1 until numBridges) {
            bridgeBitmaps.add(bridgeBitmaps[0].copy(bridgeBitmaps[0].config, true))
        }
        bridgeY = (mViewHeight - bridgeBitmaps[0].height).toFloat()

        riverRect = Rect(0, (skylineY + (skylineBitmaps[0].height / 2.0f)).toInt(), mViewWidth, mViewHeight)
        riverPaint.color = riverColor

        // Setup logos
        logoX = mViewWidth * logoMarginRatio
        val lBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        logoBitmap = BitmapScaler.scaleBitmap(lBitmap, (mViewWidth - logoX * 2).toInt())
        logoY = ((mViewHeight / 4.0f) - logoBitmap.height / 2.0f)

        val dBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.dronami)
        dronamiBitmap = BitmapScaler.scaleBitmap(dBitmap, (mViewWidth * 0.3f).toInt())
        dronamiX = 0f
        dronamiY = 0f

        // Setup buttons
        val quickButtonUp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_quick_up)
        buttonBitmaps.add(BitmapScaler.scaleBitmap(quickButtonUp, buttonWidth))
        val quickButtonDown: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_quick_down)
        buttonBitmaps.add(BitmapScaler.scaleBitmap(quickButtonDown, buttonWidth))
        val missionsButtonUp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_missions_up)
        buttonBitmaps.add(BitmapScaler.scaleBitmap(missionsButtonUp, buttonWidth))
        val missionsButtonDown: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_missions_down)
        buttonBitmaps.add(BitmapScaler.scaleBitmap(missionsButtonDown, buttonWidth))
        val tutorialButtonUp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_tutorial_up)
        buttonBitmaps.add(BitmapScaler.scaleBitmap(tutorialButtonUp, buttonWidth))
        val tutorialButtonDown: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_tutorial_down)
        buttonBitmaps.add(BitmapScaler.scaleBitmap(tutorialButtonDown, buttonWidth))

        val quickButtonY = ((mViewHeight / 2.0f))
        quickRect = Rect(buttonMargin.toInt(), quickButtonY.toInt(),
            (buttonMargin + buttonBitmaps[0].width).toInt(), (quickButtonY + buttonBitmaps[0].height).toInt())
        val missionsButtonY = ((mViewHeight / 2.0f) + (mViewHeight / 2.0f) * 0.33f)
        missionsRect = Rect(buttonMargin.toInt(), missionsButtonY.toInt(),
            (buttonMargin + buttonBitmaps[0].width).toInt(), (missionsButtonY + buttonBitmaps[0].height).toInt())
        val tutorialButtonY = ((mViewHeight / 2.0f) + (mViewHeight / 2.0f) * 0.66f)
        tutorialRect = Rect(buttonMargin.toInt(), tutorialButtonY.toInt(),
            (buttonMargin + buttonBitmaps[0].width).toInt(), (tutorialButtonY + buttonBitmaps[0].height).toInt())

        buttonRects = listOf(quickRect, missionsRect, tutorialRect)

        // Setup dialog box
        dialogBox = DialogBox(mContext, widthRatio, mViewWidth, mViewHeight)
        if (!firstTimeEver) {
            initSettings()
        }

        // Setup transition
        transition = Transition(mViewWidth, mViewHeight, transitionType)
        transition.initTransition(transitionType)
        if (!firstTime) {
            transition.startTransition(false)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (firstTimeEver) {
            return false
        }
        if (event != null) {
            if (titleStatus == 0) {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    for (b in 0 until 3) {
                        if (!buttonDown[b] && buttonRects[b].contains(event.x.toInt(), event.y.toInt())) {
                            buttonDown[b] = true
                            SoundManager.playSound(SoundManager.Sounds.SWITCH2.value)
                        } else if (buttonDown[b] && !buttonRects[b].contains(event.x.toInt(), event.y.toInt())) {
                            buttonDown[b] = false
                            SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    buttonDown[0] = false
                    buttonDown[1] = false
                    buttonDown[2] = false
                    if (quickRect.contains(event.x.toInt(), event.y.toInt())) {
                        dialogBox.initSelectionDialog("Quick Play", listOf("4x4", "5x5", "6x6"), listOf("Expert"), true)
                        dialogBox.startTransition(true, false)
                        titleStatus = 1
                        SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    } else if (missionsRect.contains(event.x.toInt(), event.y.toInt())) {
                        transition.initTransition(-1)
                        transition.startTransition(true)
                        titleStatus = 3
                        SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    } else if (tutorialRect.contains(event.x.toInt(), event.y.toInt())) {
                        transition.initTransition(-1)
                        transition.startTransition(true)
                        titleStatus = 4
                        SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    }
                }
            } else {
                dialogReturn = dialogBox.handleTouchEvent(event)
                if (dialogReturn == 1) {
                    transition.initTransition(-1)
                    transition.startTransition(true)
                    titleStatus = 2
                } else if (dialogReturn == -1) {
                    dialogBox.startTransition(false, false)
                    titleStatus = 5
                }
            }

            return true
        }

        return false
    }

    private fun getRandomPuzzle(numRows: Int, isExpert: Boolean, intent: Intent) {
        val dbHelper: PuzzlesDBHelper = PuzzlesDBHelper(mContext)
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID, PuzzlesContract.PuzzleEntry.PUZZLE_SIZE, PuzzlesContract.PuzzleEntry.PUZZLE_TYPE,
            PuzzlesContract.PuzzleEntry.PUZZLE_MAP, PuzzlesContract.PuzzleEntry.PUZZLE_MAP2)
        var selection = "${PuzzlesContract.PuzzleEntry.PUZZLE_SIZE} = ? AND ${PuzzlesContract.PuzzleEntry.PUZZLE_TYPE} = ?"
        if (isExpert) {
            selection = "${PuzzlesContract.PuzzleEntry.PUZZLE_SIZE} = ? AND NOT ${PuzzlesContract.PuzzleEntry.PUZZLE_TYPE} = ?"
        }
        val selectionArgs = arrayOf(numRows.toString(), 0.toString())

        val cursor = db.query(
            PuzzlesContract.PuzzleEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val randPuzzle: Int = Random.nextInt(0, cursor.count)

        repeat(randPuzzle) {
            cursor.moveToNext()
        }
        intent.putExtra("numRows", cursor.getInt(1))
        intent.putExtra("gameType", cursor.getInt(2))
        intent.putExtra("lightMap", cursor.getLong(3))
        intent.putExtra("lightMap2", cursor.getLong(4))

        val dbW = dbHelper.writableDatabase
        var expertVal: Int = 0
        if (isExpert) {
            expertVal = 1
        }

        val updateQuery = "UPDATE ${SettingsContract.SettingsEntry.TABLE_NAME} SET ${SettingsContract.SettingsEntry.SETTINGS_EXPERT} = $expertVal, " +
                "${SettingsContract.SettingsEntry.SETTINGS_SIZE} = ${numRows-4} " +
                "WHERE ${BaseColumns._ID} = 1"
        dbW.execSQL(updateQuery)
    }

    fun initSettings() {
        val dbHelper: PuzzlesDBHelper = PuzzlesDBHelper(mContext)
        val db = dbHelper.readableDatabase

        val selectQuery = "SELECT ${SettingsContract.SettingsEntry.SETTINGS_SIZE}, " +
                "${SettingsContract.SettingsEntry.SETTINGS_EXPERT}, ${SettingsContract.SettingsEntry.SETTINGS_SOUND} " +
                " FROM ${SettingsContract.SettingsEntry.TABLE_NAME}"
        val cursor = db.rawQuery(
            selectQuery, null
        )
        cursor.moveToNext()

        dialogBox.initSelectionDialog("Quick Play", listOf("4x4", "5x5", "6x6"), listOf("Expert"), true)
        dialogBox.setSelection(cursor.getInt(0))
        dialogBox.setCheckbox(0, cursor.getInt(1) == 1)
        SoundManager.silenced = cursor.getInt(2) == 1

        SoundManager.resumeMusic(0)
        settingsInitialized = true
    }

    override fun run() {
        while(mRunning) {
            if (holder.surface.isValid) {
                if (firstTimeEver) {
                    SoundManager.resumeMusic(0)
                    initDB()
                    firstTimeEver = false
                    initSettings()
                }

                mCanvas = holder.lockCanvas()
                mCanvas.save()

                mCanvas.drawColor(skyColor)
                mCanvas.drawRect(riverRect, riverPaint)

                skylineX += skylineVel
                if (skylineX < -skylineBitmaps[0].width) {
                    skylineX += skylineBitmaps[0].width
                }
                mCanvas.drawBitmap(skylineBitmaps[0], skylineX, skylineY, null)
                mCanvas.drawBitmap(skylineBitmaps[0], skylineX + skylineBitmaps[0].width, skylineY, null)

                bridgeX += bridgeVel
                if (bridgeX < -bridgeBitmaps[0].width) {
                    bridgeX += bridgeBitmaps[0].width
                }
                if (bridgeX > -bridgeBitmaps[0].width) {
                    mCanvas.drawBitmap(bridgeBitmaps[0], bridgeX, bridgeY, null)
                }
                for (b in 1 until numBridges) {
                    mCanvas.drawBitmap(bridgeBitmaps[0], bridgeX + b * bridgeBitmaps[0].width, bridgeY, null)
                }

                mCanvas.drawBitmap(dronamiBitmap, dronamiX, dronamiY, null)
                mCanvas.drawBitmap(logoBitmap, logoX, logoY, null)

                if (buttonDown[0]) {
                    mCanvas.drawBitmap(buttonBitmaps[1], quickRect.left.toFloat(), quickRect.top.toFloat(), null)
                } else {
                    mCanvas.drawBitmap(buttonBitmaps[0], quickRect.left.toFloat(), quickRect.top.toFloat(), null)
                }
                if (buttonDown[1]) {
                    mCanvas.drawBitmap(buttonBitmaps[3], missionsRect.left.toFloat(), missionsRect.top.toFloat(), null)
                } else {
                    mCanvas.drawBitmap(buttonBitmaps[2], missionsRect.left.toFloat(), missionsRect.top.toFloat(), null)
                }
                if (buttonDown[2]) {
                    mCanvas.drawBitmap(buttonBitmaps[5], tutorialRect.left.toFloat(), tutorialRect.top.toFloat(), null)
                } else {
                    mCanvas.drawBitmap(buttonBitmaps[4], tutorialRect.left.toFloat(), tutorialRect.top.toFloat(), null)
                }

                dialogBox.updateDialog()
                dialogBox.draw(mCanvas)

                if (transition.transitionActive || titleStatus >= 2) {
                    transition.updateTransition()
                    transition.draw(mCanvas)
                }

                if (titleStatus == 2) {
                    // Transition over, launch Activity
                    if (!transition.transitionActive) {
                        val intent: Intent = Intent(mContext, MainActivity::class.java)
                        val puzzleSize: Int = when(dialogBox.getRadialSelection()) {
                            0 -> 4
                            1 -> 5
                            else -> 6
                        }
                        val isExpert: Boolean = dialogBox.getCheckboxValues()[0]
                        getRandomPuzzle(puzzleSize, isExpert, intent)
                        intent.putExtra("transitionType", transition.transitionType)
                        intent.putExtra("returnScreen", 0)

                        mContext.startActivity(intent)
                    }
                } else if (titleStatus == 3) {
                    if (!transition.transitionActive) {
                        val intent: Intent = Intent(mContext, MissionsActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        mContext.startActivity(intent)
                    }
                } else if (titleStatus == 4) {
                    if (!transition.transitionActive) {
                        val intent: Intent = Intent(mContext, TutorialActivity::class.java)
                        intent.putExtra("transitionType", transition.transitionType)
                        mContext.startActivity(intent)
                    }
                } else if (titleStatus == 5 && !dialogBox.dialogActive) {
                    titleStatus = 0
                }

                mPath.rewind()
                mCanvas.restore()
                mSurfaceHolder.unlockCanvasAndPost(mCanvas)
            }
        }
    }

    fun pause(softPause: Boolean = false) {
        mRunning = false
        SoundManager.pauseMusic(0)
        try {
            mGameThread.join()
        } catch (e: InterruptedException) {
        }
    }

    fun resume(softPause: Boolean = false) {
        mRunning = true
        if (settingsInitialized) {
            SoundManager.resumeMusic(0)
        }
        mGameThread = Thread(this)
        mGameThread.start()
    }
}
