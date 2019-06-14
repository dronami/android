package com.dronami.twistnsisters;

import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class GameButton {
    public enum ButtonType {
        Normal, Radial
    }
    ButtonType buttonType;
    boolean buttonDown = false;

    private Pixmap buttonUpPixmap;
    private Pixmap buttonDownPixmap;

    Rect buttonRect;

    ArrayList<GameButton> radialGroup;
    int radialIndex;

    // Normal button constructor
    public GameButton(Rect buttonRect, Pixmap buttonUpPixmap, Pixmap buttonDownPixmap) {
        buttonType = ButtonType.Normal;
        buttonDown = false;
        this.buttonRect = buttonRect;
        this.buttonUpPixmap = buttonUpPixmap;
        this.buttonDownPixmap = buttonDownPixmap;
    }

    public void initializeRadial(int radialIndex, ArrayList<GameButton> radialGroup) {
        buttonType = ButtonType.Radial;
        this.radialIndex = radialIndex;
        this.radialGroup = radialGroup;
    }

    public int handleTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (buttonRect.contains((int)event.getX(), (int)event.getY())) {
                buttonDown = true;
                if (buttonType == ButtonType.Radial) {
                    for (int r = 0; r < radialGroup.size(); r++) {
                        radialGroup.get(r).buttonDown = false;
                    }
                }

                return radialIndex;
            }
        }

        if (buttonType == ButtonType.Normal) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                buttonDown = buttonRect.contains((int)event.getX(), (int)event.getY());
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                buttonDown = false;
                if (buttonRect.contains((int)event.getX(), (int)event.getY())) {
                    return 1;
                }
            }
        }

        return 0;
    }

    public void draw(Graphics g) {
        if (buttonDown) {
            g.drawPixmap(buttonDownPixmap, buttonRect.left, buttonRect.top);
        } else {
            g.drawPixmap(buttonUpPixmap, buttonRect.left, buttonRect.top);
        }
    }
}
