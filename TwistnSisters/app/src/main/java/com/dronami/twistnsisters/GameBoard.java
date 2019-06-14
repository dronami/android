package com.dronami.twistnsisters;

import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Explosion {
    static GameBoard gameBoard;
    int color;
    Rect explosionRect;

    public boolean isFinished = false;

    static final float EXPLOSION_DURATION = 0.3f;
    float frameDuration = EXPLOSION_DURATION / NUM_FRAMES;
    float frameCounter = 0.0f;

    int currentFrame = 0;
    static final int NUM_FRAMES = 16;
    static final int NUM_SHEET_ROWS = 4;
    static final int NUM_SHEET_COLS = 4;

    static boolean frameRectsInitialized = false;
    static Rect frameSrcRects[] = new Rect[NUM_SHEET_ROWS * NUM_SHEET_COLS];

    Explosion(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        if (!frameRectsInitialized) {
            int frameSize = gameBoard.explosionSheet.getWidth() / NUM_SHEET_COLS;
            for (int r = 0; r < NUM_SHEET_ROWS; r++) {
                for (int c = 0; c < NUM_SHEET_COLS; c++) {
                    Rect frameRect = new Rect(frameSize * c, frameSize * r,
                            (frameSize * c) + frameSize, (frameSize * r) + frameSize);
                    frameSrcRects[(r * NUM_SHEET_COLS) + c] = frameRect;
                }
            }
        }
    }

    public void initializeExplosion(int color, Rect explosionRect) {
        this.color = color;
        this.explosionRect = explosionRect;
        currentFrame = 0;
        frameCounter = 0.0f;
        isFinished = false;
    }

    public void update(float deltaTime) {
        frameCounter += deltaTime;
        if (frameCounter >= frameDuration) {
            currentFrame++;
            frameCounter -= frameDuration;
            if (currentFrame >= NUM_FRAMES) {
                isFinished = true;
            }
        }
    }

    public void draw(Graphics g) {
        g.drawPixmapColorized(gameBoard.explosionSheet, explosionRect, frameSrcRects[currentFrame], color);
    }
}

class PointBubble {
    static float fontSize;
    static boolean fontInitialized = false;
    static Paint fontPaint = new Paint();
    static Paint shadowPaint = new Paint();
    static GameBoard gameBoard;
    static final float margin = 0.1f;
    int color;
    Rect pbRect;
    String pointsString;
    static float textHeight;

    int x;
    int y;
    static int shadowOffset;

    public boolean isFinished = false;

    static final float POINT_BUBBLE_DURATION = 1.0f;
    float pointBubbleCounter = 0.0f;

    public PointBubble(GameBoard gameBoard, int color) {
        this.gameBoard = gameBoard;

        pointBubbleCounter = 0.0f;
        if (!fontInitialized) {
            this.color = color;
            fontSize = gameBoard.fontManager.getBiggestFontSizeByWidth(0, (int)(gameBoard.tileSize * (1.0f - margin * 2)), "+99");
            textHeight = gameBoard.fontManager.getTextHeight(0, fontSize, "+99");
            fontPaint.setTypeface(gameBoard.fontManager.getTypeface(0));
            fontPaint.setTextSize(fontSize);
            fontPaint.setColor(color);
            fontPaint.setAntiAlias(true);
            shadowPaint.set(fontPaint);
            shadowPaint.setColor(Color.BLACK);
            fontInitialized = true;
            shadowOffset = (int)(gameBoard.tileSize * 0.05f);
        }
    }

    public void initializePointBubble(Rect pbRect, int points) {
        this.pbRect = pbRect;
        pointsString = "+"+points;
        pointBubbleCounter = 0.0f;
        isFinished = false;

        float textWidth = gameBoard.fontManager.getTextWidth(0, fontSize, pointsString);
        x = pbRect.left + (int)((gameBoard.tileSize - textWidth)/2);
        y = pbRect.top + (gameBoard.tileSize/2) + (int)(textHeight / 2);
    }

    public void update(float deltaTime) {

        pointBubbleCounter += deltaTime;
        if (pointBubbleCounter > POINT_BUBBLE_DURATION) {
            isFinished = true;
        }
    }

    public void draw(Graphics g) {
        g.drawText(pointsString, x + shadowOffset, y + shadowOffset, shadowPaint);
        g.drawText(pointsString, x, y, fontPaint);
    }
}


public class GameBoard {
    static enum BoardState {
        Active, Cascading, TwistaSquashing, Paused
    }
    BoardState boardState;
    BoardState savedState;
    Game game;
    FontManager fontManager;

    int gemColorIndex;

    public static final int NUM_TILES_X = 4;
    public static final int NUM_TILES_Y = 8;
    public static final int NUM_GEM_TYPES = 4;
    public static final int NUM_TWISTA_TYPES = 3;
    Rect[][] tileRects;
    public int tileSize;
    Rect playArea;

    private int score = 0;
    private long gameStartTime = 0;
    private long timeElapsed = 0;
    private long savedTime = 0;

    ArrayList<Gem> dropperGems = new ArrayList<Gem>();
    ArrayList<Gem> fallingGems = new ArrayList<Gem>();

    private int gemCount;
    Gem[][] boardGems = new Gem[NUM_TILES_X][NUM_TILES_Y];
    Pixmap[] gemPixmaps = new Pixmap[NUM_GEM_TYPES];
    Pixmap gemSheet;
    Pixmap twistaSheet;
    Pixmap[] twistaPixmaps = new Pixmap[NUM_GEM_TYPES];
    public Rect gemSrcRect;

    public Pixmap explosionSheet;
    float explosionMargin = 0.6f;
    private ArrayList<Explosion> explosions = new ArrayList<Explosion>();
    private Pool<Explosion> explosionPool;

    private ArrayList<PointBubble> pointBubbles = new ArrayList<PointBubble>();
    private Pool<PointBubble> pointBubblePool;

    // Tile shit
    Pixmap bgPixmap;
    Paint tilePaint;
    BitmapShader tileShader;
    Matrix tileMatrix;
    final static float COLUMNN_ALPHA_MAX = 0.2f;
    int columnSeletedColors[] = { 0, 0, 0, 0 };
    float columnSelectedCounter[] = {0.0f, 0.0f, 0.0f, 0.0f};
    final static float COLUMN_SELECTED_DURATION = 0.2f;

    Rect bufferArea;
    Rect[] columnRects = new Rect[NUM_TILES_X];
    int selectedColumn = -1;

    // Game logic shit
    boolean isDropping = false;
    final static float DROPPING_DURATION = 1.2f;
    float droppingCounter = 0.0f;

    boolean isTwisting = false;
    final static float TWIST_DURATION = 0.15f;
    float twistCounter = 0.0f;
    int twistStartColumn = 0;
    ArrayList<Gem> twistGemsLeft = new ArrayList<Gem>();
    ArrayList<Gem> twistGemsRight = new ArrayList<Gem>();
    int twistLeftX;
    int twistRightX;
    int twistLeftCol;
    int twistRightCol;

    boolean fastFalling = false;
    int fastFallFrame = 0;
    float fastFallCounter = 0.0f;
    float fastFallFrameDuration = 0.2f;
    int fastFallFrameCount = 3;

    long lastTouchTime = -1;
    long doubleTapWindow = 200000000;

    ArrayList<Gem> cascadeGems = new ArrayList<Gem>();
    final static float CASCADE_DURATION = 0.8f;
    float cascadeCounter = 0.0f;
    int cascadeRow = 0;
    boolean cascadeHorizontal = false;

    Gem twistaTop;
    Rect twistaStartRect = new Rect();
    ArrayList<Gem> twistaGems = new ArrayList<Gem>();
    final static float TWISTA_DURATION = 0.2f;
    float twistaCounter = 0.0f;
    int curTwista = 0;
    int twistaCount = 0;
    int twistaBottomRow = 0;
    final static float TWISTOUT_DURATION = 0.8f;

    private float GEMS_PER_LEVEL = 2.4f;
    private int gameMode = 0;
    private int gameLevel = 0;

    private int speedLevel = 6;
    private int speedCounter = 0;
    private int speedDuration = 20;
    float droppingSpeeds[] = {
            200.0f,
            250.0f,
            300.0f,
            350.0f,
            400.0f,
            450.0f,
            500.0f,
            550.0f,
            600.0f,
            650.0f,
            700.0f,
            750.0f,
            800.0f
    };

    Mascot mascot;

    public GameBoard(Rect playArea, Game game, Mascot mascot,
                     int gameMode, int gameLevel, int speedLevel) {
        tileSize = playArea.width() / NUM_TILES_X;
        this.playArea = playArea;
        this.fontManager = game.getFontManager();

        this.gameMode = gameMode;
        this.gameLevel = gameLevel;
        this.speedLevel = speedLevel * 4;
        this.mascot = mascot;

        final GameBoard gb = this;
        Pool.PoolObjectFactory<Explosion> factory = new Pool.PoolObjectFactory<Explosion>() {
            @Override
            public Explosion createObject() {
                return new Explosion(gb);
            }
        };

        explosionPool = new Pool<Explosion>(factory, NUM_TILES_X * (NUM_TILES_Y+1));

        Pool.PoolObjectFactory<PointBubble> pbFactory = new Pool.PoolObjectFactory<PointBubble>() {
            @Override
            public PointBubble createObject() {
                return new PointBubble(gb, Game.ColorManager.twistaColor);
            }
        };

        pointBubblePool = new Pool<PointBubble>(pbFactory, NUM_TILES_X * (NUM_TILES_Y+1));

        // +1 for buffer row at top
        tileRects = new Rect[NUM_TILES_X][NUM_TILES_Y + 1];
        for (int y = 0; y < NUM_TILES_Y + 1; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                // Generate tiles from bottom of screen to top
                tileRects[x][GameBoard.NUM_TILES_Y-y] = new Rect(playArea.left + x * tileSize, playArea.top + y * tileSize,
                        playArea.left + (x * tileSize) + tileSize, playArea.top + (y * tileSize) + tileSize);
            }
        }

        bufferArea = new Rect(playArea.left, playArea.top, playArea.right, playArea.top + tileSize);

        for (int p = 0; p < NUM_GEM_TYPES; p++) {
            gemPixmaps[p] = game.getGraphics().newScaledPixmap("gem-"+p+".png",
                    Graphics.PixmapFormat.ARGB4444, tileSize, false);
        }

        for (int t = 0; t < NUM_TWISTA_TYPES; t++) {
            twistaPixmaps[t] = game.getGraphics().newScaledPixmap("twista-"+t+".png",
                    Graphics.PixmapFormat.ARGB4444, tileSize, false);
        }

        gemSheet = game.getGraphics().newScaledPixmap("gems-a.png",
                Graphics.PixmapFormat.ARGB4444, tileSize * 2, false);
        twistaSheet = game.getGraphics().newScaledPixmap("twistas.png",
                Graphics.PixmapFormat.ARGB4444, tileSize * 2, false);
        bgPixmap = game.getGraphics().newScaledPixmap("bg-grass.png",
                Graphics.PixmapFormat.ARGB4444, playArea.width(), true);
        Pixmap tileBG = game.getGraphics().newScaledPixmap("tilebg.png",
                Graphics.PixmapFormat.RGB565, tileSize, false);
        tilePaint = new Paint();
        tileShader = new BitmapShader(((AndroidPixmap)tileBG).bitmap,
                BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
        tilePaint.setShader(tileShader);
        tileMatrix = new Matrix();
        tileMatrix.postTranslate(playArea.left, playArea.top);
        tileShader.setLocalMatrix(tileMatrix);

        explosionSheet = game.getGraphics().newPixmap("explosion-sheet.png",
                Graphics.PixmapFormat.ARGB4444);

        gemSrcRect = new Rect(0, 0, tileSize, tileSize);

        clearBoard();
        spawnDroppers();



        for (int c = 0; c < NUM_TILES_X; c++) {
            Rect columnRect = new Rect(playArea.left + c * tileSize, playArea.top + tileSize,
                    playArea.left + (c * tileSize) + tileSize, playArea.bottom);
            columnRects[c] = columnRect;
        }

        fillBoard();
//        int[][] boardPattern = {
//                {0, 0, 0, -1},
//                {2, 2, 2, -1},
//                {-1, -1, -1, -1},
//                {-1, -1, -1, -1},
//                {-1, -1, -1, -1},
//                {-1, -1, -1, -1},
//                {-1, -1, -1, -1},
//                {-1, -1, -1, -1}
//        };
//
//        initBoardPattern(boardPattern);
//        printBoard();

        boardState = BoardState.Active;

        score = 0;
        gameStartTime = System.currentTimeMillis();
    }

    private void fillBoard() {
        int numGems = (int)(gameLevel * GEMS_PER_LEVEL);
        Log.d("Assy", "NUMGEMS: "+numGems);
        int[][] boardPattern = {
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1}
        };

        Random random = new Random();
        ArrayList<Integer> typeCandidates = new ArrayList<>();
        for (int y = 0; y < NUM_TILES_Y && numGems > 0; y++) {
            for (int x = 0; x < NUM_TILES_X && numGems > 0; x++) {
                typeCandidates.clear();
                for (int g = 0; g < NUM_GEM_TYPES; g++) {
                    typeCandidates.add(g);
                }

                if (x > 0) {
                    typeCandidates.remove(new Integer(boardPattern[y][x-1]));
                }
                if (y > 0) {
                    typeCandidates.remove(new Integer(boardPattern[y-1][x]));
                }

                int randIndex = random.nextInt(typeCandidates.size());
                int randType = typeCandidates.get(randIndex);
                boardPattern[y][x] = randType;
                Log.d("Assy", x+", "+y+": "+" = "+randType);

                numGems--;
            }
        }

        for (int y = 0; y < NUM_TILES_Y; y++) {
            String rowString = "";
            for (int x = 0; x < NUM_TILES_X; x++) {
                rowString += boardPattern[y][x]+", ";
            }
            Log.d("Assy", rowString);
        }
        initBoardPattern(boardPattern);
    }

    public int getScore() {
        return score;
    }

    public String getScoreString() {
        StringBuilder scoreString = new StringBuilder();
        int tempScore = Math.min(score, 9999);
        if (score < 1000) {
            scoreString.append("0");
        }
        if (score < 100) {
            scoreString.append("0");
        }
        if (score < 10) {
            scoreString.append("0");
        }
        scoreString.append(tempScore);

        return scoreString.toString();
    }

    public String getTimeString() {
        StringBuilder timeString = new StringBuilder();
        int seconds = (int)(timeElapsed / 1000) % 60;
        int minutes = (int)(timeElapsed / 60000) % 60;

        timeString.append(minutes);
        timeString.append(":");
        if (seconds < 10) {
            timeString.append("0");
        }
        timeString.append(seconds);

        return timeString.toString();
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    private void printBoard() {
        for (int y = NUM_TILES_Y-1; y >= 0; y--) {
            String rowString = "["+y+"] ";
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (boardGems[x][y] != null) {
                    rowString += boardGems[x][y].gemType + ", ";
                } else {
                    rowString += "-1, ";
                }

            }
            Log.d("PrintBoard", rowString+"\n");
        }
    }

    private void clearBoard() {
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                boardGems[x][y] = null;
            }
        }
    }

    private void shiftBoardDown(int row) {
        for (int r = row; r < NUM_TILES_Y - 1; r++) {
            for (int c = 0; c < 4; c++) {
                boardGems[c][r] = boardGems[c][r+1];
                boardGems[c][r+1] = null;
                if (boardGems[c][r] != null) {
                    boardGems[c][r].row--;
                }
            }
        }
    }

    private void shiftCascadeDown() {
        for (int c = 0; c < cascadeGems.size(); c++) {
            Gem curGem = cascadeGems.get(c);
            boardGems[curGem.column][curGem.row] = null;
            curGem.row -= 2;
            boardGems[curGem.column][curGem.row] = curGem;
            curGem.setCascading(false);
        }

        cascadeGems.clear();
    }

    private void incGemCount() {
        gemCount++;
        checkMascotState();
    }

    private void decGemCount() {
        gemCount--;
        checkMascotState();
    }

    private void checkMascotState() {
        Log.d("Assy", "GemCount: "+gemCount);
        if (gemCount < (NUM_TILES_X * NUM_TILES_Y)/4) {
            mascot.setFaceState(Mascot.FaceState.Happy);
        } else if (gemCount > (NUM_TILES_X * NUM_TILES_Y)*3/4) {
            mascot.setFaceState(Mascot.FaceState.Sad);
        } else {
            mascot.setFaceState(Mascot.FaceState.Normal);
        }
    }

    private void initBoardPattern(int[][] boardPattern) {
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (boardPattern[y][x] != -1) {
                    Log.d("PrintBoard", "Adding gem: "+boardPattern[y][x]);
                    Gem newGem = new Gem(boardPattern[y][x], x, new Rect(tileRects[x][y]), this);
                    newGem.row = y;
                    newGem.column = x;
                    newGem.currentState = Gem.GemState.Landed;
                    boardGems[x][y] = newGem;
                    incGemCount();
                }
            }
        }
    }

    public void draw(Graphics g) {
        drawBG(g);
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (boardGems[x][y] != null
                        && boardGems[x][y].currentState != Gem.GemState.Landing
                        && !boardGems[x][y].getIsCascading()) {
                    if (!boardGems[x][y].isTwisting) {
                        boardGems[x][y].draw(g);
                    }
                }
            }
        }

        for (int d = 0; d < dropperGems.size(); d++) {
            dropperGems.get(d).draw(g);
        }

        for (int f = 0; f < fallingGems.size(); f++) {
            fallingGems.get(f).draw(g);
        }

        if (boardState == BoardState.Cascading) {
            for (int c = 0; c < cascadeGems.size(); c++) {
                cascadeGems.get(c).draw(g);
            }
        } else if (boardState == BoardState.TwistaSquashing) {
            twistaTop.draw(g);
        }

        for (int e = 0; e < explosions.size(); e++) {
            explosions.get(e).draw(g);
        }

        for (int p = 0; p < pointBubbles.size(); p++) {
            pointBubbles.get(p).draw(g);
        }

        if (isTwisting) {
            for (int l = 0; l < twistGemsLeft.size(); l++) {
                twistGemsLeft.get(l).draw(g);
            }

            for (int r = 0; r < twistGemsRight.size(); r++) {
                twistGemsRight.get(r).draw(g);
            }
        }
        Paint countPaint = new Paint();
        countPaint.setColor(Color.WHITE);
        countPaint.setTextSize(30);
        g.drawText(""+gemCount, 200, 200, countPaint);
    }

    private void drawBG(Graphics g) {
        g.drawRect(bufferArea, tilePaint);
        g.drawPixmap(bgPixmap, playArea.left, playArea.top+tileSize);

        for (int x = 0; x < NUM_TILES_X; x++) {
            if (columnSelectedCounter[x] > 0.0f) {
                g.drawRect(columnRects[x], columnSeletedColors[x]);
            }
        }
    }

    private void spawnDroppers() {
        // Increase speed level
        speedCounter++;
        if (speedCounter > speedDuration) {
            speedCounter = 0;
            speedLevel++;
            if (speedLevel >= droppingSpeeds.length) {
                speedLevel = droppingSpeeds.length - 1;
            }
        }

        Random rand = new Random();
        int typeA = rand.nextInt(6);
        int typeB = rand.nextInt(6);

        ArrayList<Integer> freeColumns = new ArrayList<Integer>();
        for (int c = 0; c < NUM_TILES_X; c++) {
            freeColumns.add(c);
        }
        int randIndex = rand.nextInt(freeColumns.size());
        int columnA = freeColumns.get(randIndex);
        freeColumns.remove(randIndex);

        randIndex = rand.nextInt(freeColumns.size());
        int columnB = freeColumns.get(randIndex);
        freeColumns.remove(randIndex);

        Gem gemA = new Gem(typeA, columnA, new Rect(tileRects[columnA][NUM_TILES_Y]), this);
        Gem gemB = new Gem(typeB, columnB, new Rect(tileRects[columnB][NUM_TILES_Y]), this);
        gemA.startSpinning(true, DROPPING_DURATION);
        gemB.startSpinning(true, DROPPING_DURATION);

        gemA.setDropperSpeed(droppingSpeeds[speedLevel]);
        gemB.setDropperSpeed(droppingSpeeds[speedLevel]);
        dropperGems.add(gemA);
        dropperGems.add(gemB);

        isDropping = true;
        droppingCounter = 0.0f;
    }

    private void updateDroppers(float deltaTime) {
        for (int d = 0; d < dropperGems.size(); d++) {
            dropperGems.get(d).update(deltaTime);
        }
    }

    private void onGemLanding(Gem gem, int row) {
        gem.stopSquashing();
        gem.startLanding(row);
        if (row < NUM_TILES_Y) {
            boardGems[gem.column][gem.row] = gem;
            gem.gemRect.set(tileRects[gem.column][gem.row]);
            incGemCount();
        } else {
            Log.e("DronamiError", "Game over");
        }
    }

    private void clearGem(Gem gem, int multiplier) {
        int points = 1 * multiplier;
        Rect curRect;
        curRect = tileRects[gem.column][gem.row];
        if (gem.row < NUM_TILES_Y) {
            if (boardGems[gem.column][gem.row] != null) {
                boardGems[gem.column][gem.row] = null;
                decGemCount();
            }
        }
        if (gem.gemType < NUM_GEM_TYPES) {
            initExplosion(curRect, Game.ColorManager.gemColorSets[gemColorIndex][gem.gemType]);
        } else {
            initExplosion(curRect, Game.ColorManager.twistaColor);
        }
        if (gem.gemType < NUM_GEM_TYPES) {
            initPointBubble(curRect, points);
            score += points;
        }

        mascot.setFaceState(Mascot.FaceState.Ecstatic);
    }

    private boolean isRowMatch(int row) {
        if (row >= NUM_TILES_Y || boardGems[0][row] == null) {
            return false;
        } else {
            int matchType = boardGems[0][row].gemType;
            for (int c = 1; c < NUM_TILES_X; c++) {
                if (boardGems[c][row] == null || boardGems[c][row].gemType != matchType) {
                    return false;
                }
             }
        }

        return true;
    }

    private void explodeRow(int row, int gemType) {
        for (int c = 0; c < NUM_TILES_X; c++) {
            if (boardGems[c][row] != null) {
                clearGem(boardGems[c][row], 4);
            }
        }
    }

    private boolean startCascadeColumn(int row, int column) {
        for (int r = row; r < NUM_TILES_Y; r++) {
            if (boardGems[column][r] != null) {
                cascadeGems.add(boardGems[column][r]);
                boardGems[column][r].setCascading(true);
            }
        }

        if (cascadeGems.size() == 0) {
            return false;
        }

        cascadeCounter = 0.0f;
        cascadeHorizontal = false;
        setFastFalling(false);
        for (int f = 0; f < fallingGems.size(); f++) {
            fallingGems.get(f).stopSquashing();
        }

        return true;
    }

    private boolean startCascadeRow(int row) {
        for (int r = row; r < NUM_TILES_Y; r++) {
            for (int c = 0; c < NUM_TILES_X; c++) {
                if (boardGems[c][r] != null) {
                    boardGems[c][r].gemRect.set(tileRects[c][r]);
                    boardGems[c][r].setCascading(true);
                    cascadeGems.add(boardGems[c][r]);
                }
            }
        }

        if (cascadeGems.size() == 0) {
            return false;
        }

        cascadeCounter = 0.0f;
        cascadeRow = row;
        cascadeHorizontal = true;
        setFastFalling(false);
        for (int f = 0; f < fallingGems.size(); f++) {
            fallingGems.get(f).stopSquashing();
        }

        return true;
    }

    private void updateCascade(float deltaTime) {
        float ratio = cascadeCounter / CASCADE_DURATION;

        for (int d = 0; d < cascadeGems.size(); d++) {
            Gem curGem = cascadeGems.get(d);
            Rect baseRect = tileRects[curGem.column][curGem.row];
            if (cascadeHorizontal) {
                curGem.gemRect.set(baseRect.left, baseRect.top + (int)(ratio * tileSize),
                        baseRect.right, baseRect.bottom + (int)(ratio * tileSize));
            } else {
                curGem.gemRect.set(baseRect.left, baseRect.top + (int)(ratio * tileSize * 2),
                        baseRect.right, baseRect.bottom + (int)(ratio * tileSize * 2));
            }

        }

        cascadeCounter += deltaTime;
        if (cascadeCounter > CASCADE_DURATION) {
            if (cascadeHorizontal) {
                shiftBoardDown(cascadeRow - 1);
            } else {
                shiftCascadeDown();
            }
            boardState = BoardState.Active;
            for (int c = 0; c < cascadeGems.size(); c++) {
                cascadeGems.get(c).setCascading(false);
            }
            cascadeGems.clear();

            // Check to see if cascade triggered more explosions
            if (isRowMatch(cascadeRow)) {
                explodeRow(cascadeRow, boardGems[0][cascadeRow].gemType);

                if (startCascadeRow(cascadeRow)) {
                    boardState = BoardState.Cascading;
                }
            }

            for (int r = 0; r < NUM_TILES_Y - 1; r++) {
                for (int c = 0; c < NUM_TILES_X; c++) {
                    if (boardGems[c][r] != null && boardGems[c][r+1] != null
                        && boardGems[c][r].gemType == boardGems[c][r+1].gemType) {
                        clearGem(boardGems[c][r], 1);
                        clearGem(boardGems[c][r+1], 1);

                        if (startCascadeColumn(r+1, c)) {
                            boardState = BoardState.Cascading;
                        }
                    }
                }
            }

        }
    }

    private boolean checkColumnForTwista(int row, int column) {
        boolean twistaFound = false;
        twistaGems.clear();
        for (int r = row; r >= 0; r--) {
            if (boardGems[column][r] != null) {
                if (boardGems[column][r].gemType < 4) {
                    twistaGems.add(boardGems[column][r]);
                } else if (boardGems[column][r].gemType == 5) {
                    twistaGems.add(boardGems[column][r]);
                    twistaBottomRow = r;
                    twistaFound = true;
                    break;
                }
            }
        }

        if (twistaFound) {
            twistaCount = twistaGems.size();
            curTwista = 0;
            twistaCounter = 0.0f;

            setFastFalling(false);
            for (int f = 0; f < fallingGems.size(); f++) {
                fallingGems.get(f).stopSquashing();
            }
        }

        return twistaFound;
    }

    private void updateTwista(float deltaTime) {
        if (twistaTop.currentState == Gem.GemState.Hidden) {
            initExplosion(tileRects[twistaTop.column][twistaBottomRow],
                    Game.ColorManager.twistaColor);
            boardState = BoardState.Active;
            twistaTop = null;
        } else if (twistaTop.currentState == Gem.GemState.SpinningOut) {
            twistaTop.update(deltaTime);
        } else {
            float ratio = twistaCounter / TWISTA_DURATION;
            twistaTop.gemRect.set(twistaStartRect.left, (int) (twistaStartRect.top + (ratio * tileSize)),
                    twistaStartRect.right, (int) (twistaStartRect.bottom + (ratio * tileSize)));

            twistaCounter += deltaTime;
            if (twistaCounter > TWISTA_DURATION) {
                twistaCounter -= TWISTA_DURATION;
                curTwista++;
                if (curTwista >= twistaCount) {
                    twistaTop.currentRotation = 0.0f;
                    twistaTop.startSpinning(false, TWISTOUT_DURATION);
                    twistaTop.gemType = 6;
                    boardGems[twistaTop.column][twistaBottomRow] = null;
                    decGemCount();
                } else {
                    if (curTwista - 1 < twistaGems.size()) {
                        Gem curGem = twistaGems.get(curTwista - 1);
                        clearGem(curGem, curTwista);
                        twistaStartRect.offset(0, tileSize);
                    }
                }
            }
        }
    }

    public void update(float deltaTime) {
        if (boardState == BoardState.Active) {
            for (int d = 0; d < fallingGems.size(); d++) {
                Gem curGem = fallingGems.get(d);
                curGem.update(deltaTime);

                if (curGem.currentState == Gem.GemState.Falling) {
                    for (int r = 0; r < NUM_TILES_Y; r++) {
                        // Landing on bottom row
                        if (curGem.gemRect.bottom >= tileRects[curGem.column][0].bottom) {
                            if (curGem.gemType == NUM_GEM_TYPES) {
                                initExplosion(tileRects[curGem.column][r],
                                        Game.ColorManager.twistaColor);
                                fallingGems.remove(curGem);
                                curGem = null;
                                break;
                            } else {
                                onGemLanding(curGem, 0);
                                if (isRowMatch(0)) {
                                    explodeRow(0, curGem.gemType);
                                    fallingGems.remove(curGem);
                                    curGem = null;

                                    if (startCascadeRow(1)) {
                                        boardState = BoardState.Cascading;
                                    }
                                    break;
                                }
                            }
                        }
                        // Landing on other gems
                        else if (boardGems[curGem.column][r] != null) {
                            if (curGem.gemRect.bottom >= tileRects[curGem.column][r].top) {
                                // Check for vertical match
                                if (boardGems[curGem.column][r].gemType == curGem.gemType) {
                                    fallingGems.remove(curGem);
                                    //boardGems[curGem.column][r] = null;
                                    int curGemType = curGem.gemType;

                                    if (curGemType >= NUM_GEM_TYPES) {
                                        curGemType = NUM_GEM_TYPES;
                                    }

                                    curGem.row = r+1;
                                    clearGem(curGem, 1);
                                    clearGem(boardGems[curGem.column][r], 1);

                                    if (r+1 < NUM_TILES_Y) {
                                        boardGems[curGem.column][r + 1] = curGem;
                                        if (isRowMatch(r + 1)) {
                                            fallingGems.remove(curGem);
                                            explodeRow(r + 1, curGemType);

                                            cascadeRow = r + 2;
                                            if (startCascadeRow(cascadeRow)) {
                                                boardState = BoardState.Cascading;
                                            }
                                            boardGems[curGem.column][r + 1] = null;
                                            curGem = null;
                                        } else {
                                            boardGems[curGem.column][r + 1] = null;
                                        }
                                    }
                                } else {
                                    boolean twistaActivated = false;
                                    if (curGem.gemType == NUM_GEM_TYPES) {
                                        twistaActivated = checkColumnForTwista(r, curGem.column);
                                        if (twistaActivated) {
                                            boardState = BoardState.TwistaSquashing;
                                            twistaTop = curGem;
                                            twistaStartRect.set(curGem.gemRect);
                                            fallingGems.remove(curGem);
                                        } else {
                                            initExplosion(tileRects[curGem.column][r+1], Game.ColorManager.twistaColor);
                                        }
                                        fallingGems.remove(curGem);
                                        curGem = null;
                                    } else {
                                        onGemLanding(curGem, r + 1);
                                        if (isRowMatch(curGem.row)) {
                                            fallingGems.remove(curGem);
                                            int curGemType = curGem.gemType;
                                            if (curGemType >= NUM_GEM_TYPES) {
                                                curGemType = NUM_GEM_TYPES;
                                            }

                                            explodeRow(curGem.row, curGemType);

                                            cascadeRow = curGem.row + 1;
                                            curGem = null;
                                            if (startCascadeRow(cascadeRow)) {
                                                boardState = BoardState.Cascading;
                                            }

                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else if (curGem.currentState == Gem.GemState.Landed) {
                    fallingGems.remove(curGem);
                }
            }

            if (fallingGems.size() == 0) {
                for (int d = 0; d < dropperGems.size(); d++) {
                    dropperGems.get(d).currentState = Gem.GemState.Falling;
                    dropperGems.get(d).setFastFall(fastFalling);
                    fallingGems.add(dropperGems.get(d));
                }

                dropperGems.clear();
            }

            if (dropperGems.size() == 0) {
                spawnDroppers();
            }

            if (isDropping) {
                updateDroppers(deltaTime);
            }

            if (isTwisting) {
                updateTwisting(deltaTime);
            }

            if (fastFalling) {
                fastFallCounter += deltaTime;
                if (fastFallCounter > fastFallFrameDuration) {
                    fastFallCounter -= fastFallFrameDuration;
                    fastFallFrame++;
                    if (fastFallFrame >= fastFallFrameCount) {
                        fastFallFrame = 0;
                    }
                }
            }
        } else if (boardState == BoardState.Cascading) {
            updateCascade(deltaTime);
        } else if (boardState == BoardState.TwistaSquashing) {
            if (!isTwisting) {
                updateTwista(deltaTime);
            } else {
                updateTwisting(deltaTime);
            }
        }

        if (boardState != BoardState.Paused) {
            for (int e = explosions.size() - 1; e >= 0; e--) {
                explosions.get(e).update(deltaTime);
                if (explosions.get(e).isFinished) {
                    explosionPool.free(explosions.get(e));
                    explosions.remove(e);
                }
            }

            for (int p = pointBubbles.size() - 1; p >= 0; p--) {
                pointBubbles.get(p).update(deltaTime);
                if (pointBubbles.get(p).isFinished) {
                    pointBubblePool.free(pointBubbles.get(p));
                    pointBubbles.remove(p);
                }
            }

            timeElapsed = Math.min(savedTime + System.currentTimeMillis() - gameStartTime, 3599999);

            // Selected column stuff
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (x == selectedColumn) {
                    if (columnSelectedCounter[x] < COLUMN_SELECTED_DURATION) {
                        columnSelectedCounter[x] += deltaTime;
                        columnSelectedCounter[x] = Math.min(columnSelectedCounter[x], COLUMN_SELECTED_DURATION);
                        columnSeletedColors[x] = Color.argb((int)((columnSelectedCounter[x] / COLUMN_SELECTED_DURATION) * COLUMNN_ALPHA_MAX * 255), 255, 255, 255);
                    }
                } else if (columnSelectedCounter[x] > 0.0f) {
                    columnSelectedCounter[x] -= deltaTime;
                    columnSelectedCounter[x] = Math.max(columnSelectedCounter[x], 0.0f);
                    columnSeletedColors[x] = Color.argb((int)((columnSelectedCounter[x] / COLUMN_SELECTED_DURATION) * COLUMNN_ALPHA_MAX * 255), 255, 255, 255);
                }
            }
        }
    }

    private void startTwist(int startCol, int endCol) {

        if (startCol > endCol) {
            twistLeftCol = endCol;
            twistRightCol = startCol;
        } else {
            twistLeftCol = startCol;
            twistRightCol = endCol;
        }

        twistLeftX = tileRects[twistLeftCol][0].left;
        twistRightX = tileRects[twistRightCol][0].left;

        // Add gems to twist ArrayLists
        twistGemsLeft.clear();
        twistGemsRight.clear();
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (x == twistLeftCol) {
                    if (boardGems[x][y] != null) {
                        twistGemsLeft.add(boardGems[x][y]);
                        boardGems[x][y].isTwisting = true;
                        boardGems[x][y].column = twistLeftCol + 1;
                        boardGems[x][y].stopSquashing();
                        if (boardGems[x][y].currentState == Gem.GemState.Landing) {
                            Log.d("Assy", "Landing twist");
                        }
                    }
                } else if (x == twistRightCol) {
                    if (boardGems[x][y] != null) {
                        twistGemsRight.add(boardGems[x][y]);
                        boardGems[x][y].isTwisting = true;
                        boardGems[x][y].column = twistRightCol - 1;
                        boardGems[x][y].stopSquashing();
                        if (boardGems[x][y].currentState == Gem.GemState.Landing) {
                            Log.d("Assy", "Landing twist");
                        }
                    }
                }
            }
        }

        // Calculate column heights to determine if droppers are offset
        int leftColumnHeight = tileRects[0][0].bottom;
        int rightColumnHeight = tileRects[0][0].bottom;
        if (twistGemsLeft.size() > 0) {
            // Last gem is topmost
            leftColumnHeight = twistGemsLeft.get(twistGemsLeft.size()-1).gemRect.top;
        }
        if (twistGemsRight.size() > 0) {
            // Last gem is topmost
            rightColumnHeight = twistGemsRight.get(twistGemsRight.size()-1).gemRect.top;
        }

        // Update board to post-twist state to ensure collision detection works properly
        if (twistGemsLeft.size() > twistGemsRight.size()) {
            for (int l = 0; l < twistGemsLeft.size(); l++) {
                Gem curGem = twistGemsLeft.get(l);
                Gem tempGem = boardGems[twistRightCol][curGem.row];
                boardGems[twistRightCol][curGem.row] = curGem;
                boardGems[twistLeftCol][curGem.row] = tempGem;
            }
        } else {
            for (int r = 0; r < twistGemsRight.size(); r++) {
                Gem curGem = twistGemsRight.get(r);
                Gem tempGem = boardGems[twistLeftCol][curGem.row];
                boardGems[twistLeftCol][curGem.row] = curGem;
                boardGems[twistRightCol][curGem.row] = tempGem;
            }
        }

        // Twist droppers if they are offset by columns
        for (int d = 0; d < fallingGems.size(); d++) {
            Gem curDropper = fallingGems.get(d);
            if (curDropper.currentState != Gem.GemState.Landing) {
                if (curDropper.column == twistLeftCol
                        && curDropper.gemRect.bottom >= rightColumnHeight) {
                    twistGemsLeft.add(curDropper);
                    curDropper.column = twistRightCol;
                    curDropper.isTwisting = true;
                    curDropper.stopSquashing();
                } else if (curDropper.column == twistRightCol
                        && curDropper.gemRect.bottom >= leftColumnHeight) {
                    twistGemsRight.add(curDropper);
                    curDropper.column = twistLeftCol;
                    curDropper.isTwisting = true;
                    curDropper.stopSquashing();
                }
            }
        }

        twistStartColumn = twistLeftCol;
        twistCounter = 0.0f;
        isTwisting = true;
        selectedColumn = -1;
    }

    private void updateTwisting(float deltaTime) {
        twistCounter += deltaTime;
        float ratio = twistCounter / TWIST_DURATION;
        if (ratio > 1.0f) {
            ratio = 1.0f;
        }

        for (int l = 0; l < twistGemsLeft.size(); l++) {
            twistGemsLeft.get(l).gemRect.set((int) (twistLeftX + tileSize * ratio), twistGemsLeft.get(l).gemRect.top,
                    (int) (twistLeftX + (tileSize * ratio) + tileSize), twistGemsLeft.get(l).gemRect.bottom);
            twistGemsLeft.get(l).update(deltaTime);
        }
        for (int r = 0; r < twistGemsRight.size(); r++) {
            twistGemsRight.get(r).gemRect.set((int) (twistRightX - tileSize * ratio), twistGemsRight.get(r).gemRect.top,
                    (int) (twistRightX - (tileSize * ratio) + tileSize), twistGemsRight.get(r).gemRect.bottom);
            twistGemsRight.get(r).update(deltaTime);
        }

        if (twistCounter > TWIST_DURATION) {
            isTwisting = false;

            for (int l = 0; l < twistGemsLeft.size(); l++) {
                twistGemsLeft.get(l).isTwisting = false;
            }
            for (int r = 0; r < twistGemsRight.size(); r++) {
                twistGemsRight.get(r).isTwisting = false;
            }

            twistGemsLeft.clear();
            twistGemsRight.clear();
        }
    }

    private void setFastFalling(boolean fastFalling) {
        this.fastFalling = fastFalling;
        if (fastFalling) {
            fastFallFrame = 0;
            fastFallCounter = 0.0f;
        }

        for (int d = 0; d < fallingGems.size(); d++) {
            fallingGems.get(d).setFastFall(fastFalling);
        }
        selectedColumn = -1;
    }

    public void handleTouchEvents(List<Input.TouchEvent> events) {
        if (boardState == BoardState.Cascading || boardState == BoardState.TwistaSquashing) {
            return;
        }
        for (int e = 0; e < events.size(); e++) {
            Input.TouchEvent curEvent = events.get(e);

            if (curEvent.type == Input.TouchEvent.TOUCH_DOWN) {
                for (int c = 0; c < columnRects.length; c++) {
                    if (columnRects[c].contains(curEvent.x, curEvent.y)) {
                        selectedColumn = c;
                    }
                }

                if (System.nanoTime() - lastTouchTime < doubleTapWindow) {

                    setFastFalling(true);
                } else {

                }

                lastTouchTime = System.nanoTime();
            } else if (curEvent.type == Input.TouchEvent.TOUCH_UP) {
                setFastFalling(false);
            } else if (curEvent.type == Input.TouchEvent.TOUCH_DRAGGED) {
                if (selectedColumn != -1) {
                    for (int c = 0; c < columnRects.length; c++) {
                        if (c == selectedColumn - 1 && columnRects[c].contains(curEvent.x, curEvent.y)) {
                            startTwist(selectedColumn, selectedColumn-1);
                        } else if (c == selectedColumn + 1 && columnRects[c].contains(curEvent.x, curEvent.y)) {
                            startTwist(selectedColumn, selectedColumn+1);
                        }
                    }
                }
            }
        }
    }

    synchronized public boolean handleTouchEvent(MotionEvent event) {
        if (boardState == BoardState.Cascading || boardState == BoardState.TwistaSquashing) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            boolean retVal = false;
            for (int c = 0; c < columnRects.length; c++) {
                if (columnRects[c].contains((int)event.getX(), (int)event.getY())) {
                    selectedColumn = c;
                    retVal = true;
                }
            }

            if (System.nanoTime() - lastTouchTime < doubleTapWindow) {
                setFastFalling(true);
                retVal = true;
            } else {

            }

            lastTouchTime = System.nanoTime();

            return retVal;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            setFastFalling(false);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (selectedColumn != -1) {
                for (int c = 0; c < columnRects.length; c++) {
                    if (c == selectedColumn - 1 && columnRects[c].contains((int)event.getX(), (int)event.getY())) {
                        startTwist(selectedColumn, selectedColumn-1);
                    } else if (c == selectedColumn + 1 && columnRects[c].contains((int)event.getX(), (int)event.getY())) {
                        startTwist(selectedColumn, selectedColumn+1);
                    }
                }
            }
        }

        return false;
    }

    private void initExplosion(Rect tileRect, int gemType) {
        Rect explosionRect = new Rect((int)(tileRect.left - tileSize * explosionMargin), (int)(tileRect.top - tileSize * explosionMargin),
                (int)(tileRect.right + tileSize * explosionMargin),(int)(tileRect.bottom + tileSize * explosionMargin));

        if (gemType >= NUM_GEM_TYPES) {
            gemType = NUM_GEM_TYPES;
        }
        Explosion explosion = explosionPool.newObject(); //Explosion(this, gemType, explosionRect);
        explosion.initializeExplosion(gemType, explosionRect);
        explosions.add(explosion);
    }

    private void initPointBubble(Rect tileRect, int points) {
        PointBubble pointBubble = pointBubblePool.newObject();
        pointBubble.initializePointBubble(tileRect, points);
        pointBubbles.add(pointBubble);
    }

    public void pause() {
        savedState = boardState;
        boardState = BoardState.Paused;
        savedTime = timeElapsed;
    }

    public void unPause() {
        boardState = savedState;
        gameStartTime = System.currentTimeMillis();
    }
}
