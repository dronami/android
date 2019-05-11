package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface Pixmap {
    public int getWidth();
    public int getHeight();

    public Graphics.PixmapFormat getFormat();
    public void dispose();
}
