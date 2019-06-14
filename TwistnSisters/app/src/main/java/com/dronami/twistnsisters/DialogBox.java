package com.dronami.twistnsisters;

import android.graphics.*;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

class DialogBox {
    private int boxWidth;
    private int boxHeight = 0;
    private int screenWidth;
    private int screenHeight;
    private ArrayList<Pixmap> boxBitmaps = new ArrayList<Pixmap>();
    //private ArrayList<Pixmap> buttonBitmaps = new ArrayList<Pixmap>();
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
    private int radialTouchOffset = 0;
    private ArrayList<Integer> radialXOffsets = new ArrayList<>();
    private ArrayList<Integer> radialYOffsets = new ArrayList<>();
    private Paint radialPaintCurrent = new Paint();
    private Paint radialPaintEmpty = new Paint();

    private ArrayList<Boolean> checkboxSelections = new ArrayList<Boolean>();
    private int numCheckboxTexts = 0;
    private Pixmap checkboxBitmap;
    private ArrayList<Rect> checkboxRects = new ArrayList<Rect>();
    private ArrayList<Integer> checkboxXOffsets = new ArrayList<>();
    private ArrayList<Integer> checkboxYOffsets = new ArrayList<>();

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

    private int buttonWidth;
    private GameButton okButton;
    private GameButton noButton;
    private ArrayList<Integer> buttonXOffsets = new ArrayList<>();
    private ArrayList<Integer> buttonYOffsets = new ArrayList<>();

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
        buttonWidth = (int)(boxWidth * buttonRatio);
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
            radialXOffsets.add((int)(radialRects.get(radialRects.size()-1).left - boxPosX));
            radialYOffsets.add((int)(radialRects.get(radialRects.size()-1).top - boxPosY));
            radialTouchRects.add(new Rect((int)radialX, (int)(bodyTextYs.get(r) + radialYOffset),
                    (int)(radialX + radialBitmap.getWidth()), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight())));
            r++;
        }
        if (radialRects.size() > 0) {
            radialTouchOffset = radialTouchRects.get(0).left - radialRects.get(0).left;
        }

        while (r < bodyTextYs.size()) {
            checkboxRects.add(new Rect((int)(radialX + radialBitmap.getWidth()*0.18f), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight()*0.18f),
                    (int)(radialX + radialBitmap.getWidth() * 0.82f), (int)(bodyTextYs.get(r) + radialYOffset + radialBitmap.getHeight() * 0.82f)));
            checkboxXOffsets.add((int)(checkboxRects.get(checkboxRects.size()-1).left - boxPosX));
            checkboxYOffsets.add((int)(checkboxRects.get(checkboxRects.size()-1).top - boxPosY));
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
        buttonXOffsets.clear();
        buttonYOffsets.clear();
        Pixmap okUp = g.newScaledPixmap("button_ok_up.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        Pixmap okDown = g.newScaledPixmap("button_ok_down.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        Pixmap noUp = g.newScaledPixmap("button_no_up.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);
        Pixmap noDown = g.newScaledPixmap("button_no_down.png",
                Graphics.PixmapFormat.ARGB4444, buttonWidth, true);

        int buttonTop = (int)(boxHeight - (boxWidth * 0.238f));
        int okX = (int)((boxBitmaps.get(0).getWidth() / 3.25f) - (okUp.getWidth() / 2.0f));
        if (isYesNo) {
            int noX = (int)(boxBitmaps.get(0).getWidth() - (boxBitmaps.get(0).getWidth() / 3.25f) - (noUp.getWidth() / 2.0f));
            Rect okButtonRect = new Rect(okX, buttonTop, okX + okUp.getWidth(),
                    buttonTop + okUp.getHeight());
            okButton = new GameButton(okButtonRect, okUp, okDown);
            Rect noButtonRect = new Rect(noX, buttonTop, noX + noUp.getWidth(),
                    buttonTop + noUp.getHeight());
            noButton = new GameButton(noButtonRect, noUp, noDown);

            buttonXOffsets.add((int)(okButtonRect.left - boxPosX));
            buttonYOffsets.add((int)(okButtonRect.top - boxPosY));

            buttonXOffsets.add((int)(noButtonRect.left - boxPosX));
            buttonYOffsets.add((int)(noButtonRect.top - boxPosY));
        } else {
            okX = (int)((boxBitmaps.get(0).getWidth() / 2.0f) - (okUp.getWidth() / 2.0f));
            Rect okButtonRect = new Rect(okX, buttonTop, okX + okUp.getWidth(), buttonTop + okUp.getHeight());
            okButton = new GameButton(okButtonRect, okUp, okDown);

            buttonXOffsets.add((int)(okButtonRect.left - boxPosX));
            buttonYOffsets.add((int)(okButtonRect.top - boxPosY));
        }


    }

    void startTransition(boolean transitionIn, boolean horizontal) {
//        if (transitionIn) {
//            lastBoxPosX = 0f;
//            lastBoxPosY = 0f;
//            boxPosX = 0f;
//            boxPosY = 0f;
//        }

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

        lastBoxPosX = transitionStartX;
        lastBoxPosY = transitionStartY;
        boxPosX = lastBoxPosX;
        boxPosY = lastBoxPosY;
        updateButtonPositions();

        transitionCounter = 0.0f;
        dialogActive = true;
    }

    public boolean updateDialog(float deltaTime) {
        if (boxStatus == 1 || boxStatus == 3) {
            if (transitionCounter >= TRANSITION_DURATION) {
                if (boxStatus == 1) {
                    boxStatus = 2;
                } else {
                    boxStatus = 0;
                    return true;
                }
            } else {
                transitionCounter += deltaTime;
                transitionCounter = Math.min(transitionCounter, TRANSITION_DURATION);
                float transitionRatio = transitionCounter / TRANSITION_DURATION;
                updatePosition(transitionStartX + (transitionEndX - transitionStartX) * transitionRatio,
                        transitionStartY + (transitionEndY - transitionStartY) * transitionRatio);
            }
        }

        return false;
    }

    private void updatePosition(float posX, float posY) {
        lastBoxPosX = (float)((int)boxPosX);
        lastBoxPosY = (float)((int)boxPosY);
        boxPosX = (float)((int)posX);
        boxPosY = (float)((int)posY);

        updateButtonPositions();
    }

    private void updateButtonPositions() {
        okButton.buttonRect.set((int)(boxPosX + buttonXOffsets.get(0)), (int)(boxPosY + buttonYOffsets.get(0)),
                (int)(boxPosX + buttonXOffsets.get(0) + okButton.buttonRect.width()), (int)(boxPosY + buttonYOffsets.get(0) + okButton.buttonRect.height()));
        if (isYesNo) {
            noButton.buttonRect.set((int)(boxPosX + buttonXOffsets.get(1)), (int)(boxPosY + buttonYOffsets.get(1)),
                    (int)(boxPosX + buttonXOffsets.get(1) + noButton.buttonRect.width()), (int)(boxPosY + buttonYOffsets.get(1) + noButton.buttonRect.height()));
        }

        for (int c = 0; c < checkboxRects.size(); c++) {
            checkboxRects.get(c).set((int)(boxPosX + checkboxXOffsets.get(c)), (int)(boxPosY + checkboxYOffsets.get(c)),
                    (int)(boxPosX + checkboxXOffsets.get(c) + checkboxRects.get(c).width()), (int)(boxPosY + checkboxYOffsets.get(c) + checkboxRects.get(c).height()));
        }

        for (int r = 0; r < radialRects.size(); r++) {
            radialRects.get(r).set((int)(boxPosX + radialXOffsets.get(r)), (int)(boxPosY + radialYOffsets.get(r)),
                    (int)(boxPosX + radialXOffsets.get(r) + radialRects.get(r).width()), (int)(boxPosY + radialYOffsets.get(r) + radialRects.get(r).height()));
            radialTouchRects.get(r).set(radialRects.get(r).left + radialTouchOffset, radialRects.get(r).top + radialTouchOffset,
                    radialRects.get(r).right - radialTouchOffset, radialRects.get(r).bottom - radialTouchOffset);
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

//    int handleTouchEvent(List<Input.TouchEvent> events) {
//        if (boxStatus == 2) {
//            int okRetval = okButton.handleTouchEvent(events);
//            if (okRetval == 1) {
//                return 1;
//            }
//            if (isYesNo) {
//                int noRetval = noButton.handleTouchEvent(events);
//                if (noRetval == 1) {
//                    return -1;
//                }
//            }
//            for (int e = 0; e < events.size(); e++) {
//                Input.TouchEvent event = events.get(e);
//                if (event != null) {
//                    if (event.type == MotionEvent.ACTION_DOWN) {
//                        for (int r = 0; r < radialTouchRects.size(); r++) {
//                            if (r != radialSelection && radialTouchRects.get(r).contains(event.x, event.y)) {
//                                setSelection(r);
//                            }
//                        }
//                        for (int c = 0; c < checkboxRects.size(); c++) {
//                            if (checkboxRects.get(c).contains(event.x, event.y)) {
//                                setCheckbox(c, !checkboxSelections.get(c));
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return 0;
//    }

    synchronized public int handleTouchEvent(MotionEvent event) {
        if (boxStatus == 2) {
            int okRetval = okButton.handleTouchEvent(event);
            if (okRetval == 1) {
                return 1;
            }
            if (isYesNo) {
                int noRetval = noButton.handleTouchEvent(event);
                if (noRetval == 1) {
                    return -1;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                for (int r = 0; r < radialTouchRects.size(); r++) {
                    if (r != radialSelection && radialTouchRects.get(r).contains((int)event.getX(), (int)event.getY())) {
                        setSelection(r);
                    }
                }
                for (int c = 0; c < checkboxRects.size(); c++) {
                    if (checkboxRects.get(c).contains((int)event.getX(), (int)event.getY())) {
                        setCheckbox(c, !checkboxSelections.get(c));
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

        okButton.draw(g);
        if (isYesNo) {
            noButton.draw(g);
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