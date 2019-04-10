package com.dronami.brightcitylights

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import kotlin.random.Random

class TutorialActivity : AppCompatActivity() {

    lateinit var tutorialScreen: TutorialScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        // Fullscreen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_tutorial)

        tutorialScreen = findViewById(R.id.maintutorial)
        var transitionType: Int = -1
        if (intent != null) {
            transitionType = intent.getIntExtra("transitionType", 0)
        }
        tutorialScreen.initTutorialScreen(this, transitionType)
    }

    override fun onPause() {
        super.onPause()
        tutorialScreen.pause()
    }

    override fun onResume() {
        super.onResume()
        tutorialScreen.resume()
    }
}

class TutorialScreen : SurfaceView, Runnable {
    private lateinit var mContext: Context
    private lateinit var mSurfaceHolder: SurfaceHolder
    private lateinit var mPath: Path
    private lateinit var mGameThread: Thread
    private var mRunning: Boolean = false
    private lateinit var mCanvas: Canvas
    private var tutorialStatus: Int = 0
    private var transitionType = 0

    private var mViewHeight: Int = 0
    private var mViewWidth: Int = 0
    private var baseWidth: Float = 1080f
    private val sideMarginBase: Int = 60

    private lateinit var playArea: Rect
    private var windowSize: Int = 0
    private var windowsList: MutableList<CityWindow> = mutableListOf()
    private var windowMarginPercent: Float = 0.04f
    private val numRows: Int = 4

    private var buttonDown: Boolean = false
    private lateinit var okButtonUp: Bitmap
    private lateinit var okButtonDown: Bitmap
    private lateinit var buttonRect: Rect

    private val bgColor: Int = Color.rgb(30, 30, 80)
    private val textPaint: Paint = Paint()
    private val shadowPaint: Paint = Paint()
    private var shadowOffset: Int = 0
    private val shadowOffsetRatio: Float = 0.01f
    private lateinit var textFont: Typeface
    private var textLines: MutableList<String> = mutableListOf()
    private var textXs: MutableList<Float> = mutableListOf()
    private var textYs: MutableList<Float> = mutableListOf()
    private var lineHeight: Int = 0
    private var textTop: Int = 0

    private lateinit var cursorUpBitmap: Bitmap
    private lateinit var cursorDownBitmap: Bitmap
    private var cursorStatus: Int = 0
    private var cursorX: Float = 0f
    private var cursorY: Float = 0f
    private var nextWindow: Int = 0
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var targetX: Float = 0f
    private var targetY: Float = 0f
    private var cursorCounter: Int = 0
    private val cursorDurations: List<Float> = listOf(40f, 20f, 40f, 30f)
    private var cursorRatio: Float = 0f

    private lateinit var transition: Transition

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    fun initTutorialScreen(_context: Context, tType: Int) {
        mContext = _context
        mSurfaceHolder = holder
        mPath = Path()

        transitionType = tType
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewHeight = h
        mViewWidth = w
        val widthRatio: Float = mViewWidth.toFloat() / baseWidth
        val sideMargin = (sideMarginBase * widthRatio).toInt()

        val playAreaSize: Int = mViewWidth - sideMargin * 2
        playArea = Rect(sideMargin, 0,
            mViewWidth-sideMargin, playAreaSize)

        // Setup windows
        initWindows(numRows, ColorManager.getSpecificColor(0, 2), ColorManager.getSpecificColor(0, 3))

        val cUp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cursor_up)
        cursorUpBitmap = BitmapScaler.scaleBitmap(cUp, windowSize)
        val cDown: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cursor_down)
        cursorDownBitmap = BitmapScaler.scaleBitmap(cDown, windowSize)
        cursorX = mViewWidth/2.0f
        cursorY = mViewHeight * 0.25f

        // Setup text
        textFont = Typeface.createFromAsset(context.assets, "futur.otf")
        textPaint.setTypeface(textFont)
        textPaint.setARGB(255, 240, 240, 240)
        textPaint.textSize = mViewWidth / 17f
        textPaint.flags = Paint.ANTI_ALIAS_FLAG

        shadowOffset = (mViewWidth * shadowOffsetRatio).toInt()
        shadowPaint.setTypeface(textFont)
        shadowPaint.textSize = mViewWidth / 17f
        shadowPaint.flags = Paint.ANTI_ALIAS_FLAG
        lineHeight = mViewHeight / 20
        textTop = mViewHeight / 20

        textLines.add("Tap windows to flip lights.")
        textLines.add("Adjacent windows will flip.")
        textLines.add("Turn on all the lights to win.")
        textLines.add("")
        textLines.add("That's it! Good luck!")

        for (t in 0 until textLines.count()) {
            textXs.add((mViewWidth - textPaint.measureText(textLines[t]))/2.0f)
            textYs.add(textTop + (playArea.bottom + t * lineHeight).toFloat())
        }

        // Setup button
        val buttonWidth: Int = (mViewWidth * 0.4f).toInt()
        val okBUp: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_ok_up)
        okButtonUp = BitmapScaler.scaleBitmap(okBUp, buttonWidth)
        val okBDown: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.button_ok_down)
        okButtonDown = BitmapScaler.scaleBitmap(okBDown, buttonWidth)
        val buttonX: Int = ((mViewWidth/2.0f) - okButtonUp.width / 2.0f).toInt()
        val buttonY: Int = (mViewHeight - okButtonUp.height * 0.8f).toInt()
        buttonRect = Rect(buttonX, buttonY, buttonX + okButtonUp.width, buttonY + okButtonUp.height)

        // Setup transition
        transition = Transition(mViewWidth, mViewHeight, transitionType)
        transition.initTransition(transitionType)
        transition.startTransition(false)

        setNextTarget()

        SoundManager.resumeMusic(0)
    }

    private fun setNextTarget() {
        nextWindow = Random.nextInt(0, windowsList.count())
        val nextPoint: Point = windowsList[nextWindow].getCenter()
        startX = cursorX
        startY = cursorY
        targetX = nextPoint.x.toFloat()
        targetY = nextPoint.y.toFloat()
        cursorStatus = 0
        cursorCounter = 0
    }

    private fun updateCursor() {
        if (cursorCounter > cursorDurations[cursorStatus]) {
            cursorStatus++
            if (cursorStatus >= cursorDurations.count()) {
                setNextTarget()
            } else if (cursorStatus == 2) {
               toggleWindows(nextWindow)
            } else {
                cursorCounter = 0
            }
        } else {
            if (cursorStatus == 0) {
                cursorRatio = cursorCounter / cursorDurations[cursorStatus]
                cursorX = startX + (targetX - startX) * cursorRatio
                cursorY = startY + (targetY - startY) * cursorRatio
            }

            cursorCounter++
        }
    }

    private fun toggleWindows(index: Int) {
        windowsList[index].toggle()
        SoundManager.playSound(SoundManager.Sounds.LIGHT_SELECT.value)
        // Up
        if (index-numRows >= 0) {
            windowsList[index-numRows].toggle()
        }
        // Right
        if (index % numRows != numRows-1 && index + 1 < (numRows*numRows)) {
            windowsList[index+1].toggle()
        }
        // Down
        if (index + numRows < (numRows*numRows)) {
            windowsList[index+numRows].toggle()
        }
        // Left
        if (index % numRows != 0 && index-1 >= 0) {
            windowsList[index-1].toggle()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (tutorialStatus == 0) {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    if (!buttonDown && buttonRect.contains(event.x.toInt(), event.y.toInt())) {
                        buttonDown = true
                        SoundManager.playSound(SoundManager.Sounds.SWITCH2.value)
                    } else if (buttonDown && !buttonRect.contains(event.x.toInt(), event.y.toInt())) {
                        buttonDown = false
                        SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (buttonRect.contains(event.x.toInt(), event.y.toInt())) {
                        buttonDown = false
                        tutorialStatus = 1
                        transition.startTransition(true)
                        SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                    }
                }
            }

            return true
        }

        return false
    }

    private fun initWindows(numRows: Int, fColor: Int, sColor: Int) {
        CityWindow.initSavedColors()
        val windowMarginSize = (playArea.width() * windowMarginPercent).toInt()
        windowSize = (playArea.width() - (windowMarginSize * (numRows+1))) / numRows
        val silhouetteSheet: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.silhouettesheet)
        CityWindow.initSilhouettes(silhouetteSheet, windowSize)

        for (r in 0 until numRows) {
            for (c in 0 until numRows) {
                val x = playArea.left + ((1+c) * windowMarginSize) + (windowSize * c)
                val y = playArea.top + ((1+r) * windowMarginSize) + (windowSize * r)
                val window: CityWindow = CityWindow( (r*numRows)+c, x, y, windowSize, fColor, sColor)
                windowsList.add(window)
            }
        }
    }

    override fun run() {
        while(mRunning) {
            if (holder.surface.isValid) {
                mCanvas = holder.lockCanvas()
                mCanvas.save()
                mCanvas.drawColor(bgColor)

                for (window in windowsList) {
                    window.update()
                    window.drawWindow(mCanvas)
                }
                for (l in 0 until textLines.count()) {
                    mCanvas.drawText(textLines[l], textXs[l] + shadowOffset, textYs[l] + shadowOffset, shadowPaint)
                    mCanvas.drawText(textLines[l], textXs[l], textYs[l], textPaint)
                }
                if (buttonDown) {
                    mCanvas.drawBitmap(okButtonDown, buttonRect.left.toFloat(), buttonRect.top.toFloat(), null)
                } else {
                    mCanvas.drawBitmap(okButtonUp, buttonRect.left.toFloat(), buttonRect.top.toFloat(), null)
                }


                updateCursor()
                if (cursorStatus == 2) {
                    mCanvas.drawBitmap(cursorDownBitmap, cursorX, cursorY, null)
                } else {
                    mCanvas.drawBitmap(cursorUpBitmap, cursorX, cursorY, null)
                }

                if (transition.transitionActive) {
                    transition.updateTransition()
                    transition.draw(mCanvas)
                }

                if (tutorialStatus == 1 && !transition.transitionActive) {
                    val intent: Intent = Intent(mContext, TitleScreenActivity::class.java)
                    intent.putExtra("transitionType", transition.transitionType)
                    mContext.startActivity(intent)
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
        SoundManager.resumeMusic(0)
        mGameThread = Thread(this)
        mGameThread.start()
    }
}
