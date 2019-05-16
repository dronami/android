package com.dronami.twistnsisters;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// This is a simple test screen that just displays an Android logo in front of a green background.
// If the screen is tapped, the colors are inverted to demonstrate that input is working.
public class GameScreen extends Screen {
    GameBoard gameBoard;
    int screenStatus = 0;

    Rect playArea;
    int playAreaColor = Color.RED;

    Rect[][] tileRects = new Rect[GameBoard.NUM_TILES_X][GameBoard.NUM_TILES_Y];
    ArrayList<Integer> tileColors = new ArrayList<Integer>();

    Rect borderRect;
    Rect separatorRect;
    int borderColor = Color.DKGRAY;

    Rect headerArea;

    public GameScreen(Game game) {
        super(game);

        int screenWidth = game.getGraphics().getWidth();
        int screenHeight = game.getGraphics().getHeight();

        headerArea = new Rect(0, 0, screenWidth, (int)(screenHeight * 0.08f));
        int playAreaHeight = (screenHeight - headerArea.bottom) - (int)(screenHeight * 0.03f);
        int tileSize = playAreaHeight / 9;
        int playAreaWidth = tileSize * 4;
        int playAreaTop = headerArea.bottom + (int)(screenHeight * 0.015f);
        int playAreaLeft = (screenWidth - playAreaWidth) / 2;
        playArea = new Rect(playAreaLeft, playAreaTop, playAreaLeft + playAreaWidth,
                playAreaTop + playAreaHeight);
        int borderSize = playAreaTop - headerArea.bottom;
        borderRect = new Rect(playArea.left - borderSize, playArea.top - borderSize,
                playArea.right + borderSize, playArea.bottom + borderSize);
        separatorRect = new Rect(playAreaLeft, (playArea.top + tileSize - borderSize/2),
            playArea.right, (playArea.top + tileSize - borderSize/2) + borderSize);


        gameBoard = new GameBoard(playArea, game.getGraphics());
    }

    @Override
    public void update(float deltaTime) {
        List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();
        if (touchEvents.size() > 0) {
            gameBoard.handleTouchEvents(touchEvents);
        }

        gameBoard.update(deltaTime);
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.clear(Color.GRAY);
        g.drawRect(borderRect, playAreaColor);
        g.drawRect(headerArea, borderColor);

        gameBoard.draw(g);
        g.drawRect(separatorRect, playAreaColor);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
