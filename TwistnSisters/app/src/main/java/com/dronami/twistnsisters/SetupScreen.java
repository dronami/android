package com.dronami.twistnsisters;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

class SetupHeader {
    Rect headerRect;
    int headerColor;
    int textX;
    int textY;
    String headerText;

    static Paint headerTextPaint;
    static Paint headerShadowPaint;
    static final float SHADOW_RATIO = 0.1f;
    static int shadowOffset;

    public SetupHeader(Rect headerRect, int headerColor, String headerText, Game game) {
        this.headerRect = headerRect;
        this.headerColor = headerColor;
        this.headerText = headerText;

        int textWidth = (int)game.getFontManager().getTextWidth(0, headerTextPaint.getTextSize(),
                headerText);
        int textHeight = (int)game.getFontManager().getTextHeight(0, headerTextPaint.getTextSize(),
                headerText);

        textX = (headerRect.width() / 2) - textWidth / 2;
        textY = headerRect.top + (headerRect.height() / 2) + textHeight / 2;
        shadowOffset = (int)(headerRect.height() * SHADOW_RATIO);
    }

    public static void initializePaints(Paint htp) {
        headerTextPaint = htp;
        headerShadowPaint = new Paint(headerTextPaint);
        headerShadowPaint.setColor(Game.ColorManager.darkShadowColor);
    }

    public void draw(Graphics g) {
        g.drawRect(headerRect, headerColor);
        g.drawText(headerText, textX + shadowOffset, textY + shadowOffset, headerShadowPaint);
        g.drawText(headerText, textX, textY, headerTextPaint);
    }
}

public class SetupScreen extends Screen {
    Game game;
    ArrayList<SetupHeader> headers = new ArrayList<SetupHeader>();
    int uiColorIndex = 0;
    int bgColor;
    int headerColor;
    int borderColor;
    Paint headerTextPaint = new Paint();

    Rect borderLeftRect;
    Rect borderRightRect;

    Rect buttonAreaRect;

    GameButton okButton;
    GameButton noButton;

    SliderBar levelSlider;

    public SetupScreen(Game game) {
        super(game);
        this.game = game;

        int screenWidth = game.getGraphics().getWidth();
        int screenHeight = game.getGraphics().getHeight();

        bgColor = Game.ColorManager.uiColorSets[uiColorIndex][0];
        headerColor = Game.ColorManager.uiColorSets[uiColorIndex][1];
        borderColor = Game.ColorManager.uiColorSets[uiColorIndex][2];

        int buttonAreaHeight = (int)(screenHeight * 0.14f);
        buttonAreaRect = new Rect(0, screenHeight - buttonAreaHeight,
                screenWidth, screenHeight);
        int buttonHeight = (int)(buttonAreaRect.height() * 0.9f);

        Pixmap okUp = game.getGraphics().newScaledPixmap("button_ok_up.png",
                Graphics.PixmapFormat.ARGB4444, buttonHeight, false);
        Pixmap okDown = game.getGraphics().newScaledPixmap("button_ok_down.png",
                Graphics.PixmapFormat.ARGB4444, buttonHeight, false);
        Pixmap noUp = game.getGraphics().newScaledPixmap("button_no_up.png",
                Graphics.PixmapFormat.ARGB4444, buttonHeight, false);
        Pixmap noDown = game.getGraphics().newScaledPixmap("button_no_down.png",
                Graphics.PixmapFormat.ARGB4444, buttonHeight, false);


        int buttonWidth = okUp.getWidth();

        int okButtonLeft = ((screenWidth/2) - buttonWidth)/2;
        int buttonTop = buttonAreaRect.top + (buttonAreaRect.height() - buttonHeight)/2;
        Rect okButtonRect = new Rect(okButtonLeft, buttonTop,
                okButtonLeft + buttonWidth, buttonTop + buttonHeight);
        int noButtonLeft = (screenWidth/2) + ((screenWidth/2) - buttonWidth)/2;
        Rect noButtonRect = new Rect(noButtonLeft, buttonTop,
                noButtonLeft + buttonWidth, buttonTop + buttonHeight);

        okButton = new GameButton(okButtonRect, okUp, okDown);
        noButton = new GameButton(noButtonRect, noUp, noDown);

        int remainingHeight = screenHeight - buttonAreaRect.height();
        int headerHeight = screenHeight / 14;
        float headerTextSize = (int)game.getFontManager().getBiggestFontSizeByHeight(0,
                (int)(headerHeight * 0.6f), "Speed Level");
        headerTextPaint.setTypeface(FontManager.getTypeface(0));
        headerTextPaint.setColor(Game.ColorManager.textColor);
        headerTextPaint.setTextSize(headerTextSize);
        headerTextPaint.setAntiAlias(true);
        SetupHeader.initializePaints(headerTextPaint);

        Rect typeHeaderRect = new Rect(0, 0, screenWidth, headerHeight);
        headers.add(new SetupHeader(typeHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Mode", game));

        Rect levelHeaderRect = new Rect(0, remainingHeight/4, screenWidth, (remainingHeight/4) + headerHeight);
        headers.add(new SetupHeader(levelHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Level", game));

        Rect speedHeaderRect = new Rect(0, (remainingHeight*2)/4,
                screenWidth, ((remainingHeight*2)/4) + headerHeight);
        headers.add(new SetupHeader(speedHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Speed", game));

        Rect gemHeaderRect = new Rect(0, (remainingHeight*3)/4,
                screenWidth, ((remainingHeight*3)/4) + headerHeight);
        headers.add(new SetupHeader(gemHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Gem Type", game));

        borderLeftRect = new Rect(0, 0, headerHeight/2, screenHeight);
        borderRightRect = new Rect(screenWidth - headerHeight/2, 0, screenWidth, screenHeight);

        Rect sliderArea = new Rect(borderLeftRect.right, levelHeaderRect.bottom,
                borderRightRect.left, speedHeaderRect.top);
        int sliderMargin = (int)(sliderArea.width() * 0.08f);
        Rect sliderRect = new Rect(sliderArea.left + sliderMargin, sliderArea.top + sliderMargin,
                sliderArea.right - sliderMargin, sliderArea.bottom - sliderMargin);
        levelSlider = new SliderBar(sliderRect, headerColor, 10, game);
    }

    @Override
    public void update(float deltaTime) {
        List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();
        if (touchEvents.size() > 0) {
            levelSlider.handleTouchEvent(touchEvents);
        }

        levelSlider.update(deltaTime);
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.drawRect(borderLeftRect, bgColor);
        g.drawRect(borderRightRect, bgColor);
        g.drawRect(buttonAreaRect, headerColor);
        okButton.draw(g);
        noButton.draw(g);
        levelSlider.draw(g);

        for (int h = 0; h < headers.size(); h++) {
            headers.get(h).draw(g);
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

    private void handleTouchEvents(List<Input.TouchEvent> events) {
        for (int e = 0; e < events.size(); e++) {
            Input.TouchEvent curEvent = events.get(e);

            if (curEvent.type == Input.TouchEvent.TOUCH_DOWN) {

            } else if (curEvent.type == Input.TouchEvent.TOUCH_UP) {

            } else if (curEvent.type == Input.TouchEvent.TOUCH_DRAGGED) {

            }
        }
    }

}