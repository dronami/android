package com.dronami.twistnsisters;

import android.graphics.Rect;

public class Gem {
    GameBoard gameBoard;
    static enum GemState {
        Dropping, Falling, Landing, Landed, Cascading
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
    float droppingDuration = 0.8f;
    float droppingCounter = 0.0f;

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
        if (currentState == GemState.Dropping) {
            float ratio = 1.0f - droppingCounter / droppingDuration;

            if (gemType >= 0) {
                scaleRect.set(gemRect.left + (int)(ratio * (gameBoard.tileSize/2)), gemRect.top + (int)(ratio * (gameBoard.tileSize/2)),
                        gemRect.right - (int)(ratio * (gameBoard.tileSize/2)), gemRect.bottom - (int)(ratio * (gameBoard.tileSize/2)));

            }
            droppingCounter += deltaTime;
            if (droppingCounter > droppingDuration) {
                currentState = GemState.Falling;
                if (isFastFalling) {
                    startSquashing(true);
                }
            }
        } else if (currentState == GemState.Falling) {
            int offset = (int)(deltaTime * dropperSpeed);
            gemRect.offset(0, offset);

            updateTrail();
        } else if (currentState == GemState.Landing) {
            float ratio = landingCounter / landingDuration;

            if (ratio <= 0.5f) {
                scaleRect.set(gemRect.left, gemRect.top + (int)((ratio / 0.5f) * (gameBoard.tileSize/3)),
                        gemRect.right, gemRect.bottom);
            } else {
                scaleRect.set(gemRect.left, gemRect.top + (gameBoard.tileSize/3) - (int)((gameBoard.tileSize/3) * (ratio-0.5f)/0.5f),
                        gemRect.right, gemRect.bottom);
            }
            landingCounter += deltaTime;
            if (landingCounter > landingDuration) {
                currentState = GemState.Landed;
            }

            updateTrail();
        }

        if (squashingStatus != SquashState.Off) {
            updateSquashing(deltaTime);
        }
    }

    public void draw(Graphics g) {
        if (currentState == GemState.Dropping) {
            g.drawPixmap(gameBoard.gemSheet, scaleRect, gameBoard.gemSrcRects.get(gemType));
        } else if (squashingStatus != SquashState.Off) {
            g.drawPixmap(gameBoard.gemSheet, squashRect, gameBoard.gemSrcRects.get(gemType));
        } else {
            if (currentState == GemState.Landing) {
                g.drawPixmap(gameBoard.gemSheet, scaleRect, gameBoard.gemSrcRects.get(gemType));
            } else if (currentState == GemState.Falling) {
                g.drawPixmap(gameBoard.gemSheet, gemRect, gameBoard.gemSrcRects.get(gemType));
            } else {
                g.drawPixmap(gameBoard.gemSheet, gemRect, gameBoard.gemSrcRects.get(gemType));
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

    public void setCascading(boolean isCascading) {
        this.isCascading = isCascading;
    }

    public boolean getIsCascading() {
        return isCascading;
    }
}