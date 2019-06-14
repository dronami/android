package com.dronami.twistnsisters;

import android.graphics.Matrix;
import android.graphics.Rect;

import java.util.ArrayList;

public class Mascot {
    public enum FaceState {
        Normal(0), Happy(1), Sad(2), Ecstatic(3);

        private final int value;
        private FaceState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    private FaceState faceState = FaceState.Normal;
    private FaceState savedFaceState;
    ArrayList<Pixmap> facePixmaps = new ArrayList<>();
    int basePosX;
    int basePosY;
    int curPosX;
    int curPosY;

    private boolean handActive = false;
    private int handStatus = 0;
    private static final int HAND_STATUS_MAX = 4;
    private float handCounter = 0.0f;
    private static final float HAND_DURATION = 0.2f;
    private static final float HAND_ROTATION_START = -30.0f;
    private static final float HAND_ROTATION_END = 60.0f;
    private static final float HAND_ROTATION_DIFF = HAND_ROTATION_END - HAND_ROTATION_START;
    private float handRotation;
    Pixmap handPixmap;
    int handXOffset;
    int handYOffset;

    int floatIntensity;
    double floatAmount = 0.0f;
    float floatCounter = 0.0f;
    float FLOAT_DENOMINATOR = 0.5f;

    boolean shaking = false;
    int shakeIntensity;
    double shakeAmount = 0.0f;
    double shakeCounter = 0.0f;
    float SHAKE_DURATION = 0.8f;
    float SHAKE_DENOMINATOR = 0.001f;

    public Mascot(ArrayList<Pixmap> facePixmaps, Pixmap handPixmap, Rect mascotRect) {
        this.facePixmaps = facePixmaps;
        this.handPixmap = handPixmap;
        basePosX = mascotRect.left;
        basePosY = mascotRect.top;
        curPosX = basePosX;
        curPosY = basePosY;

        floatIntensity = (int)(mascotRect.width() * 0.05f);
        floatCounter = 0.0f;

        shakeIntensity = (int)(mascotRect.width() * 0.05f);
        handXOffset = mascotRect.left + (int)(mascotRect.width() * 0.75f);
        handYOffset = mascotRect.top + (int)(mascotRect.width() * 0.6f);
    }

    public void update(float deltaTime) {
        floatCounter += deltaTime;

        floatAmount = Math.sin(floatCounter/FLOAT_DENOMINATOR) * floatIntensity;
        curPosX = basePosX;
        curPosY = (int)(basePosY + floatAmount);

        if (shaking) {
            shakeCounter += deltaTime;
            if (shakeCounter >= SHAKE_DURATION) {
                shaking = false;
                faceState = savedFaceState;
            }
            shakeAmount = Math.sin(shakeCounter/SHAKE_DENOMINATOR)
                    * (shakeIntensity - (shakeIntensity * (shakeCounter/SHAKE_DURATION)));
            curPosX = (int)(basePosX + shakeAmount);
        }

        if (handActive) {
            handCounter += deltaTime;
            if (handCounter >= HAND_DURATION) {
                handStatus++;
                handCounter -= HAND_DURATION;
                if (handStatus >= HAND_STATUS_MAX) {
                    handActive = false;
                }
            }

            float ratio = handCounter / HAND_DURATION;
            if (handStatus % 2 == 0) {
                handRotation = HAND_ROTATION_START + HAND_ROTATION_DIFF * ratio;
            } else {
                handRotation = (HAND_ROTATION_START + HAND_ROTATION_DIFF) - HAND_ROTATION_DIFF * ratio;
            }
        }
    }

    public void setFaceState(FaceState faceState) {
        if (this.faceState == faceState) {
            return;
        }
        savedFaceState = this.faceState;
        this.faceState = faceState;

        if (faceState == FaceState.Ecstatic) {
            shakeCounter = 0.0f;
            shaking = true;

            startHandWag();
        }
    }

    private void startHandWag() {
        handActive = true;
        handStatus = 0;
        handCounter = 0.0f;
        handRotation = HAND_ROTATION_START;
    }

    public void draw(Graphics g) {
        g.drawPixmap(facePixmaps.get(faceState.getValue()), curPosX, curPosY);
        if (handActive) {
            g.drawMatrixPixmap(handPixmap, handXOffset, handYOffset, handRotation, 1.0f, 1.0f);
        }
    }
}
