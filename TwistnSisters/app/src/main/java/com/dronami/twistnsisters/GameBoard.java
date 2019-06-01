package com.dronami.twistnsisters;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

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

    Explosion(GameBoard gameBoard, int color, Rect explosionRect) {
        this.gameBoard = gameBoard;
        this.color = color;
        this.explosionRect = explosionRect;
        int frameSize = gameBoard.explosionSheet.getWidth() / NUM_SHEET_COLS;

        if (!frameRectsInitialized) {
            for (int r = 0; r < NUM_SHEET_ROWS; r++) {
                for (int c = 0; c < NUM_SHEET_COLS; c++) {
                    Rect frameRect = new Rect(frameSize * c, frameSize * r,
                            (frameSize * c) + frameSize, (frameSize * r) + frameSize);
                    frameSrcRects[(r * NUM_SHEET_COLS) + c] = frameRect;
                }
            }
        }
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
    int shadowOffset;

    public boolean isFinished = false;

    static final float POINT_BUBBLE_DURATION = 1.0f;
    float pointBubbleCounter = 0.0f;

    public PointBubble(GameBoard gameBoard, int color, Rect pbRect, int points) {
        this.gameBoard = gameBoard;
        this.color = color;
        this.pbRect = pbRect;
        pointsString = "+"+points;

        pointBubbleCounter = 0.0f;
        if (!fontInitialized) {
            fontSize = gameBoard.fontManager.getBiggestFontSize(0, (int)(gameBoard.tileSize * (1.0f - margin * 2)), "+99");
            textHeight = gameBoard.fontManager.getTextHeight(0, fontSize, "+99");
            fontPaint.setTypeface(gameBoard.fontManager.getTypeface(0));
            fontPaint.setTextSize(fontSize);
            fontPaint.setColor(color);
            fontPaint.setAntiAlias(true);
            shadowPaint.set(fontPaint);
            shadowPaint.setColor(Color.BLACK);
            fontInitialized = true;

            Log.d("Assy", "Font Size: "+fontSize+", "+textHeight);
        }

        float textWidth = gameBoard.fontManager.getTextWidth(0, fontSize, pointsString);
        x = pbRect.left + (int)((gameBoard.tileSize - textWidth)/2);
        y = pbRect.top + (gameBoard.tileSize/2) + (int)(textHeight / 2);
        shadowOffset = (int)(gameBoard.tileSize * 0.05f);
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
        Active, Cascading, TwistaSquashing
    }
    BoardState boardState;
    FontManager fontManager;

    public int[] gemColors = new int[5];

    public static final int NUM_TILES_X = 4;
    public static final int NUM_TILES_Y = 8;
    public static final int NUM_GEM_TYPES = 4;
    public static final int NUM_TWISTA_TYPES = 3;
    Rect[][] tileRects;
    public int tileSize;

    Rect[] gemRects;
    ArrayList<Gem> dropperGems = new ArrayList<Gem>();
    ArrayList<Gem> fallingGems = new ArrayList<Gem>();

    Gem[][] boardGems = new Gem[NUM_TILES_X][NUM_TILES_Y];
    Pixmap[] gemPixmaps = new Pixmap[NUM_GEM_TYPES];
    Pixmap gemSheet;
    Pixmap twistaSheet;
    Pixmap[] twistaPixmaps = new Pixmap[NUM_GEM_TYPES];
    public ArrayList<Rect> gemSrcRects = new ArrayList<Rect>();
    public Rect gemSrcRect;

    public Pixmap explosionSheet;
    public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
    float explosionMargin = 0.6f;

    public ArrayList<PointBubble> pointBubbles = new ArrayList<PointBubble>();

    Pixmap tileBG;
    Pixmap tileBGSelected;

    Rect[] columnRects = new Rect[NUM_TILES_X];
    int selectedColumn = -1;

    boolean isDropping = false;
    final static float DROPPING_DURATION = 0.6f;
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
    final static boolean[][] fastFallPattern = {
            {true, true, true, true},
            {false, false, false, false},
            {false, false, false, false},
            {true, true, true, true},
            {false, false, false, false},
            {false, false, false, false},
            {true, true, true, true},
            {false, false, false, false},
            {false, false, false, false}
    };

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

    public GameBoard(Rect playArea, Graphics g, FontManager fontManager) {
        tileSize = playArea.width() / NUM_TILES_X;
        this.fontManager = fontManager;

        gemRects = new Rect[NUM_TILES_X];
        for (int x = 0; x < NUM_TILES_X; x++) {
            gemRects[x] = new Rect(playArea.left + x * tileSize, playArea.top,
                    playArea.left + (x * tileSize) + tileSize, playArea.top + tileSize);
        }

        tileRects = new Rect[NUM_TILES_X][NUM_TILES_Y];
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                Rect newTile = new Rect(playArea.left + x * tileSize, (playArea.top+tileSize) + y * tileSize,
                        playArea.left + (x * tileSize) + tileSize, (playArea.top+tileSize) + (y * tileSize) + tileSize);

                // Generate tiles from bottom of screen to top
                tileRects[x][GameBoard.NUM_TILES_Y-1-y] = newTile;
            }
        }

        for (int p = 0; p < NUM_GEM_TYPES; p++) {
            gemPixmaps[p] = g.newScaledPixmap("gem-"+p+".png",
                    Graphics.PixmapFormat.ARGB4444, tileSize, false);
        }

        for (int t = 0; t < NUM_TWISTA_TYPES; t++) {
            twistaPixmaps[t] = g.newScaledPixmap("twista-"+t+".png",
                    Graphics.PixmapFormat.ARGB4444, tileSize, false);
        }

        gemSheet = g.newScaledPixmap("gems-a.png",
                Graphics.PixmapFormat.ARGB4444, tileSize * 2, false);
        twistaSheet = g.newScaledPixmap("twistas.png",
                Graphics.PixmapFormat.ARGB4444, tileSize * 2, false);
        tileBG = g.newScaledPixmap("tilebg.png",
                Graphics.PixmapFormat.RGB565, tileSize, false);
        tileBGSelected = g.newScaledPixmap("tilebg-light.png",
                Graphics.PixmapFormat.RGB565, tileSize, false);
        explosionSheet = g.newPixmap("explosion-sheet.png",
                Graphics.PixmapFormat.ARGB4444);

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                Rect srcRect = new Rect(x * tileSize, y * tileSize,
                        (x * tileSize) + tileSize, (y * tileSize) + tileSize);
                gemSrcRects.add(srcRect);
            }
        }
        gemSrcRect = new Rect(0, 0, tileSize, tileSize);

        clearBoard();
        spawnDroppers();

        gemColors[0] = 0xFF06E7FF;
        gemColors[1] = 0xFFA9F34E;
        gemColors[2] = 0xFFAE52D4;
        gemColors[3] = 0xFFFF5185;
        gemColors[4] = 0xFFFBC02D;

        for (int c = 0; c < NUM_TILES_X; c++) {
            Rect columnRect = new Rect(playArea.left + c * tileSize, playArea.top,
                    playArea.left + (c * tileSize) + tileSize, playArea.bottom);
            columnRects[c] = columnRect;
        }

        int[][] boardPattern = {
                {0, 0, 0, 5},
                {1, 1, 1, -1},
                {0, 2, 3, -1},
                {1, -1, -1, -1},
                {2, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1},
                {-1, -1, -1, -1}
        };

        initBoardPattern(boardPattern);
        printBoard();

        boardState = BoardState.Active;
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
        Random rand = new Random();
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
                    int gemType = boardGems[x][y].gemType;
                    if (!boardGems[x][y].isTwisting) {
                        if (gemType < NUM_GEM_TYPES) {
                            g.drawPixmap(gemPixmaps[gemType], tileRects[x][y].left, tileRects[x][y].top);
                        }
                        // Twistas
                        else {
                            g.drawPixmap(twistaPixmaps[gemType-NUM_GEM_TYPES], tileRects[x][y].left, tileRects[x][y].top);
                        }
                    }
                }
            }
        }

        //if (boardState == BoardState.Active) {
            for (int d = 0; d < dropperGems.size(); d++) {
                dropperGems.get(d).draw(g);
            }
        //}

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
    }

    private void drawBG(Graphics g) {
        if (fastFalling) {
            for (int y = 0; y < NUM_TILES_Y; y++) {
                int curY = y + fastFallFrame;
                if (curY >= fastFallPattern.length) {
                    curY -= fastFallPattern.length;
                }
                for (int x = 0; x < NUM_TILES_X; x++) {
                    if (fastFallPattern[curY][x]) {
                        g.drawPixmap(tileBGSelected, tileRects[x][y].left, tileRects[x][y].top);
                    } else {
                        g.drawPixmap(tileBG, tileRects[x][y].left, tileRects[x][y].top);
                    }
                }
            }

        } else {
            for (int y = 0; y < NUM_TILES_Y; y++) {
                for (int x = 0; x < NUM_TILES_X; x++) {
                    if (x == selectedColumn) {
                        g.drawPixmap(tileBGSelected, tileRects[x][y].left, tileRects[x][y].top);
                    } else {
                        g.drawPixmap(tileBG, tileRects[x][y].left, tileRects[x][y].top);
                    }
                }
            }
        }

        // Draw buffer area tiles
        for (int x = 0; x < NUM_TILES_X; x++) {
            g.drawPixmap(tileBG, gemRects[x].left, gemRects[x].top);
        }
    }

    private void spawnDroppers() {
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

        Gem gemA = new Gem(typeA, columnA, new Rect(gemRects[columnA]), this);
        Gem gemB = new Gem(typeB, columnB, new Rect(gemRects[columnB]), this);
        gemA.startSpinning(true, DROPPING_DURATION);
        gemB.startSpinning(true, DROPPING_DURATION);
        dropperGems.add(gemA);
        dropperGems.add(gemB);

        isDropping = true;
        droppingCounter = 0.0f;
    }

    private void updateDroppers(float deltaTime) {
        //float ratio = 1.0f - droppingCounter / DROPPING_DURATION;
        for (int d = 0; d < dropperGems.size(); d++) {
            dropperGems.get(d).update(deltaTime);
            /*
            Gem curGem = dropperGems.get(d);
            if (curGem.gemType >= 0) {
                curGem.scaleRect.set(curGem.gemRect.left + (int)(ratio * (tileSize/2)), curGem.gemRect.top + (int)(ratio * (tileSize/2)),
                        curGem.gemRect.right - (int)(ratio * (tileSize/2)), curGem.gemRect.bottom - (int)(ratio * (tileSize/2)));

            }
            droppingCounter += deltaTime;
            if (droppingCounter > DROPPING_DURATION) {
                curGem.currentState = Gem.GemState.Waiting;
                isDropping = false;
            }
            */
        }
    }

    private void onGemLanding(Gem gem, int row) {
        gem.startLanding(row);
        if (row < NUM_TILES_Y) {
            boardGems[gem.column][gem.row] = gem;
            gem.gemRect.set(tileRects[gem.column][gem.row]);
        } else {
            Log.e("DronamiError", "Game over");
        }
    }

    private void clearGem(Gem gem, int multiplier) {
        int points = 1 * multiplier;
        int curGemType = gem.gemType;
        if (curGemType > NUM_GEM_TYPES) {
            curGemType = NUM_GEM_TYPES;
        }
        initExplosion(gem.gemRect, gemColors[curGemType]);
        initPointBubble(gem.gemRect, points, gemColors[4]);
        boardGems[gem.column][gem.row] = null;
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
            clearGem(boardGems[c][row], 4);
            //initExplosion(tileRects[c][row], gemColors[gemType]);
            //boardGems[c][row] = null;
        }
    }

    private boolean startCascadeColumn(int row, int column) {
        Log.d("Assy", "Start cascadeCol: "+row+", "+column);
        for (int r = row; r < NUM_TILES_Y; r++) {
            if (boardGems[column][r] != null) {
                cascadeGems.add(boardGems[column][r]);
                boardGems[column][r].setCascading(true);
            }
        }

        if (cascadeGems.size() == 0) {
            return false;
        }

        Log.d("Assy", "CascadingCol: "+cascadeGems.size());
        cascadeCounter = 0.0f;
        cascadeHorizontal = false;
        setFastFalling(false);

        return true;
    }

    private boolean startCascadeRow(int row) {
        Log.d("Assy", "Start cascadeRow: "+row);
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
            Log.d("Assy", "False");
            return false;
        }

        Log.d("Assy", "CascadingRow: "+cascadeGems.size());
        cascadeCounter = 0.0f;
        cascadeRow = row;
        cascadeHorizontal = true;

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
            Log.d("Assy", "Cascade complete");
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

                        //initExplosion(boardGems[c][r].gemRect, gemColors[boardGems[c][r].gemType]);
                        //initExplosion(boardGems[c][r+1].gemRect, gemColors[boardGems[c][r+1].gemType]);
                        //boardGems[c][r] = null;
                        //boardGems[c][r+1] = null;

                        if (startCascadeColumn(r+1, c)) {
                            boardState = BoardState.Cascading;
                        }
                    }
                }
            }

        }
    }

    private boolean checkColumnForTwista(int row, int column) {
        Log.d("Assy", "Check Column for twista: "+row+", "+column);
        boolean twistaFound = false;
        twistaGems.clear();
        for (int r = row; r >= 0; r--) {
            if (boardGems[column][r] != null) {
                if (boardGems[column][r].gemType < 4) {
                    twistaGems.add(boardGems[column][r]);
                    Log.d("Assy", "Adding twista at row: "+r);
                } else if (boardGems[column][r].gemType == 5) {
                    twistaGems.add(boardGems[column][r]);
                    twistaBottomRow = r;
                    twistaFound = true;
                    Log.d("Assy", "Twista found: "+r);
                    break;
                }
            }
        }

        if (twistaFound) {
            twistaCount = twistaGems.size();
            curTwista = 0;
            twistaCounter = 0.0f;
        }

        return twistaFound;
    }

    private void updateTwista(float deltaTime) {
        if (twistaTop.currentState == Gem.GemState.Hidden) {
            initExplosion(tileRects[twistaTop.column][twistaBottomRow], gemColors[NUM_GEM_TYPES]);
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
                    twistaTop.startSpinning(false, DROPPING_DURATION);
                    twistaTop.gemType = 6;
                    boardGems[twistaTop.column][twistaBottomRow] = null;
                } else {
                    if (curTwista - 1 < twistaGems.size()) {
                        Gem curGem = twistaGems.get(curTwista - 1);
                        Log.d("Assy", "Twista Gem: "+curGem.column+", "+curGem.row);
                        clearGem(curGem, curTwista + 1);
                        //initExplosion(tileRects[curGem.column][curGem.row], gemColors[curGem.gemType]);
                        //boardGems[curGem.column][curGem.row] = null;
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
                            if (curGem.gemType == 4) {
                                initExplosion(tileRects[curGem.column][r], gemColors[curGem.gemType]);
                                curGem = null;
                                fallingGems.remove(curGem);
                                break;
                            } else {
                                onGemLanding(curGem, 0);
                                if (isRowMatch(0)) {
                                    fallingGems.remove(curGem);
                                    explodeRow(0, curGem.gemType);
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
                                    if (r + 1 <= NUM_TILES_Y - 1) {
                                        //initExplosion(tileRects[curGem.column][r + 1], gemColors[curGemType]);
                                    } else {
                                        //initExplosion(curGem.gemRect, gemColors[curGemType]);
                                    }

                                    //initExplosion(tileRects[curGem.column][r], gemColors[curGemType]);

                                    if (r+1 < NUM_TILES_Y) {
                                        boardGems[curGem.column][r + 1] = curGem;
                                        if (isRowMatch(r + 1)) {
                                            fallingGems.remove(curGem);
                                            explodeRow(curGem.row + 1, curGemType);

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
                                    if (curGem.gemType == 4) {
                                        twistaActivated = checkColumnForTwista(r, curGem.column);
                                        if (twistaActivated) {
                                            boardState = BoardState.TwistaSquashing;
                                            twistaTop = curGem;
                                            twistaStartRect.set(curGem.gemRect);
                                            fallingGems.remove(curGem);
                                        } else {
                                            // Explode in dropper buffer row
                                            if (r+1 >= NUM_TILES_Y) {
                                                initExplosion(gemRects[curGem.column], gemColors[curGem.gemType]);
                                            }
                                            // Explode normally
                                            else {
                                                initExplosion(tileRects[curGem.column][r+1], gemColors[curGem.gemType]);
                                            }
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

            if (/*boardState == BoardState.Active &&*/ dropperGems.size() == 0) {
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

        for (int e = explosions.size() - 1; e >= 0; e--) {
            explosions.get(e).update(deltaTime);
            if (explosions.get(e).isFinished) {
                explosions.remove(e);
            }
        }

        for (int p = pointBubbles.size() - 1; p >= 0; p--) {
            pointBubbles.get(p).update(deltaTime);
            if (pointBubbles.get(p).isFinished) {
                pointBubbles.remove(p);
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
                    }
                } else if (x == twistRightCol) {
                    if (boardGems[x][y] != null) {
                        twistGemsRight.add(boardGems[x][y]);
                        boardGems[x][y].isTwisting = true;
                        boardGems[x][y].column = twistRightCol - 1;
                        boardGems[x][y].stopSquashing();
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
                if (twistGemsLeft.get(l).currentState != Gem.GemState.Falling) {
                    Log.d("Assy", "TwistColL: "+twistGemsLeft.get(l).column);
                }
            }
            for (int r = 0; r < twistGemsRight.size(); r++) {
                twistGemsRight.get(r).isTwisting = false;
                if (twistGemsRight.get(r).currentState != Gem.GemState.Falling) {
                    Log.d("Assy", "TwistColR: "+twistGemsRight.get(r).column);
                }
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
                    Log.d("TapD", (System.nanoTime() - lastTouchTime) + " vs. " + doubleTapWindow);
                    setFastFalling(true);
                } else {
                    Log.d("TapD", "Not within: "+(System.nanoTime() - lastTouchTime) + " vs. " + doubleTapWindow);
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

    private void initExplosion(Rect tileRect, int gemType) {
        Rect explosionRect = new Rect((int)(tileRect.left - tileSize * explosionMargin), (int)(tileRect.top - tileSize * explosionMargin),
                (int)(tileRect.right + tileSize * explosionMargin),(int)(tileRect.bottom + tileSize * explosionMargin));

        if (gemType >= NUM_GEM_TYPES) {
            gemType = NUM_GEM_TYPES;
        }
        Explosion explosion = new Explosion(this, gemType, explosionRect);
        explosions.add(explosion);
    }

    private void initPointBubble(Rect tileRect, int points, int color) {
        Rect explosionRect = new Rect((int)(tileRect.left - tileSize * explosionMargin), (int)(tileRect.top - tileSize * explosionMargin),
                (int)(tileRect.right + tileSize * explosionMargin),(int)(tileRect.bottom + tileSize * explosionMargin));

        PointBubble pointBubble = new PointBubble(this, color, tileRect, points);
        pointBubbles.add(pointBubble);
    }
}
