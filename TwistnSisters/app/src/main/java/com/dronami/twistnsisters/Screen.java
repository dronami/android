package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.view.MotionEvent;

public abstract class Screen {
    protected final Game game;

    public Screen(Game game) {
        this.game = game;
    }

    public abstract void update(float deltaTime);
    public abstract void present(float deltaTime);

    public abstract void handleTouchEvent(MotionEvent event);

    public abstract void pause();
    public abstract void resume();
    public abstract void dispose();
}
