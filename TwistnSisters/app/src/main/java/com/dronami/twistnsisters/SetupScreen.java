package com.dronami.twistnsisters;

import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;

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
    enum SetupState {
        Intro, Active, StartingGame, Quitting
    }

    Game game;
    SetupState currentState = SetupState.Intro;
    ArrayList<SetupHeader> headers = new ArrayList<SetupHeader>();
    int uiColorIndex = 0;
    int bgColor;
    int headerColor;
    int borderColor;
    Paint headerTextPaint = new Paint();

    Rect borderLeftRect;
    Rect borderRightRect;

    Rect buttonAreaRect;

    GameButton survivalButton;
    GameButton clearButton;

    GameButton okButton;
    GameButton noButton;

    GameButton slowButton;
    GameButton medButton;
    GameButton fastButton;

    GameButton gemAButton;
    GameButton gemBButton;
    GameButton gemCButton;
    GameButton gemDButton;

    int currentLevel = 6;
    final int MAX_LEVEL = 10;
    SliderBar levelSlider;
    Rect levelTextArea;
    Paint levelTextPaint;
    Paint levelShadowPaint;
    int levelTextX;
    int levelTextY;
    int shadowOffset;

    Paint bgPaint = new Paint();
    BitmapShader bgShader;
    Rect bgRect;
    Matrix bgMatrix;
    float bgOffsetSpeed = 20.0f;

    Transition transition;

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

        int okButtonMargin = (screenWidth - buttonWidth*2)/3;
        int buttonTop = buttonAreaRect.top + (buttonAreaRect.height() - buttonHeight)/2;
        Rect okButtonRect = new Rect(okButtonMargin, buttonTop,
                okButtonMargin + buttonWidth, buttonTop + buttonHeight);
        Rect noButtonRect = new Rect(okButtonRect.right + okButtonMargin, buttonTop,
                okButtonRect.right + okButtonMargin + buttonWidth, buttonTop + buttonHeight);

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

        borderLeftRect = new Rect(0, 0, headerHeight/2, screenHeight);
        borderRightRect = new Rect(screenWidth - headerHeight/2, 0, screenWidth, screenHeight);

        // Mode stuff
        Rect typeHeaderRect = new Rect(0, 0, screenWidth, headerHeight);
        headers.add(new SetupHeader(typeHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Mode", game));

        // Level stuff
        Rect levelHeaderRect = new Rect(0, remainingHeight/4, screenWidth, (remainingHeight/4) + headerHeight);
        headers.add(new SetupHeader(levelHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Level", game));

        Rect modeAreaRect = new Rect(borderLeftRect.right , typeHeaderRect.bottom,
                borderRightRect.left, levelHeaderRect.top);
        int modeButtonWidth = (int)(modeAreaRect.width() * 0.4f);
        int modeButtonMargin = (int)(modeButtonWidth * 0.1f);
        int modeButtonXOffset = (modeAreaRect.width() - (modeButtonWidth*2)-modeButtonMargin)/2;

        Pixmap survivalUp = game.getGraphics().newScaledPixmap("button-survival-up.png",
                Graphics.PixmapFormat.ARGB4444, modeButtonWidth, true);
        Pixmap survivalDown = game.getGraphics().newScaledPixmap("button-survival-down.png",
                Graphics.PixmapFormat.ARGB4444, modeButtonWidth, true);
        Pixmap clearUp = game.getGraphics().newScaledPixmap("button-clear-up.png",
                Graphics.PixmapFormat.ARGB4444, modeButtonWidth, true);
        Pixmap clearDown = game.getGraphics().newScaledPixmap("button-clear-down.png",
                Graphics.PixmapFormat.ARGB4444, modeButtonWidth, true);

        int modeButtonTop = modeAreaRect.top + (modeAreaRect.height() - survivalUp.getHeight())/2;
        Rect survivalRect = new Rect(modeAreaRect.left + modeButtonXOffset, modeButtonTop,
                modeAreaRect.left + modeButtonXOffset + modeButtonWidth, modeButtonTop + survivalUp.getHeight());
        Rect clearRect = new Rect(modeAreaRect.right - modeButtonXOffset - modeButtonWidth, modeButtonTop,
                modeAreaRect.right - modeButtonXOffset, modeButtonTop + survivalUp.getHeight());

        survivalButton = new GameButton(survivalRect, survivalUp, survivalDown);
        clearButton = new GameButton(clearRect, clearUp, clearDown);

        ArrayList<GameButton> survivalGroup = new ArrayList<GameButton>();
        survivalGroup.add(clearButton);
        survivalButton.initializeRadial(0, survivalGroup);

        ArrayList<GameButton> clearGroup = new ArrayList<GameButton>();
        clearGroup.add(survivalButton);
        clearButton.initializeRadial(1, clearGroup);

        int gameMode = game.getSharedPreferences().getInt(Game.SharedPrefData.GAME_MODE_KEY,
                Game.SharedPrefData.GAME_MODE_DEFAULT);
        if (gameMode == 0) {
            survivalButton.buttonDown = true;
        } else {
            clearButton.buttonDown = true;
        }

        // Speed stuff
        Rect speedHeaderRect = new Rect(0, (remainingHeight*2)/4,
                screenWidth, ((remainingHeight*2)/4) + headerHeight);
        headers.add(new SetupHeader(speedHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Speed", game));

        // Gem stuff
        Rect gemHeaderRect = new Rect(0, (remainingHeight*3)/4,
                screenWidth, ((remainingHeight*3)/4) + headerHeight);
        headers.add(new SetupHeader(gemHeaderRect, Game.ColorManager.uiColorSets[uiColorIndex][1],
                "Gem Type", game));

        Rect speedAreaRect = new Rect(borderLeftRect.right, speedHeaderRect.bottom,
                borderRightRect.left, gemHeaderRect.top);
        int speedButtonWidth = (int)(speedAreaRect.width() * 0.3f);
        int speedButtonMargin = (int)(speedButtonWidth * 0.1f);
        int speedButtonXOffset = (speedAreaRect.width() - (speedButtonWidth*3) - (speedButtonMargin*2))/2;

        Pixmap speedLowUp = game.getGraphics().newScaledPixmap("button-slow-up.png",
                Graphics.PixmapFormat.ARGB4444, speedButtonWidth, true);
        Pixmap speedLowDown = game.getGraphics().newScaledPixmap("button-slow-down.png",
                Graphics.PixmapFormat.ARGB4444, speedButtonWidth, true);
        Pixmap speedMedUp = game.getGraphics().newScaledPixmap("button-med-up.png",
                Graphics.PixmapFormat.ARGB4444, speedButtonWidth, true);
        Pixmap speedMedDown = game.getGraphics().newScaledPixmap("button-med-down.png",
                Graphics.PixmapFormat.ARGB4444, speedButtonWidth, true);
        Pixmap speedFastUp = game.getGraphics().newScaledPixmap("button-fast-up.png",
                Graphics.PixmapFormat.ARGB4444, speedButtonWidth, true);
        Pixmap speedFastDown = game.getGraphics().newScaledPixmap("button-fast-down.png",
                Graphics.PixmapFormat.ARGB4444, speedButtonWidth, true);

        int speedButtonTop = speedAreaRect.top + (speedAreaRect.height() - speedLowUp.getHeight())/2;
        Rect slowRect = new Rect(speedAreaRect.left + speedButtonXOffset, speedButtonTop,
                speedAreaRect.left + speedButtonXOffset + speedButtonWidth, speedButtonTop + speedLowUp.getHeight());
        Rect medRect = new Rect(slowRect.right + speedButtonMargin, speedButtonTop,
                slowRect.right + speedButtonMargin + speedButtonWidth, speedButtonTop + speedLowUp.getHeight());
        Rect fastRect = new Rect(medRect.right + speedButtonMargin, speedButtonTop,
                medRect.right + speedButtonMargin + speedButtonWidth, speedButtonTop + speedLowUp.getHeight());

        slowButton = new GameButton(slowRect, speedLowUp, speedLowDown);
        medButton = new GameButton(medRect, speedMedUp, speedMedDown);
        fastButton = new GameButton(fastRect, speedFastUp, speedFastDown);

        ArrayList<GameButton> slowGroup = new ArrayList<GameButton>();
        slowGroup.add(medButton); slowGroup.add(fastButton);
        slowButton.initializeRadial(0, slowGroup);

        ArrayList<GameButton> medGroup = new ArrayList<GameButton>();
        medGroup.add(slowButton); medGroup.add(fastButton);
        medButton.initializeRadial(1, medGroup);

        ArrayList<GameButton> fastGroup = new ArrayList<GameButton>();
        fastGroup.add(slowButton); fastGroup.add(medButton);
        fastButton.initializeRadial(2, fastGroup);

        int gameSpeed = game.getSharedPreferences().getInt(Game.SharedPrefData.GAME_SPEED_KEY,
                Game.SharedPrefData.GAME_SPEED_DEFAULT);
        if (gameSpeed == 0) {
            slowButton.buttonDown = true;
        } else if (gameSpeed == 1) {
            medButton.buttonDown = true;
        } else {
            fastButton.buttonDown = true;
        }

        levelTextArea = new Rect(borderRightRect.left - (speedHeaderRect.top - levelHeaderRect.bottom), levelHeaderRect.bottom,
                borderRightRect.left, speedHeaderRect.top);
        int levelTextSize = (int)game.getFontManager().getBiggestFontSizeByWidth(0, levelTextArea.width(), "12");
        int levelTextHeight = (int)game.getFontManager().getTextHeight(0, levelTextSize, ""+currentLevel);
        int levelTextWidth = (int)game.getFontManager().getTextWidth(0, levelTextSize, ""+currentLevel);
        levelTextY = levelTextArea.top + (levelTextHeight) + (levelTextArea.height() - levelTextHeight)/2;
        levelTextX = levelTextArea.left + (int)((levelTextArea.width() - levelTextWidth)/2.8f);
        levelTextPaint = new Paint();
        levelTextPaint.setTypeface(FontManager.getTypeface(0));
        levelTextPaint.setColor(Game.ColorManager.textColor);
        levelTextPaint.setAntiAlias(true);
        levelTextPaint.setTextSize(levelTextSize);
        levelShadowPaint = new Paint(levelTextPaint);
        levelShadowPaint.setColor(Game.ColorManager.darkShadowColor);
        shadowOffset = (int)(levelTextArea.width() * 0.05f);

        Rect gemAreaRect = new Rect(borderLeftRect.right, gemHeaderRect.bottom,
                borderRightRect.left, buttonAreaRect.top);
        int gemButtonWidth = (int)(gemAreaRect.width() * 0.2f);
        int gemButtonXOffset = (gemAreaRect.width() - (gemButtonWidth*4))/5;

        Pixmap gemAUp = game.getGraphics().newScaledPixmap("button-gem-a-up.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemADown = game.getGraphics().newScaledPixmap("button-gem-a-down.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemBUp = game.getGraphics().newScaledPixmap("button-gem-b-up.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemBDown = game.getGraphics().newScaledPixmap("button-gem-b-down.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemCUp = game.getGraphics().newScaledPixmap("button-gem-c-up.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemCDown = game.getGraphics().newScaledPixmap("button-gem-c-down.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemDUp = game.getGraphics().newScaledPixmap("button-gem-d-up.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);
        Pixmap gemDDown = game.getGraphics().newScaledPixmap("button-gem-d-down.png",
                Graphics.PixmapFormat.ARGB4444, gemButtonWidth, true);

        int gemButtonTop = gemAreaRect.top + (gemAreaRect.height() - gemAUp.getHeight())/2;
        Rect gemARect = new Rect(gemAreaRect.left + gemButtonXOffset, gemButtonTop,
                gemAreaRect.left + gemButtonXOffset + gemButtonWidth, gemButtonTop + gemAUp.getHeight());
        Rect gemBRect = new Rect(gemARect.right + gemButtonXOffset, gemButtonTop,
                gemARect.right + gemButtonXOffset + gemButtonWidth, gemButtonTop + gemAUp.getHeight());
        Rect gemCRect = new Rect(gemBRect.right + gemButtonXOffset, gemButtonTop,
                gemBRect.right + gemButtonXOffset + gemButtonWidth, gemButtonTop + gemAUp.getHeight());
        Rect gemDRect = new Rect(gemCRect.right + gemButtonXOffset, gemButtonTop,
                gemCRect.right + gemButtonXOffset + gemButtonWidth, gemButtonTop + gemAUp.getHeight());

        gemAButton = new GameButton(gemARect, gemAUp, gemADown);
        gemBButton = new GameButton(gemBRect, gemBUp, gemBDown);
        gemCButton = new GameButton(gemCRect, gemCUp, gemCDown);
        gemDButton = new GameButton(gemDRect, gemDUp, gemDDown);

        ArrayList<GameButton> gemAGroup = new ArrayList<GameButton>();
        gemAGroup.add(gemBButton); gemAGroup.add(gemCButton); gemAGroup.add(gemDButton);
        gemAButton.initializeRadial(0, gemAGroup);

        ArrayList<GameButton> gemBGroup = new ArrayList<GameButton>();
        gemBGroup.add(gemAButton); gemBGroup.add(gemCButton); gemBGroup.add(gemDButton);
        gemBButton.initializeRadial(1, gemBGroup);

        ArrayList<GameButton> gemCGroup = new ArrayList<GameButton>();
        gemCGroup.add(gemBButton); gemCGroup.add(gemAButton); gemCGroup.add(gemDButton);
        gemCButton.initializeRadial(2, gemCGroup);

        ArrayList<GameButton> gemDGroup = new ArrayList<GameButton>();
        gemDGroup.add(gemBButton); gemDGroup.add(gemCButton); gemDGroup.add(gemAButton);
        gemDButton.initializeRadial(3, gemDGroup);

        int gemType = game.getSharedPreferences().getInt(Game.SharedPrefData.GAME_GEMTYPE_KEY,
                Game.SharedPrefData.GAME_GEMTYPE_DEFAULT);
        if (gemType == 0) {
            gemAButton.buttonDown = true;
        } else if (gemType == 1) {
            gemBButton.buttonDown = true;
        } else if (gemType == 2) {
            gemCButton.buttonDown = true;
        } else {
            gemDButton.buttonDown = true;
        }

        Rect sliderArea = new Rect(borderLeftRect.right, levelHeaderRect.bottom,
                levelTextArea.left, speedHeaderRect.top);
        int sliderMargin = (int)(sliderArea.width() * 0.08f);
        Rect sliderRect = new Rect(sliderArea.left + sliderMargin, sliderArea.top + sliderMargin,
                sliderArea.right - sliderMargin, sliderArea.bottom - sliderMargin);
        levelSlider = new SliderBar(sliderRect, bgColor, MAX_LEVEL, game);

        bgRect = new Rect(borderLeftRect.right, typeHeaderRect.bottom,
                borderRightRect.left, buttonAreaRect.top);
        Pixmap tileBG = game.getGraphics().newScaledPixmap("tilebg.png",
                Graphics.PixmapFormat.RGB565, bgRect.width()/8, false);
        bgPaint = new Paint();
        bgShader = new BitmapShader(((AndroidPixmap)tileBG).bitmap,
                BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
        bgPaint.setShader(bgShader);
        //PorterDuffColorFilter bgFilter = new PorterDuffColorFilter(bgColor, PorterDuff.Mode.ADD);
        //bgPaint.setColorFilter(bgFilter);
        bgMatrix = new Matrix();
        bgMatrix.postTranslate(bgRect.left, bgRect.top);
        bgShader.setLocalMatrix(bgMatrix);

        setCurrentLevel(game.getSharedPreferences().getInt(Game.SharedPrefData.GAME_LEVEL_KEY,
                Game.SharedPrefData.GAME_LEVEL_DEFAULT));
        levelSlider.setRatio(currentLevel/(float)MAX_LEVEL);

        transition = new Transition(screenWidth, screenHeight, -1);
        transition.startTransition(false);
    }

    @Override
    public void update(float deltaTime) {
        levelSlider.update(deltaTime);
        bgMatrix.postTranslate(deltaTime * bgOffsetSpeed, deltaTime * bgOffsetSpeed);
        bgShader.setLocalMatrix(bgMatrix);

        if (currentState == SetupState.Intro) {
            if (transition.updateTransition(deltaTime)) {
                currentState = SetupState.Active;
            }
        } else if (currentState == SetupState.StartingGame) {
            if (transition.updateTransition(deltaTime)) {
                game.setScreen(new GameScreen(game));
            }
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();
        g.clear(Color.DKGRAY);

        g.drawRect(bgRect, bgPaint);
        g.drawRect(borderLeftRect, bgColor);
        g.drawRect(borderRightRect, bgColor);
        g.drawRect(buttonAreaRect, headerColor);

        survivalButton.draw(g);
        clearButton.draw(g);

        okButton.draw(g);
        noButton.draw(g);

        levelSlider.draw(g);

        slowButton.draw(g);
        medButton.draw(g);
        fastButton.draw(g);

        gemAButton.draw(g);
        gemBButton.draw(g);
        gemCButton.draw(g);
        gemDButton.draw(g);

        g.drawText(currentLevel+"", levelTextX + shadowOffset, levelTextY + shadowOffset, levelShadowPaint);
        g.drawText(currentLevel+"", levelTextX, levelTextY, levelTextPaint);

        for (int h = 0; h < headers.size(); h++) {
            headers.get(h).draw(g);
        }

        if (currentState == SetupState.Intro || currentState == SetupState.Quitting
            || currentState == SetupState.StartingGame) {
            transition.draw(g);
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

    private void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
        int textWidth = (int) game.getFontManager().getTextWidth(0, levelTextPaint.getTextSize(), "" + currentLevel);
        levelTextX = levelTextArea.left + (int) ((levelTextArea.width() - textWidth) / 2.8f);
    }

    public void handleTouchEvent(MotionEvent event) {
        if (currentState == SetupState.Active) {
            float levelRatio = levelSlider.handleTouchEvent(event);
            if (levelRatio >= 0.0f) {
                setCurrentLevel((int)(levelRatio * MAX_LEVEL));
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_LEVEL_KEY, currentLevel);
                return;
            }

            int buttonReturn = okButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                currentState = SetupState.StartingGame;
                transition.startTransition(true);
            }
            buttonReturn = noButton.handleTouchEvent(event);
            if (buttonReturn == 1) {

            }

            buttonReturn = survivalButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_MODE_KEY, 0);
                return;
            }
            buttonReturn = clearButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_MODE_KEY, 1);
                return;
            }

            buttonReturn = slowButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_SPEED_KEY, 0);
                return;
            }
            buttonReturn = medButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_SPEED_KEY, 1);
                return;
            }
            buttonReturn = fastButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_SPEED_KEY, 2);
                return;
            }

            buttonReturn = gemAButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_GEMTYPE_KEY, 0);
                return;
            }
            buttonReturn = gemBButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_GEMTYPE_KEY, 1);
                return;
            }
            buttonReturn = gemCButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_GEMTYPE_KEY, 2);
                return;
            }
            buttonReturn = gemDButton.handleTouchEvent(event);
            if (buttonReturn > 0) {
                game.commitToSharedPrefs(Game.SharedPrefData.GAME_GEMTYPE_KEY, 3);
                return;
            }
        }
    }
}