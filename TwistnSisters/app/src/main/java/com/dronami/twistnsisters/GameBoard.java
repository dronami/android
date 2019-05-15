package com.dronami.twistnsisters;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class Dropper {
    GameBoard gameBoard;
    static enum DropperState {
        Dropping, Falling, Landing, Landed
    }
    DropperState currentState;
    public int gemType;
    public int column;
    public int row;
    Rect dropperRect;
    Rect scaleRect;

    float droppingDuration = 0.5f;
    float droppingCounter = 0.0f;

    float landingDuration = 0.25f;
    float landingCounter = 0.0f;

    float dropperSpeed = 8.0f;

    Dropper(int gemType, int column, Rect dropperRect, GameBoard gameBoard) {
        currentState = DropperState.Dropping;
        this.gameBoard = gameBoard;
        this.gemType = gemType;
        this.column = column;
        this.dropperRect = dropperRect;

        scaleRect = new Rect();
    }

    public void update(float deltaTime) {
        if (currentState == DropperState.Dropping) {
            float ratio = 1.0f - droppingCounter / droppingDuration;

            if (gemType >= 0) {
                scaleRect.set(dropperRect.left + (int)(ratio * (gameBoard.tileSize/2)), dropperRect.top + (int)(ratio * (gameBoard.tileSize/2)),
                        dropperRect.right - (int)(ratio * (gameBoard.tileSize/2)), dropperRect.bottom - (int)(ratio * (gameBoard.tileSize/2)));

            }
            droppingCounter += deltaTime;
            if (droppingCounter > droppingDuration) {
                currentState = DropperState.Falling;
            }
        } else if (currentState == DropperState.Falling) {
            dropperRect.offset(0, (int)dropperSpeed);
        } else if (currentState == DropperState.Landing) {
            float ratio = landingCounter / landingDuration;

            if (ratio <= 0.5f) {
                scaleRect.set(dropperRect.left, dropperRect.top + (int)((ratio / 0.5f) * (gameBoard.tileSize/3)),
                        dropperRect.right, dropperRect.bottom);
            } else {
                scaleRect.set(dropperRect.left, dropperRect.top + (gameBoard.tileSize/3) - (int)((gameBoard.tileSize/3) * (ratio-0.5f)/0.5f),
                        dropperRect.right, dropperRect.bottom);
            }
            landingCounter += deltaTime;
            if (landingCounter > landingDuration) {
                currentState = DropperState.Landed;
            }
        }
    }

    public void draw(Graphics g) {
        if (currentState == DropperState.Dropping || currentState == DropperState.Landing) {
            g.drawPixmap(gameBoard.gemSheet, scaleRect, gameBoard.gemSrcRects.get(gemType));
        } else {
            g.drawPixmap(gameBoard.gemSheet, dropperRect, gameBoard.gemSrcRects.get(gemType));
        }
    }

    public void startLanding(int row) {
        this.row = row;
        scaleRect.set(dropperRect);
        currentState = DropperState.Landing;
        landingCounter = 0.0f;
    }
}

class Explosion {
    GameBoard gameBoard;
    int color;
    Rect explosionRect;

    public boolean isFinished = false;

    static final float explosionDuration = 0.4f;
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
        g.drawPixmap(gameBoard.explosionSheet, explosionRect, frameSrcRects[currentFrame]);
    }
}

public class GameBoard {
    static enum BoardState {
        Dropping, Falling
    }
    BoardState boardState;

    public static final int NUM_TILES_X = 4;
    public static final int NUM_TILES_Y = 8;
    Rect[][] tileRects;
    public int tileSize;

    Rect[] dropperRects;
    ArrayList<Dropper> droppers = new ArrayList<Dropper>();

    int[][] gems = new int[NUM_TILES_X][NUM_TILES_Y];
    Pixmap gemSheet;
    public ArrayList<Rect> gemSrcRects = new ArrayList<Rect>();

    public Pixmap explosionSheet;
    public ArrayList<Explosion> explosions = new ArrayList<Explosion>();

    Pixmap tileBG;

    public GameBoard(Rect playArea, Graphics g) {
        tileSize = playArea.width() / NUM_TILES_X;

        dropperRects = new Rect[NUM_TILES_X];
        for (int x = 0; x < NUM_TILES_X; x++) {
            dropperRects[x] = new Rect(playArea.left + x * tileSize, playArea.top,
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
    }

    private void randomizeBoard() {
        Random rand = new Random();
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                gems[x][y] = rand.nextInt(4);
            }
        }
    }

    private void clearBoard() {
        Random rand = new Random();
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                gems[x][y] = -1;
            }
        }
    }

    private void printBoard() {
        StringBuffer sb = new StringBuffer("");
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                sb.append(gems[x][y]+", ");
            }
            sb.append("\n");
        }

        Log.d("PrintBoard", sb.toString());
    }

    public void draw(Graphics g) {
        drawBG(g);
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                int gemType = gems[x][y];
                if (gemType >= 0) {
                    g.drawPixmap(gemSheet, tileRects[x][y].left, tileRects[x][y].top,
                            gemSrcRects.get(gemType).left, gemSrcRects.get(gemType).top,
                            gemSrcRects.get(gemType).width(),
                            gemSrcRects.get(gemType).height());
                }
            }
        }

        for (int d = 0; d < droppers.size(); d++) {
            droppers.get(d).draw(g);
        }

        for (int e = 0; e < explosions.size(); e++) {
            explosions.get(e).draw(g);
        }
    }

    private void drawBG(Graphics g) {
        for (int y = 0; y < NUM_TILES_Y; y++) {
            for (int x = 0; x < NUM_TILES_X; x++) {
                g.drawPixmap(tileBG, tileRects[x][y].left, tileRects[x][y].top);
            }
        }

        for (int x = 0; x < NUM_TILES_X; x++) {
            g.drawPixmap(tileBG, dropperRects[x].left, dropperRects[x].top);
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

        Dropper dropperA = new Dropper(typeA, columnA, new Rect(dropperRects[columnA]), this);
        Dropper dropperB = new Dropper(typeB, columnB, new Rect(dropperRects[columnB]), this);
        droppers.add(dropperA);
        droppers.add(dropperB);

        boardState = BoardState.Dropping;
    }

    public void update(float deltaTime) {
        for (int d = 0; d < droppers.size(); d++) {
            Dropper curDropper = droppers.get(d);
            curDropper.update(deltaTime);

            if (curDropper.currentState == Dropper.DropperState.Falling) {
                for (int r = 0; r < NUM_TILES_Y; r++) {
                    if (curDropper.dropperRect.bottom >= tileRects[curDropper.column][0].bottom) {
                        curDropper.startLanding(0);
                    } else if (gems[curDropper.column][r] != -1) {
                        if (curDropper.dropperRect.bottom >= tileRects[curDropper.column][r].top) {
                            if (gems[curDropper.column][r] == curDropper.gemType) {
                                droppers.remove(curDropper);
                                gems[curDropper.column][r] = -1;

                                initExplosion(tileRects[curDropper.column][r+1], curDropper.gemType);
                                initExplosion(tileRects[curDropper.column][r], curDropper.gemType);
                            } else {
                                curDropper.startLanding(r + 1);
                            }
                            break;
                        }
                    }
                }
            } else if (curDropper.currentState == Dropper.DropperState.Landed) {
                gems[curDropper.column][curDropper.row] = curDropper.gemType;
                droppers.remove(curDropper);
            }
        }

        if (droppers.size() == 0) {
            spawnDroppers();
        }

        for (int e = explosions.size()-1; e >= 0; e--) {
            explosions.get(e).update(deltaTime);
            if (explosions.get(e).isFinished) {
                explosions.remove(e);
            }
        }
    }

    private void initExplosion(Rect tileRect, int gemType) {
        Rect explosionRect = new Rect((int)(tileRect.left - tileSize * 0.25f), (int)(tileRect.top - tileSize * 0.25f),
                (int)(tileRect.right + tileSize * 0.25f),(int)(tileRect.bottom + tileSize * 0.25f));

        Explosion explosion = new Explosion(this, gemType, explosionRect);
        explosions.add(explosion);
    }
}
