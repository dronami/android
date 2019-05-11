package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface Game {
    public Input getInput();
    public FileIO getFileIO();
    public Graphics getGraphics();
    public Audio getAudio();

    public void setScreen(Screen screen);
    public Screen getCurrentScreen();
    public Screen getStartScreen();
}
