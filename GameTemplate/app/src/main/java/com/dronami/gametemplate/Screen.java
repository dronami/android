package com.dronami.gametemplate;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public abstract class Screen {
    protected final Game game;

    public Screen(Game game) {
        this.game = game;
    }

    public abstract void update(float deltaTime);
    public abstract void present(float deltaTime);

    public abstract void pause();
    public abstract void resume();
    public abstract void dispose();
}
