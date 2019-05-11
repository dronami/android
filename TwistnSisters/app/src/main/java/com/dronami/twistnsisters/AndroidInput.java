package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.content.Context;
import android.view.View;

import java.util.List;

public class AndroidInput implements Input {
    TouchHandler touchHandler;

    public AndroidInput(Context context, View view, float scaleX, float scaleY) {
        touchHandler = new SingleTouchHandler(view, scaleX, scaleY);
    }

    public boolean isTouchDown(int pointer) {
        return touchHandler.isTouchDown(pointer);
    }

    public int getTouchX(int pointer) {
        return touchHandler.getTouchX(pointer);
    }

    public int getTouchY(int pointer) {
        return touchHandler.getTouchY(pointer);
    }

    public List<TouchEvent> getTouchEvents() {
        return touchHandler.getTouchEvents();
    }
}
