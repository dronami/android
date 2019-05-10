package com.dronami.gametemplate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// This is the main activity of the game,
// which is merely responsible for initializing the first screen
public class GameActivity extends AndroidGame {
    @Override
    public Screen getStartScreen() {
        return new TestScreen(this);
    }
}
