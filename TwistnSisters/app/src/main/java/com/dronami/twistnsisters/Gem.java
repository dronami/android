package com.dronami.twistnsisters;

import android.graphics.Rect;
import android.util.Log;

public class Gem {
    GameBoard gameBoard;
    static enum GemState {
        Dropping, Falling, Landing, Landed, Cascading, Waiting, SpinningIn, SpinningOut, Hidden
    }
    private static enum SquashState {
        Off, In, On, Out
    }
    GemState currentState;
    public int gemType;
    public int column;
    public int row;
    public boolean isTwisting = false;
    public boolean isFastFalling = false;
    public boolean isCascading = false;
    Rect gemRect;
    Rect scaleRect;

    static int gemWidth;

    float landingDuration = 0.25f;
    float landingCounter = 0.0f;

    float dropperSpeed = 300.0f;
    float dropperSpeedBase = 300.0f;
    float fastFallRatio = 3.0f;

    SquashState squashingStatus = SquashState.Off;
    float squashingDuration = 0.2f;
    float squashingCounter = 0.0f;
    static final float SQUASH_RATIO = 0.75f;
    static int squashWidthDiff = 0;
    static int squashHeightDiff = 0;
    Rect squashRect;

    float currentRotation = 0.0f;
    float currentScale = 1.0f;
    float spinningDuration = 0.5f;
    float spinningCounter = 0.0f;

    Gem(int gemType, int column, Rect gemRect, GameBoard gameBoard) {
        currentState = GemState.Dropping;
        this.gameBoard = gameBoard;
        this.gemType = gemType;
        this.column = column;
        this.gemRect = gemRect;
        gemWidth = gemRect.width();
        squashWidthDiff = gemWidth - (int)(gemWidth * SQUASH_RATIO);
        squashHeightDiff = (int)(gemWidth / SQUASH_RATIO) - gemWidth;

        scaleRect = new Rect();
        squashRect = new Rect();
    }

    public void setFastFall(boolean isFastFalling) {
        if (this.isFastFalling != isFastFalling) {
            this.isFastFalling = isFastFalling;
            if (isFastFalling) {
                dropperSpeed = dropperSpeedBase * fastFallRatio;
                startSquashing(true);
            } else {
                dropperSpeed = dropperSpeedBase;
                startSquashing(false);
            }
        }
    }

    public void stopSquashing() {
        squashingStatus = SquashState.Off;
    }

    private void startSquashing(boolean in) {
        squashingCounter = 0.0f;
        if (in) {
            squashingStatus = SquashState.In;
        } else {
            squashingStatus = SquashState.Out;
        }
    }

    private void updateSquashing(float deltaTime) {
        if (squashingStatus == SquashState.In) {
            float ratio = squashingCounter / squashingDuration;
            int widthDiff = (int)((squashWidthDiff / 2) * ratio);
            squashRect.set(gemRect.left + widthDiff, gemRect.top,
                    gemRect.right - widthDiff,
                    gemRect.bottom + (int)(squashHeightDiff * ratio));

            squashingCounter += deltaTime;
            if (squashingCounter > squashingDuration) {
                squashingStatus = SquashState.On;
            }
        } else if (squashingStatus == SquashState.On) {
            squashRect.set(gemRect.left + squashWidthDiff / 2, gemRect.top,
                    gemRect.right - squashWidthDiff / 2,
                    gemRect.bottom + squashHeightDiff);
        } else {
            float ratio = squashingCounter / squashingDuration;
            int widthDiff = (int)((squashWidthDiff / 2) - (squashWidthDiff / 2) * ratio);
            squashRect.set(gemRect.left + widthDiff, gemRect.top,
                    gemRect.right - widthDiff,
                    gemRect.bottom + (int)(squashHeightDiff - squashHeightDiff * ratio));

            squashingCounter += deltaTime;
            if (squashingCounter > squashingDuration) {
                squashingStatus = SquashState.Off;
            }
        }
    }

    public void startSpinning(boolean in, float duration) {
        spinningDuration = duration;
        spinningCounter = 0.0f;
        if (in) {
            currentState = GemState.SpinningIn;
            currentScale = 0.0f;
        } else {
            currentState = GemState.SpinningOut;
            currentScale = 1.0f;
        }

        currentRotation = 0.0f;
    }

    private void updateSpinning(float deltaTime) {
        float ratio = spinningCounter / spinningDuration;

        if (currentState == GemState.SpinningIn) {
            currentRotation = ratio * 720.0f;
            currentScale = ratio;
        } else {
            currentRotation = ratio * 720.0f;
            currentScale = 1.0f - ratio;
        }

        spinningCounter += deltaTime;
        if (spinningCounter > spinningDuration) {
            if (currentState == GemState.SpinningIn) {
                currentState = GemState.Waiting;
            } else {
                currentState = GemState.Hidden;
            }
         }
    }

    public void startLanding(int row) {
        this.row = row;
        scaleRect.set(gemRect);
        currentState = GemState.Landing;
        landingCounter = 0.0f;
        squashingStatus = SquashState.Off;
    }

    private void updateLanding(float deltaTime) {
        float ratio = landingCounter / landingDuration;

        if (ratio <= 0.5f) {
            scaleRect.set(gemRect.left - (int)((ratio / 0.5f) * (gameBoard.tileSize/4)), gemRect.top + (int)((ratio / 0.5f) * (gameBoard.tileSize/2)),
                    gemRect.right + (int)((ratio / 0.5f) * (gameBoard.tileSize/4)), gemRect.bottom);
        } else {
            scaleRect.set(gemRect.left - (gameBoard.tileSize/4) + (int)((gameBoard.tileSize/4) * (ratio-0.5f)/0.5f), gemRect.top + (gameBoard.tileSize/2) - (int)((gameBoard.tileSize/2) * (ratio-0.5f)/0.5f),
                    gemRect.right + (gameBoard.tileSize/4) - (int)((gameBoard.tileSize/4) * (ratio-0.5f)/0.5f), gemRect.bottom);
        }
        landingCounter += deltaTime;
        if (landingCounter > landingDuration) {
            currentState = GemState.Landed;
        }

        updateTrail();
    }

    private void updateTrail() {

//        trailOffsetCounter += deltaTime;
//        if (trailOffsetCounter > trailOffsetDuration || true) {
//            trailOffsetCounter = trailOffsetCounter - trailOffsetDuration;
//            // Shift trails down
//            for (int t = TRAIL_LENGTH-1; t > 0; t--) {
//                trailRects[t].set(trailRects[t-1]);
//            }
//            // Add current position at head of trail
//            trailRects[0].set(gemRect);
//        }
    }

    public void update(float deltaTime) {
        if (currentState == GemState.SpinningIn || currentState == GemState.SpinningOut) {
            updateSpinning(deltaTime);
        } else if (currentState == GemState.Falling) {
            int offset = (int)(deltaTime * dropperSpeed);
            gemRect.offset(0, offset);

            updateTrail();
        } else if (currentState == GemState.Landing) {
            updateLanding(deltaTime);
        }

        if (squashingStatus != SquashState.Off) {
            updateSquashing(deltaTime);
        }
    }

    public void draw(Graphics g) {
        int curType = gemType;
        Pixmap curPixmap;
        if (gemType >= gameBoard.NUM_GEM_TYPES) {
            curType -= gameBoard.NUM_GEM_TYPES;
            curPixmap = gameBoard.twistaPixmaps[curType];
        } else {
            curPixmap = gameBoard.gemPixmaps[curType];
        }
        if (currentState == GemState.SpinningIn || currentState == GemState.SpinningOut) {
            g.drawMatrixPixmap(curPixmap, gemRect.left, gemRect.top, currentRotation,
                    currentScale, currentScale);
        } else if (squashingStatus != SquashState.Off) {
            g.drawPixmap(curPixmap, squashRect, gameBoard.gemSrcRect);
        } else if (currentState == GemState.Landing) {
            g.drawPixmap(curPixmap, scaleRect, gameBoard.gemSrcRect);
        } else if (currentState != GemState.Hidden) {
            g.drawPixmap(curPixmap, gemRect, gameBoard.gemSrcRect);
        }
    }

    public void setCascading(boolean isCascading) {
        this.isCascading = isCascading;
    }

    public boolean getIsCascading() {
        return isCascading;
    }
}