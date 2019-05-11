package com.dronami.twistnsisters;

// This is the main activity of the game,
// which is merely responsible for initializing the first screen
public class GameActivity extends AndroidGame {
    @Override
    public Screen getStartScreen() {
        return new GameScreen(this);
    }
}
