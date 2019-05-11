package com.dronami.gametemplate;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public interface Graphics {
    public static enum PixmapFormat {
        ARGB8888, ARGB4444, RGB565
    }

    public Pixmap newPixmap(String fileName, PixmapFormat format);
    public void clear(int color);

    public void drawPixel(int x, int y, int color);
    public void drawLine(int x, int y, int x2, int y2, int color);
    public void drawRect(int x, int y, int height, int width, int color);
    public void drawPixmap(Pixmap pixmap, int x, int y);
    public void drawPixmap(Pixmap pixmap, int x, int y,
                           int srcX, int srcY, int srcWidth, int srcHeight);

    public int getWidth();
    public int getHeight();
}