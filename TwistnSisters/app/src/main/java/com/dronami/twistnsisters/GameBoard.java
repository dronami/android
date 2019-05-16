package com.dronami.twistnsisters;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Float.max;


class Gem {
    GameBoard gameBoard;
    static enum GemState {
        Dropping, Falling, Landing, Landed
    }
    GemState currentState;
    public int gemType;
    public int column;
    public int row;
    public boolean isTwisting = false;
    public boolean isFastFalling = true;
    Rect gemRect;
    Rect scaleRect;

    float droppingDuration = 0.8f;
    float droppingCounter = 0.0f;

    float landingDuration = 0.25f;
    float landingCounter = 0.0f;

    float dropperSpeed = 600.0f;
    float dropperSpeedBase = 600.0f;
    float fastFallRatio = 1.5f;

    Gem(int gemType, int column, Rect gemRect, GameBoard gameBoard) {
        currentState = GemState.Dropping;
        this.gameBoard = gameBoard;
        this.gemType = gemType;
        this.column = column;
        this.gemRect = gemRect;

        scaleRect = new Rect();
    }

    public void setFastFall(boolean isFastFalling) {
        this.isFastFalling = isFastFalling;
        if (isFastFalling) {
            dropperSpeed = dropperSpeedBase * fastFallRatio;
        } else {
            dropperSpeed = dropperSpeedBase;
        }
    }

    private void updateTrail() {

//        trailOffsetCounter += deltaTime;
//        if (trailOffsetCounter > trailOffsetDuration || true) {
//            trailOffsetCounter = trailOffsetCounter - trailOffsetDuration;
//            // Shift trails down
//            for (int t = TRAIL_LENGTH-1; t > 0; t--) {
//                trailRects[t].set(trailRects[t-1]);
//            }
//            // Add current position at head of trail
//            trailRects[0].set(gemRect);
//        }
    }

    public void update(float deltaTime) {
        if (currentState == GemState.Dropping) {
            float ratio = 1.0f - droppingCounter / droppingDuration;

            if (gemType >= 0) {
                scaleRect.set(gemRect.left + (int)(ratio * (gameBoard.tileSize/2)), gemRect.top + (int)(ratio * (gameBoard.tileSize/2)),
                        gemRect.right - (int)(ratio * (gameBoard.tileSize/2)), gemRect.bottom - (int)(ratio * (gameBoard.tileSize/2)));

            }
            droppingCounter += deltaTime;
            if (droppingCounter > droppingDuration) {
                currentState = GemState.Falling;
            }
        } else if (currentState == GemState.Falling) {
            int offset = (int)(deltaTime * dropperSpeed);
            gemRect.offset(0, offset);

            updateTrail();
        } else if (currentState == GemState.Landing) {
            float ratio = landingCounter / landingDuration;

            if (ratio <= 0.5f) {
                scaleRect.set(gemRect.left, gemRect.top + (int)((ratio / 0.5f) * (gameBoard.tileSize/3)),
                        gemRect.right, gemRect.bottom);
            } else {
                scaleRect.set(gemRect.left, gemRect.top + (gameBoard.tileSize/3) - (int)((gameBoard.tileSize/3) * (ratio-0.5f)/0.5f),
                        gemRect.right, gemRect.bottom);
            }
            landingCounter += deltaTime;
            if (landingCounter > landingDuration) {
                currentState = GemState.Landed;
            }

            updateTrail();
        }
    }

    public void draw(Graphics g) {
        if (currentState == GemState.Dropping) {
            g.drawPixmap(gameBoard.gemSheet, scaleRect, gameBoard.gemSrcRects.get(gemType));
        } else if (currentState == GemState.Landing) {
            g.drawPixmap(gameBoard.gemSheet, scaleRect, gameBoard.gemSrcRects.get(gemType));
        } else if (currentState == GemState.Falling) {
            g.drawPixmap(gameBoard.gemSheet, gemRect, gameBoard.gemSrcRects.get(gemType));
        } else {
            g.drawPixmap(gameBoard.gemSheet, gemRect, gameBoard.gemSrcRects.get(gemType));
        }
    }

    public void startLanding(int row) {
        this.row = row;
        scaleRect.set(gemRect);
        currentState = GemState.Landing;
        landingCounter = 0.0f;
    }
}

class Explosion {
    GameBoard gameBoard;
    int color;
    Rect explosionRect;

    public boolean isFinished = false;

    static final float explosionDuration = 0.2f;
    float frameDuration = explosionDuration / NUM_FRAMES;
    float frameCounter = 0.0f;

    int currentFrame = 0;
    static final int NUM_FRAMES = 7;
    static final int NUM_SHEET_ROWS = 3;
    static final int NUM_SHEET_COLS = 3;

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

public class GameBoard {
    static enum BoardState {
        Dropping, Falling
    }
    BoardState boardState;

    public int[] gemColors = new int[4];

    public static final int NUM_TILES_X = 4;
    public static final int NUM_TILES_Y = 8;
    Rect[][] tileRects;
    public int tileSize;

    Rect[] gemRects;
    ArrayList<Gem> dropperGems = new ArrayList<Gem>();

    Gem[][] boardGems = new Gem[NUM_TILES_X][NUM_TILES_Y];
    Pixmap gemSheet;
    public ArrayList<Rect> gemSrcRects = new ArrayList<Rect>();

    public Pixmap explosionSheet;
    public ArrayList<Explosion> explosions = new ArrayList<Explosion>();

    Pixmap tileBG;
    Pixmap tileBGSelected;

    Rect[] columnRects = new Rect[NUM_TILES_X];
    int selectedColumn = -1;

    boolean isTwisting = false;
    final static float TWIST_DURATION = 0.2f;
    float twistCounter = 0.0f;
    ArrayList<Gem> twistGemsLeft = new ArrayList<Gem>();
    ArrayList<Gem> twistGemsRight = new ArrayList<Gem>();
    int twistLeftX;
    int twistRightX;
    int twistLeftCol;
    int twistRightCol;

    boolean fastFalling = true;

    long lastTouchTime = -1;
    long doubleTapWindow = 200000000;

    public GameBoard(Rect playArea, Graphics g) {
        tileSize = playArea.width() / NUM_TILES_X;

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

        gemSheet = g.newScaledPixmap("gems-a.png",
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

        clearBoard();
        spawnDroppers();

        gemColors[0] = 0xFF06E7FF;
        gemColors[1] = 0xFFA9F34E;
        gemColors[2] = 0xFFAE52D4;
        gemColors[3] = 0xFFFF5185;

        for (int c = 0; c < NUM_TILES_X; c++) {
            Rect columnRect = new Rect(playArea.left + c * tileSize, playArea.top,
                    playArea.left + (c * tileSize) + tileSize, playArea.bottom);
            columnRects[c] = columnRect;
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

    public void draw(Graphics g) {
        drawBG(g);
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (boardGems[x][y] != null && boardGems[x][y].currentState != Gem.GemState.Landing) {
                    int gemType = boardGems[x][y].gemType;
                    if (!boardGems[x][y].isTwisting) {
                        g.drawPixmap(gemSheet, tileRects[x][y].left, tileRects[x][y].top,
                                gemSrcRects.get(gemType).left, gemSrcRects.get(gemType).top,
                                gemSrcRects.get(gemType).width(),
                                gemSrcRects.get(gemType).height());
                    }
                }
            }
        }

        for (int d = 0; d < dropperGems.size(); d++) {
            dropperGems.get(d).draw(g);
        }

        for (int e = 0; e < explosions.size(); e++) {
            explosions.get(e).draw(g);
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
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                if (x == selectedColumn) {
                    g.drawPixmap(tileBGSelected, tileRects[x][y].left, tileRects[x][y].top);
                } else {
                    g.drawPixmap(tileBG, tileRects[x][y].left, tileRects[x][y].top);
                }
            }
        }

        for (int x = 0; x < NUM_TILES_X; x++) {
            g.drawPixmap(tileBG, gemRects[x].left, gemRects[x].top);
        }
    }

    private void spawnDroppers() {
        Random rand = new Random();
        int typeA = rand.nextInt(4);
        int typeB = rand.nextInt(4);

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
        gemA.setFastFall(fastFalling);
        gemB.setFastFall(fastFalling);
        dropperGems.add(gemA);
        dropperGems.add(gemB);

        boardState = BoardState.Dropping;
    }

    private void onGemLanding(Gem gem, int row) {
        gem.startLanding(row);
        if (row < NUM_TILES_Y) {
            boardGems[gem.column][gem.row] = gem;
        } else {
            Log.e("DronamiError", "Game over");
        }
    }

    public void update(float deltaTime) {
        for (int d = 0; d < dropperGems.size(); d++) {
            Gem curGem = dropperGems.get(d);
            curGem.update(deltaTime);

            if (curGem.currentState == Gem.GemState.Falling) {
                for (int r = 0; r < NUM_TILES_Y; r++) {
                    if (curGem.gemRect.bottom >= tileRects[curGem.column][0].bottom) {
                        onGemLanding(curGem, 0);
                    } else if (boardGems[curGem.column][r] != null) {
                        if (curGem.gemRect.bottom >= tileRects[curGem.column][r].top) {
                            if (boardGems[curGem.column][r].gemType == curGem.gemType) {
                                dropperGems.remove(curGem);
                                boardGems[curGem.column][r] = null;

                                if (r+1 <= NUM_TILES_Y-1) {
                                    initExplosion(tileRects[curGem.column][r+1], gemColors[curGem.gemType]);
                                } else {
                                    initExplosion(curGem.gemRect, gemColors[curGem.gemType]);
                                }

                                initExplosion(tileRects[curGem.column][r], gemColors[curGem.gemType]);
                            } else {
                                onGemLanding(curGem, r+1);
                            }
                            break;
                        }
                    }
                }
            } else if (curGem.currentState == Gem.GemState.Landed) {
                dropperGems.remove(curGem);
            }
        }

        if (dropperGems.size() == 0) {
            spawnDroppers();
        }

        for (int e = explosions.size()-1; e >= 0; e--) {
            explosions.get(e).update(deltaTime);
            if (explosions.get(e).isFinished) {
                explosions.remove(e);
            }
        }

        if (isTwisting) {
            float ratio = twistCounter / TWIST_DURATION;
            if (ratio > 1.0f) {
                ratio = 1.0f;
            }

            for (int l = 0; l < twistGemsLeft.size(); l++) {
                twistGemsLeft.get(l).gemRect.set((int)(twistLeftX + tileSize * ratio), twistGemsLeft.get(l).gemRect.top,
                        (int)(twistLeftX + (tileSize * ratio) + tileSize), twistGemsLeft.get(l).gemRect.bottom);
            }
            for (int r = 0; r < twistGemsRight.size(); r++) {
                twistGemsRight.get(r).gemRect.set((int)(twistRightX - tileSize * ratio), twistGemsRight.get(r).gemRect.top,
                        (int)(twistRightX - (tileSize * ratio) + tileSize), twistGemsRight.get(r).gemRect.bottom);
            }

            twistCounter += deltaTime;
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
                    }
                } else if (x == twistRightCol) {
                    if (boardGems[x][y] != null) {
                        twistGemsRight.add(boardGems[x][y]);
                        boardGems[x][y].isTwisting = true;
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
        for (int d = 0; d < dropperGems.size(); d++) {
            Gem curDropper = dropperGems.get(d);
            if (curDropper.column == twistLeftCol
                    && curDropper.gemRect.bottom >= rightColumnHeight) {
                twistGemsLeft.add(curDropper);
                curDropper.column = twistRightCol;
                curDropper.isTwisting = true;
            } else if (curDropper.column == twistRightCol
                    && curDropper.gemRect.bottom >= leftColumnHeight) {
                twistGemsRight.add(curDropper);
                curDropper.column = twistLeftCol;
                curDropper.isTwisting = true;
            }
        }

        twistCounter = 0.0f;
        isTwisting = true;
        selectedColumn = -1;
    }

    public void handleTouchEvents(List<Input.TouchEvent> events) {
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
                    selectedColumn = -1;
                    fastFalling = true;
                    for (int d = 0; d < dropperGems.size(); d++) {
                        dropperGems.get(d).setFastFall(true);
                    }
                } else {
                    Log.d("TapD", "Not within: "+(System.nanoTime() - lastTouchTime) + " vs. " + doubleTapWindow);
                }

                lastTouchTime = System.nanoTime();
            } else if (curEvent.type == Input.TouchEvent.TOUCH_UP) {
                selectedColumn = -1;
                fastFalling = false;
                for (int d = 0; d < dropperGems.size(); d++) {
                    dropperGems.get(d).setFastFall(false);
                }
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
        Rect explosionRect = new Rect((int)(tileRect.left - tileSize * 0.4f), (int)(tileRect.top - tileSize * 0.4f),
                (int)(tileRect.right + tileSize * 0.4f),(int)(tileRect.bottom + tileSize * 0.4f));

        Explosion explosion = new Explosion(this, gemType, explosionRect);
        explosions.add(explosion);
    }
}
