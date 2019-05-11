package com.dronami.twistnsisters;

import java.util.List;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface Input {
    public static class TouchEvent {
        public static final int TOUCH_DOWN = 0;
        public static final int TOUCH_UP = 1;
        public static final int TOUCH_DRAGGED = 2;

        public int type;
        public int x, y;
        public int pointer;
    }

    public boolean isTouchDown(int pointer);
    public int getTouchX(int pointer);
    public int getTouchY(int pointer);

    public List<TouchEvent> getTouchEvents();
}
