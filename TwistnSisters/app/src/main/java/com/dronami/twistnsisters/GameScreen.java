package com.dronami.twistnsisters;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// This is a simple test screen that just displays an Android logo in front of a green background.
// If the screen is tapped, the colors are inverted to demonstrate that input is working.
public class GameScreen extends Screen {
    enum ScreenState {
        Active, PromptQuit, Intro
    }

    ScreenState screenState = ScreenState.Active;
    Game game;
    GameBoard gameBoard;
    Rect playArea;
    Rect[][] tileRects = new Rect[GameBoard.NUM_TILES_X][GameBoard.NUM_TILES_Y];
    private ArrayList<Integer> tileColors = new ArrayList<Integer>();

    private Rect borderRect;
    private Rect separatorRect;
    private int bgColor = Color.rgb(103, 58, 183);
    private int headerColor = Color.rgb(50, 11, 134);
    private int borderColor = Color.rgb(154, 103, 234);

    // Header shit
    private Rect headerArea;
    private Rect timeArea;
    private Rect scoreArea;

    private Pixmap mascotPixmap;
    private Rect mascotRect;

    private Pixmap timeIcon;
    private Rect timeIconRect;
    private Rect timeTextRect;
    private Rect timeTextAreaRect;
    private String timeText = "99:99";

    private Pixmap scoreIcon;
    private Rect scoreIconRect;
    private Rect scoreTextRect;
    private Rect scoreTextAreaRect;
    private String scoreText = "9999";

    private int textYOffset;
    private float headerTextSize;

    private Pixmap gearIcon;
    private Rect gearIconRect;

    private int headerTextColor = Color.WHITE;

    private Paint headerTextPaint;
    private Paint headerShadowPaint;

    private int shadowOffset;

    private long timeLimit = 60000;

    // Sound icon shit
    private boolean soundOn = true;
    private Pixmap soundIconOn;
    private Pixmap soundIconOff;
    private Rect soundIconRect;

    // Sidebar shit
    private ArrayList<Sidebar> sidebars = new ArrayList<>();
    private DialogBox dialogBox;

    public GameScreen(Game game) {
        super(game);
        this.game = game;

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

        int mascotSize = (int)(playAreaTop * 1.15f);
        int mascotMargin = (int)(screenWidth * 0.03f);
        mascotRect = new Rect(0, 0,
                mascotMargin + mascotSize, mascotMargin + mascotSize);
        mascotPixmap = game.getGraphics().newScaledPixmap("mascot.png",
                Graphics.PixmapFormat.ARGB4444, mascotRect.width(), false);

        int iconMargin = (int)(headerArea.height() * 0.2f);
        int headerWidthLeft = screenWidth - headerArea.height() - mascotRect.right;

        timeArea = new Rect(mascotRect.right, 0,
                mascotRect.right + headerWidthLeft / 2, headerArea.bottom);
        timeIconRect = new Rect(timeArea.left + iconMargin/2, timeArea.top + iconMargin,
                timeArea.left + iconMargin/2 + (timeArea.height() - iconMargin * 2), timeArea.bottom - iconMargin);
        timeTextRect = new Rect(timeIconRect.right, 0, timeArea.right, headerArea.bottom);
        int textMargin = (int)(timeTextRect.width() * 0.05f);
        timeTextAreaRect = new Rect(timeTextRect.left + textMargin, textMargin * 2,
                timeTextRect.right - textMargin, headerArea.bottom - textMargin * 2);
        timeIcon = game.getGraphics().newScaledPixmap("clock-icon.png",
                Graphics.PixmapFormat.ARGB4444, timeIconRect.width(), true);

        scoreArea = new Rect(timeArea.right, 0, timeArea.right + timeArea.width(), headerArea.bottom);
        scoreIconRect = new Rect(scoreArea.left + iconMargin/2, scoreArea.top + iconMargin,
                scoreArea.left + iconMargin/2 + (scoreArea.height() - iconMargin * 2), scoreArea.bottom - iconMargin);
        scoreTextRect = new Rect(scoreIconRect.right, 0, scoreArea.right, headerArea.bottom);
        scoreTextAreaRect = new Rect(scoreTextRect.left + textMargin, textMargin * 2,
                scoreTextRect.right - textMargin, headerArea.bottom - textMargin * 2);
        scoreIcon = game.getGraphics().newScaledPixmap("star-icon.png",
                Graphics.PixmapFormat.ARGB4444, scoreIconRect.width(), true);

        gearIconRect = new Rect(screenWidth - headerArea.height() + iconMargin, iconMargin,
                screenWidth - iconMargin, headerArea.bottom - iconMargin);
        gearIcon = game.getGraphics().newScaledPixmap("gear-icon.png",
                Graphics.PixmapFormat.ARGB4444, gearIconRect.width(), false);

        headerTextSize = game.getFontManager().getBiggestFontSize(0, timeTextAreaRect.width(), "99:99");
        headerTextPaint = new Paint();
        headerTextPaint.setTypeface(FontManager.getTypeface(0));
        headerTextPaint.setColor(headerTextColor);
        headerTextPaint.setTextSize(headerTextSize);
        headerTextPaint.setAntiAlias(true);

        headerShadowPaint = new Paint(headerTextPaint);
        headerShadowPaint.setColor(Color.BLACK);

        textYOffset = (int)((timeTextAreaRect.height() - game.getFontManager().getTextHeight(0, (int)headerTextSize, "99:99")) / 2);

        gameBoard = new GameBoard(playArea, game.getGraphics(), game.getFontManager());
        shadowOffset = (int)(gameBoard.tileSize * 0.05f);

        // Init sidebars
        Rect leftArea = new Rect(0, borderRect.top, borderRect.left, screenHeight);
        Rect rightArea = new Rect(borderRect.right, borderRect.top + leftArea.width(), screenWidth, screenHeight);
        ArrayList<Integer> leftTypes = new ArrayList<>();
        leftTypes.add(0);
        ArrayList<Integer> rightTypes = new ArrayList<>();
        rightTypes.add(1);

        initializeSidebars(leftTypes, leftArea, rightTypes, rightArea);

        // Setup sound shit
        soundIconRect = new Rect(rightArea.left, rightArea.top - rightArea.width(),
                rightArea.right, rightArea.top);
        soundIconOn = game.getGraphics().newScaledPixmap("sound-on-icon.png",
                Graphics.PixmapFormat.ARGB4444, soundIconRect.width(), true);
        soundIconOff = game.getGraphics().newScaledPixmap("sound-off-icon.png",
                Graphics.PixmapFormat.ARGB4444, soundIconRect.width(), true);

        dialogBox = new DialogBox(screenWidth, screenHeight, game);
    }

    @Override
    public void update(float deltaTime) {
        List<Input.TouchEvent> touchEvents = game.getInput().getTouchEvents();
        if (touchEvents.size() > 0) {
            if (screenState == ScreenState.Active) {
                handleTouchEvents(touchEvents);
                gameBoard.handleTouchEvents(touchEvents);
            } else if (screenState == ScreenState.PromptQuit){
                int buttonPressed = dialogBox.handleTouchEvent(touchEvents);
                if (buttonPressed == -1) {
                    dialogBox.startTransition(false, true);
                }
            }
        }

        if (screenState == ScreenState.PromptQuit) {
            if (!dialogBox.dialogActive) {
                screenState = ScreenState.Active;
                gameBoard.unPause();
            }
        }

        gameBoard.update(deltaTime);
        scoreText = gameBoard.getScoreString();
        timeText = gameBoard.getTimeString();

        if (dialogBox.dialogActive) {
            dialogBox.updateDialog(deltaTime);
        }

        updateSidebars(deltaTime);
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.clear(bgColor);
        g.drawRect(borderRect, borderColor);
        g.drawRect(headerArea, headerColor);

       // g.drawRect(timeArea, Color.BLUE);
        g.drawPixmap(timeIcon, timeIconRect.left, timeIconRect.top);
        //g.drawRect(timeTextRect, Color.MAGENTA);
        //g.drawRect(timeTextAreaRect, Color.DKGRAY);
        int timeTextXOffset = (int)(timeTextAreaRect.width() - game.getFontManager().getTextWidth(0, headerTextSize, timeText))/2;
        g.drawText(timeText, timeTextAreaRect.left + timeTextXOffset + shadowOffset, timeTextAreaRect.bottom - textYOffset + shadowOffset, headerShadowPaint);
        g.drawText(timeText, timeTextAreaRect.left + timeTextXOffset, timeTextAreaRect.bottom - textYOffset, headerTextPaint);

        //g.drawRect(scoreArea, Color.GREEN);
        g.drawPixmap(scoreIcon, scoreIconRect.left, scoreIconRect.top);
        //g.drawRect(scoreTextRect, Color.YELLOW);
        //g.drawRect(scoreTextAreaRect, Color.DKGRAY);
        int scoreTextXOffset = (int)(scoreTextAreaRect.width() - game.getFontManager().getTextWidth(0, headerTextSize, scoreText))/2;
        g.drawText(scoreText, scoreTextAreaRect.left + scoreTextXOffset + shadowOffset, scoreTextAreaRect.bottom - textYOffset + shadowOffset, headerShadowPaint);
        g.drawText(scoreText, scoreTextAreaRect.left + scoreTextXOffset, scoreTextAreaRect.bottom - textYOffset, headerTextPaint);

        g.drawPixmap(gearIcon, gearIconRect.left, gearIconRect.top);

        gameBoard.draw(g);
        g.drawRect(separatorRect, borderColor);

        for (int s = 0; s < sidebars.size(); s++) {
            sidebars.get(s).draw(g);
        }
        g.drawRect(soundIconRect, headerColor);
        if (soundOn) {
            g.drawPixmap(soundIconOn, soundIconRect.left, soundIconRect.top);
        } else {
            g.drawPixmap(soundIconOff, soundIconRect.left, soundIconRect.top);
        }

        g.drawPixmap(mascotPixmap, mascotRect.left, mascotRect.top);

        if (dialogBox.dialogActive) {
            dialogBox.draw(g);
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

    private void addSidebar(int type, Rect areaRect) {
        sidebars.add(new Sidebar(type, areaRect, game, headerColor));
    }

    private void initializeSidebars(ArrayList<Integer> leftTypes, Rect leftArea, ArrayList<Integer> rightTypes, Rect rightArea) {
        int leftRectHeight = leftArea.height() / leftTypes.size();
        for (int l = 0; l < leftTypes.size(); l++) {
            Rect curRect = new Rect(leftArea);
            curRect.top = leftArea.top + l * leftRectHeight;
            curRect.bottom = curRect.top + leftRectHeight;

            addSidebar(leftTypes.get(l), curRect);
        }

        int rightRectHeight = rightArea.height() / rightTypes.size();
        for (int r = 0; r < rightTypes.size(); r++) {
            Rect curRect = new Rect(rightArea);
            curRect.top = rightArea.top + r * rightRectHeight;
            curRect.bottom = curRect.top + rightRectHeight;

            addSidebar(rightTypes.get(r), curRect);
        }
    }

    private void updateSidebars(float deltaTime) {
        for (int s = 0; s < sidebars.size(); s++) {
            float ratio = 0.0f;
            if (sidebars.get(s).sidebarType == Sidebar.SidebarType.Time.getValue()) {
                ratio = gameBoard.getTimeElapsed() / (float)timeLimit;
                sidebars.get(s).setRatio(1.0f - ratio);
            } else if (sidebars.get(s).sidebarType == Sidebar.SidebarType.Points.getValue()) {
                ratio = gameBoard.getScore() / 20.0f;
                sidebars.get(s).setRatio(ratio);
            }

            sidebars.get(s).update(deltaTime);
        }
    }

    private void handleTouchEvents(List<Input.TouchEvent> events) {
        for (int e = 0; e < events.size(); e++) {
            Input.TouchEvent curEvent = events.get(e);

            if (curEvent.type == Input.TouchEvent.TOUCH_DOWN) {
                if (soundIconRect.contains(curEvent.x, curEvent.y)) {
                    soundOn = !soundOn;
                }
            } else if (curEvent.type == Input.TouchEvent.TOUCH_UP) {
                if (gearIconRect.contains(curEvent.x, curEvent.y)) {
                    gameBoard.pause();
                    screenState = ScreenState.PromptQuit;
                    initializeDialogBox("Quit game?", null, true, true, true);
                }
            } else if (curEvent.type == Input.TouchEvent.TOUCH_DRAGGED) {

            }
        }
    }

    private void initializeDialogBox(String hText, ArrayList<String> bTexts, boolean yesNo,
                                     boolean transitionIn, boolean horizontal) {
        dialogBox.initDialog(hText, bTexts, yesNo);
        dialogBox.startTransition(transitionIn, horizontal);
    }
}
