package com.dronami.twistnsisters;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.List;

// This is a simple test screen that just displays an Android logo in front of a green background.
// If the screen is tapped, the colors are inverted to demonstrate that input is working.
public class GameScreen extends Screen {
    int bgColorGreen = 0xA4C639;
    int bgColorBlack = 0x222222;
    Pixmap androidGreenPixmap;
    Pixmap androidBlackPixmap;
    int androidX;
    int androidY;
    int screenStatus = 0;

    Rect playArea;
    int playAreaColor = Color.RED;

    public GameScreen(Game game) {
        super(game);

        int droidWidth = (int)(game.getGraphics().getHeight() * 0.15f);

        androidGreenPixmap = game.getGraphics().newScaledPixmap("droid-green.png",
                Graphics.PixmapFormat.ARGB4444, droidWidth, false, false);
        androidBlackPixmap = game.getGraphics().newScaledPixmap("droid-black.png",
                Graphics.PixmapFormat.ARGB4444, droidWidth, false, false);
        androidX = (game.getGraphics().getWidth() - androidGreenPixmap.getWidth()) / 2;
        androidY = (game.getGraphics().getHeight() - androidGreenPixmap.getHeight()) / 2;

//        int playAreaWidth = (int)(game.getGraphics().getWidth() * 0.7f);
//        int playAreaHeight = playAreaWidth * 2;
        int playAreaHeight = (int)(game.getGraphics().getHeight() * 0.85f);
        int playAreaWidth = playAreaHeight / 2;
        int playAreaLeft = (game.getGraphics().getWidth() - playAreaWidth) / 2;
        int playAreaTop = (game.getGraphics().getHeight() - playAreaHeight) * 2 / 3;
        playArea = new Rect(playAreaLeft, playAreaTop,
                playAreaLeft + playAreaWidth, playAreaTop + playAreaHeight);
    }

    @Override
    public void update(float deltaTime) {
        List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();
        int length = touchEvents.size();
        for (int t = 0; t < length; t++) {
            Input.TouchEvent event = touchEvents.get(t);
            if (event.type == Input.TouchEvent.TOUCH_UP) {
                if (screenStatus == 0) {
                    screenStatus = 1;
                } else {
                    screenStatus = 0;
                }
            }
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.clear(Color.GRAY);
        g.drawRect(playArea, playAreaColor);

//        if (screenStatus == 0) {
//            g.clear(bgColorBlack);
//            g.drawPixmap(androidGreenPixmap, androidX, androidY);
//        } else {
//            g.clear(bgColorGreen);
//            g.drawPixmap(androidBlackPixmap, androidX, androidY);
//        }
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
