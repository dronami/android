package com.dronami.twistnsisters;

import android.graphics.*;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

class DialogBox {
    private int boxWidth;
    private int boxHeight = 0;
    private int screenWidth;
    private int screenHeight;
    private ArrayList<Pixmap> boxBitmaps = new ArrayList<Pixmap>();
    private ArrayList<Pixmap> buttonBitmaps = new ArrayList<Pixmap>();
    private Pixmap savedMidBitmap;
    private String headerText;
    private boolean isYesNo = false;
    private boolean isSelection = false;
    boolean dialogActive = false;

    private Graphics g;

    private float marginRatio = 0.1f;
    private float buttonRatio = 0.32f;
    private float radialRatio = 0.15f;

    private int radialSelection = 0;
    private int radialSelectionLast = -1;
    private float radialX = 0f;
    private float radialYOffset = 0f;
    private Pixmap radialBitmap;
    private float radialMarginRatio = 0.12f;
    private float radialMargin = 0f;
    private ArrayList<Rect> radialRects = new ArrayList<Rect>();
    private ArrayList<Rect> radialTouchRects = new ArrayList<Rect>();
    private Paint radialPaintCurrent = new Paint();
    private Paint radialPaintEmpty = new Paint();

    private ArrayList<Boolean> checkboxSelections = new ArrayList<Boolean>();
    private int numCheckboxTexts = 0;
    private Pixmap checkboxBitmap;
    private ArrayList<Rect> checkboxRects = new ArrayList<Rect>();

    private float lastBoxPosX = 0f;
    private float lastBoxPosY = 0f;
    private float boxPosX = 0f;
    private float boxPosY = 0f;
    private float headerX = 0f;
    private float headerY = 0f;

    private ArrayList<String> bodyTextLines = new ArrayList<String>();
    private ArrayList<Integer> bodyTextXs = new ArrayList<Integer>();
    private ArrayList<Integer> bodyTextYs = new ArrayList<Integer>();
    private float bodyTextYMarginRatio = 0.1f;
    private float bodyTextXMarginRatio = 0.025f;
    private float bodyTextLineHeightRatio = 0.11f;
    private int shadowOffset = 0;
    private float shadowOffsetRatio = 0.015f;

    private boolean okButtonDown = false;
    private boolean noButtonDown = false;

    private Rect okButtonRect;
    private Rect noButtonRect;

    private Typeface dialogFont;
    private Paint headerPaint = new Paint();
    private Paint shadowPaint = new Paint();

    private float centerX = 0f;
    private float leftX = 0f;
    private float rightX = 0f;
    private float topY = 0f;
    private float centerY = 0f;
    private float bottomY = 0f;
    private int boxStatus = 0;
    private float transitionStartX = 0f;
    private float transitionStartY = 0f;
    private float transitionEndX = 0f;
    private float transitionEndY = 0f;

    private static final float TRANSITION_DURATION = 0.5f;
    private float transitionCounter = 0.0f;
    private boolean horizontalTransition = true;

    private int darkShadowColor = Color.argb(255,40,40,40);
    private int lightShadowColor = Color.argb(255,60,60,60);

    public DialogBox(int sWidth, int sHeight, Game game) {
        g = game.getGraphics();
        boxWidth = (int)(sWidth - (sWidth * marginRatio * 2.0f));
        int buttonWidth = (int)(boxWidth * buttonRatio);
        int radialSize = (int)(boxWidth * radialRatio);

        Pixmap dialogHeader = game.getGraphics().newScaledPixmap("menutop.png",
                Graphics.PixmapFormat.ARGB4444, boxWidth, true);
        Pixmap dialogMid = game.getGraphics().newScaledPixmap("menumid.png",
                Graphics.PixmapFormat.ARGB4444, boxWidth, true);
        Pixmap dialogBottom = game.getGraphics().newScaledPixmap("menubottom.png",
                Graphics.PixmapFormat.ARGB4444, boxWidth, true);
        boxBitmaps.add(dialogHeader);
        boxBitmaps.add(dialogMid);
        boxBitmaps.add(dialogBottom);
        savedMidBitmap = boxBitmaps.get(1);

        Pixmap okUp = game.getGraphics().newScaledPixmap("button_ok_up.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        Pixmap okDown = game.getGraphics().newScaledPixmap("button_ok_down.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        Pixmap noUp = game.getGraphics().newScaledPixmap("button_no_up.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        Pixmap noDown = game.getGraphics().newScaledPixmap("button_no_down.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        buttonBitmaps.add(okUp);
        buttonBitmaps.add(okDown);
        buttonBitmaps.add(noUp);
        buttonBitmaps.add(noDown);

        screenWidth = sWidth;
        screenHeight = sHeight;

        radialBitmap = game.getGraphics().newScaledPixmap("radialbox.png",
                Graphics.PixmapFormat.ARGB4444, radialSize, false);
        radialMargin = radialMarginRatio * boxWidth;

        checkboxBitmap = game.getGraphics().newScaledPixmap("checkbox.png",
                Graphics.PixmapFormat.ARGB4444, radialSize, false);

        headerX = (boxWidth * 0.05f);
        headerY = (boxWidth * 0.12f);

        dialogFont = game.getFontManager().getTypeface(0);

        headerPaint.setTypeface(dialogFont);
        headerPaint.setARGB(255, 240, 240, 240);
        headerPaint.setTextSize(boxWidth / 11f);
        headerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        shadowPaint.setTypeface(dialogFont);
        shadowPaint.setTextSize(boxWidth / 11f);
        shadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void initDialog(String hText, ArrayList<String> bTexts, boolean yesNo) {
        lastBoxPosX = 0f;
        lastBoxPosY = 0f;
        boxPosX = 0f;
        boxPosY = 0f;

        headerText = hText;
        if (bTexts == null) {
            bodyTextLines.clear();
        } else {
            bodyTextLines = bTexts;
        }
        isYesNo = yesNo;
        isSelection = false;
        dialogActive = false;

        setupBodyText(1.0f, -1);

        boxHeight = boxBitmaps.get(0).getHeight() + boxBitmaps.get(1).getHeight()
                + boxBitmaps.get(2).getHeight();

        setupButtons();

        centerX = screenWidth/2.0f - boxWidth/2.0f;
        leftX = 0f - boxWidth;
        rightX = screenWidth;
        centerY = screenHeight/2.0f - boxHeight/2.0f;
        topY = 0f - boxHeight;
        bottomY = screenHeight;
    }

    public void initSelectionDialog(String hText, ArrayList<String> bTexts, ArrayList<String> cTexts, boolean yesNo, int selection) {
        lastBoxPosX = 0f;
        lastBoxPosY = 0f;
        boxPosX = 0f;
        boxPosY = 0f;

        numCheckboxTexts = cTexts.size();
        headerText = hText;
        bodyTextLines = bTexts;
        for (int c = 0; c < cTexts.size(); c++) {
            bodyTextLines.add(cTexts.get(c));
        }
        isYesNo = yesNo;
        isSelection = true;
        radialRects.clear();
        radialTouchRects.clear();
        checkboxRects.clear();

        setupBodyText(2.0f, (int)(radialBitmap.getWidth() + radialMargin));

        // Setup radial buttons
        radialSelection = selection;
        radialSelectionLast = -1;
        radialX = boxPosX + radialMargin;
        radialYOffset = boxPosY - (radialBitmap.getHeight()/1.5f);

        int r = 0;
        while (r < bodyTextYs.size() - numCheckboxTexts) {
            radialRects.add(new Rect((int)(radialX + radialBitmap.getWidth()*0.18f), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight()*0.18f),
                    (int)(radialX + radialBitmap.getWidth() * 0.82f), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight() * 0.82f)));
            radialTouchRects.add(new Rect((int)radialX, (int)(bodyTextYs.get(r) + radialYOffset),
                    (int)(radialX + radialBitmap.getWidth()), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight())));
            r++;
        }
        while (r < bodyTextYs.size()) {
            checkboxRects.add(new Rect((int)(radialX + radialBitmap.getWidth()*0.18f), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight()*0.18f),
                    (int)(radialX + radialBitmap.getWidth() * 0.82f), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight() * 0.82f)));
            checkboxSelections.add(false);
            r++;
        }
        radialPaintCurrent.setColor(Color.MAGENTA);
        radialPaintEmpty.setColor(Color.BLUE);

        boxHeight = boxBitmaps.get(0).getHeight() + boxBitmaps.get(1).getHeight() + boxBitmaps.get(2).getHeight();

        setupButtons();

        centerX = screenWidth/2.0f - boxWidth/2.0f;
        leftX = 0f - boxWidth;
        rightX = screenWidth;
        centerY = screenHeight/2.0f - boxHeight/2.0f;
        topY = 0f - boxHeight;
        bottomY = screenHeight;
    }

    private void setupBodyText(float lineHeightScale, int textPosX) {
        int bodyTextXMargin = (int)(boxWidth * bodyTextXMarginRatio);
        int bodyTextYMargin = (int)(boxWidth * bodyTextYMarginRatio);
        int bodyTextLineHeight = (int)(boxWidth * bodyTextLineHeightRatio * lineHeightScale);
        shadowOffset = (int)(boxWidth * shadowOffsetRatio);
        bodyTextXs.clear();
        bodyTextYs.clear();

        for (int l = 0; l < bodyTextLines.size(); l++) {
            if (textPosX == -1) {
                bodyTextXs.add((int) ((boxWidth / 2.0f) - bodyTextXMargin
                        - (headerPaint.measureText(bodyTextLines.get(l)) / 2.0f)));
            } else {
                bodyTextXs.add((int) (textPosX * 1.2f));
            }
            bodyTextYs.add(boxBitmaps.get(0).getHeight() + bodyTextYMargin + bodyTextLineHeight * l);
        }

        if (bodyTextLines.size() > 0) {
            boxBitmaps.set(1, g.newScaledPixmap(savedMidBitmap, boxWidth,
                    savedMidBitmap.getHeight() + bodyTextLineHeight * (Math.max(bodyTextLines.size() - 1, 0)) + (int) (bodyTextYMargin * 2.6f), true));
        } else {
            boxBitmaps.set(1, savedMidBitmap);
        }
    }

    private void setupButtons() {
        int buttonTop = (int)(boxHeight - (boxWidth * 0.238f));
        int okX = (int)((boxBitmaps.get(0).getWidth() / 3.25f) - (buttonBitmaps.get(0).getWidth() / 2.0f));
        if (isYesNo) {
            int noX = (int)(boxBitmaps.get(0).getWidth() - (boxBitmaps.get(0).getWidth() / 3.25f) - (buttonBitmaps.get(0).getWidth() / 2.0f));
            okButtonRect = new Rect(okX, buttonTop, okX + buttonBitmaps.get(0).getWidth(),
                    buttonTop + buttonBitmaps.get(0).getHeight());
            noButtonRect = new Rect(noX, buttonTop, noX + buttonBitmaps.get(0).getWidth(),
                    buttonTop + buttonBitmaps.get(0).getHeight());
        } else {
            okX = (int)((boxBitmaps.get(0).getWidth() / 2.0f) - (buttonBitmaps.get(0).getWidth() / 2.0f));
            okButtonRect = new Rect(okX, buttonTop, okX + buttonBitmaps.get(0).getWidth(), buttonTop + buttonBitmaps.get(0).getHeight());
        }
    }

    void startTransition(boolean transitionIn, boolean horizontal) {
        dialogActive = true;
        if (transitionIn) {
            lastBoxPosX = 0f;
            lastBoxPosY = 0f;
            boxPosX = 0f;
            boxPosY = 0f;
        }

        horizontalTransition = horizontal;
        if (transitionIn) {
            boxStatus = 1;
            transitionEndX = centerX;
            transitionEndY = centerY;
            if (horizontal) {
                transitionStartX = leftX;
                transitionStartY = centerY;
            } else {
                transitionStartX = centerX;
                transitionStartY = topY;
            }
        } else {
            boxStatus = 3;
            transitionStartX = centerX;
            transitionStartY = centerY;
            if (horizontal) {
                transitionEndX = rightX;
                transitionEndY = centerY;
            } else {
                transitionEndX = centerX;
                transitionEndY = bottomY;
            }
        }

        transitionCounter = 0.0f;
    }

    void updateDialog(float deltaTime) {
        if (boxStatus == 1 || boxStatus == 3) {
            if (transitionCounter >= TRANSITION_DURATION) {
                if (boxStatus == 1) {
                    boxStatus = 2;
                } else {
                    boxStatus = 0;
                    dialogActive = false;
                }
            } else {
                transitionCounter += deltaTime;
                transitionCounter = Math.min(transitionCounter, TRANSITION_DURATION);
                float transitionRatio = transitionCounter / TRANSITION_DURATION;
                updatePosition(transitionStartX + (transitionEndX - transitionStartX) * transitionRatio,
                        transitionStartY + (transitionEndY - transitionStartY) * transitionRatio);
            }
        }
    }

    private void updatePosition(float posX, float posY) {
        lastBoxPosX = (float)((int)boxPosX);
        lastBoxPosY = (float)((int)boxPosY);
        boxPosX = (float)((int)posX);
        boxPosY = (float)((int)posY);
        okButtonRect.offset((int)(boxPosX - lastBoxPosX), (int)(boxPosY - lastBoxPosY));
        for (int r = 0; r < radialRects.size(); r++) {
            radialRects.get(r).offset((int)(boxPosX - lastBoxPosX), (int)(boxPosY - lastBoxPosY));
        }
        for (int t = 0; t < radialTouchRects.size(); t++) {
            radialTouchRects.get(t).offset((int)(boxPosX - lastBoxPosX), (int)(boxPosY - lastBoxPosY));
        }
        for (int c = 0; c < checkboxRects.size(); c++) {
            checkboxRects.get(c).offset((int)(boxPosX - lastBoxPosX), (int)(boxPosY - lastBoxPosY));
        }
        if (isYesNo) {
            noButtonRect.offset((int)(boxPosX - lastBoxPosX), (int)(boxPosY - lastBoxPosY));
        }

    }

    void setSelection(int sel) {
        if (sel != radialSelection) {
            //SoundManager.playSound(SoundManager.Sounds.SELECT.value)
            radialSelectionLast = radialSelection;
            radialSelection = sel;
        }
    }

    void setCheckbox(int index, boolean value) {
        //SoundManager.playSound(SoundManager.Sounds.SELECT.value)
        checkboxSelections.set(index, value);
    }

    int handleTouchEvent(List<Input.TouchEvent> events) {
        for (int e = 0; e < events.size(); e++) {
            Input.TouchEvent event = events.get(e);
            if (event != null && boxStatus == 2) {
                if (event.type == MotionEvent.ACTION_DOWN) {
                    for (int r = 0; r < radialTouchRects.size(); r++) {
                        if (r != radialSelection && radialTouchRects.get(r).contains(event.x, event.y)) {
                            setSelection(r);
                        }
                    }
                    for (int c = 0; c < checkboxRects.size(); c++) {
                        if (checkboxRects.get(c).contains(event.x, event.y)) {
                            setCheckbox(c, !checkboxSelections.get(c));
                        }
                    }
                }

                if (event.type == MotionEvent.ACTION_DOWN || event.type == MotionEvent.ACTION_MOVE) {
                    if (!okButtonDown && okButtonRect.contains(event.x, event.y)) {
                        okButtonDown = true;
                        //SoundManager.playSound(SoundManager.Sounds.SWITCH2.value);
                    } else if (okButtonDown && !okButtonRect.contains(event.x, event.y)) {
                        okButtonDown = false;
                        //SoundManager.playSound(SoundManager.Sounds.SWITCH1.value);
                    }

                    if (isYesNo) {
                        if (!noButtonDown && noButtonRect.contains(event.x, event.y)) {
                            noButtonDown = true;
                            //SoundManager.playSound(SoundManager.Sounds.SWITCH2.value)
                        } else if (noButtonDown && !noButtonRect.contains(event.x, event.y)) {
                            noButtonDown = false;
                            //SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                        }
                    }
                } else if (event.type == MotionEvent.ACTION_UP) {
                    if (okButtonRect.contains(event.x, event.y)) {
                        okButtonDown = false;
                        //SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                        return 1;
                    } else if (isYesNo && noButtonRect.contains(event.x, event.y)) {
                        noButtonDown = false;
                        //SoundManager.playSound(SoundManager.Sounds.SWITCH1.value)
                        return -1;
                    }
                }
            }
        }

        return 0;
    }

    int getRadialSelection() {
        return radialSelection;
    }

    ArrayList<Boolean> getCheckboxValues() {
        return checkboxSelections;
    }

    void draw (Graphics g) {
        if (boxStatus == 0 || !dialogActive) {
            return;
        }
        g.drawPixmap(boxBitmaps.get(0), (int)boxPosX, (int)boxPosY);
        g.drawPixmap(boxBitmaps.get(1), (int)boxPosX, (int)(boxPosY + boxBitmaps.get(0).getHeight()));
        g.drawPixmap(boxBitmaps.get(2), (int)boxPosX, (int)(boxPosY + boxBitmaps.get(0).getHeight() + boxBitmaps.get(1).getHeight()));

        if (okButtonDown) {
            g.drawPixmap(buttonBitmaps.get(1), okButtonRect.left, okButtonRect.top);
        } else {
            g.drawPixmap(buttonBitmaps.get(0), okButtonRect.left, okButtonRect.top);
        }
        if (isYesNo) {
            if (noButtonDown) {
                g.drawPixmap(buttonBitmaps.get(3), noButtonRect.left, noButtonRect.top);
            } else {
                g.drawPixmap(buttonBitmaps.get(2), noButtonRect.left, noButtonRect.top);
            }
        }

        shadowPaint.setColor(darkShadowColor);
        g.drawText(headerText, (int)(boxPosX + headerX + shadowOffset), (int)(boxPosY + headerY + shadowOffset), shadowPaint);
        g.drawText(headerText, (int)(boxPosX + headerX), (int)(boxPosY + headerY), headerPaint);
        shadowPaint.setColor(lightShadowColor);
        int t = 0;
        while (t < bodyTextLines.size() - numCheckboxTexts) {
            g.drawText(bodyTextLines.get(t), (int)(boxPosX + bodyTextXs.get(t) + shadowOffset),
                    (int)(boxPosY + bodyTextYs.get(t) + shadowOffset), shadowPaint);
            g.drawText(bodyTextLines.get(t), (int)(boxPosX + bodyTextXs.get(t)), (int)(boxPosY + bodyTextYs.get(t)), headerPaint);
            if (isSelection) {
                if (t == radialSelection) {
                    g.drawRect(radialRects.get(t), radialPaintCurrent);
                } else if (t == radialSelectionLast) {
                    g.drawRect(radialRects.get(t), radialPaintEmpty);
                } else {
                    g.drawRect(radialRects.get(t), radialPaintEmpty);
                }
                g.drawPixmap(radialBitmap, (int)(boxPosX + radialX), (int)(boxPosY + bodyTextYs.get(t) + radialYOffset));
            }
            t++;
        }
        while (t < bodyTextLines.size()) {
            g.drawText(bodyTextLines.get(t), (int)(boxPosX + bodyTextXs.get(t) + shadowOffset),
                    (int)(boxPosY + bodyTextYs.get(t) + shadowOffset), shadowPaint);
            g.drawText(bodyTextLines.get(t), (int)(boxPosX + bodyTextXs.get(t)), (int)(boxPosY + bodyTextYs.get(t)), headerPaint);
            if (isSelection) {
                int cbIndex = t - (bodyTextLines.size()-numCheckboxTexts);
                if (checkboxSelections.get(cbIndex)) {
                    g.drawRect(checkboxRects.get(cbIndex), radialPaintCurrent);
                } else {
                    g.drawRect(checkboxRects.get(cbIndex), radialPaintEmpty);
                }
                g.drawPixmap(checkboxBitmap, (int)(boxPosX + radialX), (int)(boxPosY + bodyTextYs.get(t) + radialYOffset));
            }
            t++;
        }
    }
}