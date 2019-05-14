package com.dronami.twistnsisters;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameBoard {
    static enum BoardState {
        Dropping, Falling, Landing
    }
    BoardState boardState;

    public static final int NUM_TILES_X = 4;
    public static final int NUM_TILES_Y = 8;
    Rect[][] tileRects;
    int tileSize;

    float droppingDuration = 1.0f;
    float droppingCounter = 0.0f;
    int[] dropperBuffer = new int[NUM_TILES_X];
    Rect[] dropperRects;
    Rect scaleRect =  new Rect();

    int[][] gems = new int[NUM_TILES_X][NUM_TILES_Y];
    Pixmap gemSheet;
    ArrayList<Rect> gemSrcRects = new ArrayList<Rect>();

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

        if (boardState == BoardState.Dropping) {
            float ratio = 1.0f - droppingCounter / droppingDuration;
            for (int x = 0; x < NUM_TILES_X; x++) {
                int gemType = dropperBuffer[x];
                if (gemType >= 0) {
                    scaleRect.set(dropperRects[x].left + (int)(ratio * (tileSize/2)), dropperRects[x].top + (int)(ratio * (tileSize/2)),
                            dropperRects[x].right - (int)(ratio * (tileSize/2)), dropperRects[x].bottom - (int)(ratio * (tileSize/2)));
                    g.drawPixmap(gemSheet, scaleRect, gemSrcRects.get(gemType));
                }
            }
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

        dropperBuffer[columnA] = typeA;
        dropperBuffer[columnB] = typeB;

        droppingCounter = 0.0f;
        boardState = BoardState.Dropping;
    }

    public void update(float deltaTime) {
        if (boardState == BoardState.Dropping) {
            droppingCounter += deltaTime;
            if (droppingCounter >= droppingDuration) {
                clearBoard();
                spawnDroppers();
            }
        }
    }
}
