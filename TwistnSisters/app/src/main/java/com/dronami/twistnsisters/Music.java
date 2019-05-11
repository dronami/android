package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface Music {
    public void play();
    public void stop();
    public void pause();

    public void setLooping(boolean looping);
    public void setVolume(float volume);

    public boolean isPlaying();
    public boolean isStopped();
    public boolean isLooping();

    public void dispose();
}
