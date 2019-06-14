package com.dronami.twistnsisters;

import android.graphics.*;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

class Transition {
    private float screenWidth;
    private float screenHeight;
    int transitionType;
    private int numTransitionsTypes = 4;
    public boolean transitionActive = false;
    private boolean transitionIn = false;

    private boolean pathStyle = true;
    private ArrayList<Path> drawPaths = new ArrayList<>();
    private ArrayList<Rect> drawRects = new ArrayList<>();
    private Paint transitionPaint = new Paint();
    private float transitionCounter = 0.0f;
    private static final float TRANSITION_DURATION = 1.0f;
    private float transitionRatio = 0.0f;

    private int transitionColor = Color.rgb(20, 20, 20);

    public Transition(int sWidth, int sHeight, int tType) {
        screenWidth = sWidth;
        screenHeight = sHeight;
        transitionPaint.setColor(transitionColor);
        transitionType = tType;
        if (transitionType == -1) {
            randomizeType();
        }

        initTransition(tType);
    }

    public void initTransition(int tType) {
        transitionActive = false;
        if (tType == -1) {
            randomizeType();
        } else {
            transitionType = tType;
        }

        drawPaths.clear();
        drawRects.clear();
        if (transitionType == 0) {
            pathStyle = true;
            for (int x = 0; x < 2; x++) {
                drawPaths.add(new Path());
            }
        } else if (transitionType == 1) {
            pathStyle = true;
            for (int x = 0; x < 4; x++) {
                drawPaths.add(new Path());
            }
        } else if (transitionType == 2) {
            pathStyle = false;
            int numRects = 9;
            int rectWidth = (int)(screenWidth * 1.1f / numRects);
            for (int r = 0; r < numRects; r++) {
                int rectY = (int)screenHeight;
                if (r % 2 == 0) {
                    rectY = 0;
                }
                drawRects.add(new Rect(rectWidth * r, rectY, rectWidth + rectWidth * r, rectY));
            }
        } else if (transitionType == 3) {
            pathStyle = false;
            int numRects = 19;
            int rectHeight = (int)(screenHeight * 1.1f / (numRects));
            for (int r = 0; r < numRects; r++) {
                int rectX = (int)screenWidth;
                if (r % 2 == 0) {
                    rectX = 0;
                }
                drawRects.add(new Rect(rectX, rectHeight * r, rectX, rectHeight + rectHeight * r));
            }
        }
    }

    void randomizeType() {
        Random random = new Random();
        transitionType = random.nextInt(numTransitionsTypes);
    }

    public void startTransition(boolean tIn) {
        transitionIn = tIn;
        transitionActive = true;
        transitionCounter = 0;

        //SoundManager.playSound(SoundManager.Sounds.WHOOSH.value);
    }

    boolean updateTransition(float deltaTime) {
        if (!transitionActive) {
            return true;
        }

        transitionCounter += deltaTime;
        if (transitionCounter > TRANSITION_DURATION) {
            transitionActive = false;
        }

        if (!transitionIn) {
            transitionRatio = 1.0f - (transitionCounter / TRANSITION_DURATION);
        } else {
            transitionRatio = transitionCounter / TRANSITION_DURATION;
        }


        if (transitionType == 0 || transitionType == 1) {
            drawPaths.get(0).rewind();
            drawPaths.get(0).moveTo(0f, 0f);
            drawPaths.get(0).lineTo(screenWidth * transitionRatio, 0f);
            drawPaths.get(0).lineTo(0f, screenHeight * transitionRatio);
            drawPaths.get(0).lineTo(0f, 0f);
            drawPaths.get(0).close();

            drawPaths.get(1).rewind();
            drawPaths.get(1).moveTo(screenWidth, screenHeight);
            drawPaths.get(1).lineTo(screenWidth - screenWidth * transitionRatio, screenHeight);
            drawPaths.get(1).lineTo(screenWidth, screenHeight - screenHeight * transitionRatio);
            drawPaths.get(1).lineTo(screenWidth, screenHeight);
            drawPaths.get(1).close();

            if (transitionType == 1) {
                drawPaths.get(2).rewind();
                drawPaths.get(2).moveTo(screenWidth, 0f);
                drawPaths.get(2).lineTo(screenWidth - screenWidth * transitionRatio, 0f);
                drawPaths.get(2).lineTo(screenWidth, screenHeight * transitionRatio);
                drawPaths.get(2).lineTo(screenWidth, 0f);
                drawPaths.get(2).close();

                drawPaths.get(3).rewind();
                drawPaths.get(3).moveTo(0f, screenHeight);
                drawPaths.get(3).lineTo(screenWidth * transitionRatio, screenHeight);
                drawPaths.get(3).lineTo(0f, screenHeight - screenHeight * transitionRatio);
                drawPaths.get(3).lineTo(0f, screenHeight);
                drawPaths.get(3).close();
            }
        } else if (transitionType == 2) {
            for (int r = 0; r < drawRects.size(); r++) {
                int rectTop = (int)(screenHeight - (transitionRatio * screenHeight));
                int rectBottom = (int)screenHeight;
                if (r % 2 == 0) {
                    rectTop = 0;
                    rectBottom = (int)(transitionRatio * screenHeight);
                }

                drawRects.get(r).set(drawRects.get(r).left, rectTop, drawRects.get(r).right, rectBottom);
            }
        } else if (transitionType == 3) {
            for (int r = 0; r < drawRects.size(); r++) {
                int rectLeft = (int)(screenWidth - (transitionRatio * screenWidth));
                int rectRight = (int)screenWidth;
                if (r % 2 == 0) {
                    rectLeft = 0;
                    rectRight = (int)(transitionRatio * screenWidth);
                }

                drawRects.get(r).set(rectLeft, drawRects.get(r).top, rectRight, drawRects.get(r).bottom);
            }
        }

        return false;
    }

    public void draw(Graphics g) {
        if (pathStyle) {
            for (int p = 0; p < drawPaths.size(); p++) {
                g.drawPath(drawPaths.get(p), transitionPaint);
            }
        } else {
            for (int r = 0; r < drawRects.size(); r++) {
                g.drawRect(drawRects.get(r), transitionPaint);
            }
        }
    }
}