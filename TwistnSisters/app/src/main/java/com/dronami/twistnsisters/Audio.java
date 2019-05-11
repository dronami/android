package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface Audio {
    public Music newMusic(String fileName);
    public Sound newSound(String fileName);
}
