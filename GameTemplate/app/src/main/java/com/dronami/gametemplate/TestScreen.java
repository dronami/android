package com.dronami.gametemplate;

import android.graphics.Color;

import java.util.List;

// This is a simple test screen that just displays an Android logo in front of a green background.
// If the screen is tapped, the colors are inverted to demonstrate that input is working.
public class TestScreen extends Screen {
    int bgColorGreen = 0xA4C639;
    int bgColorBlack = 0x222222;
    Pixmap androidGreenPixmap;
    Pixmap androidBlackPixmap;
    int androidX;
    int androidY;
    int screenStatus = 0;

    public TestScreen(Game game) {
        super(game);
        androidGreenPixmap = game.getGraphics().newPixmap("droid-green.png",
                Graphics.PixmapFormat.ARGB4444);
        androidBlackPixmap = game.getGraphics().newPixmap("droid-black.png",
                Graphics.PixmapFormat.ARGB4444);
        androidX = (game.getGraphics().getWidth() - androidGreenPixmap.getWidth()) / 2;
        androidY = (game.getGraphics().getHeight() - androidGreenPixmap.getHeight()) / 2;
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

        if (screenStatus == 0) {
            g.clear(bgColorBlack);
            g.drawPixmap(androidGreenPixmap, androidX, androidY);
        } else {
            g.clear(bgColorGreen);
            g.drawPixmap(androidBlackPixmap, androidX, androidY);
        }
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
