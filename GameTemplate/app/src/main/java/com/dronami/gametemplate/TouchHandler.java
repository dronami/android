package com.dronami.gametemplate;

import android.view.View;

import java.util.List;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface TouchHandler extends View.OnTouchListener {
    public boolean isTouchDown(int pointer);
    public int getTouchX(int pointer);
    public int getTouchY(int pointer);
    public List<Input.TouchEvent> getTouchEvents();
}
