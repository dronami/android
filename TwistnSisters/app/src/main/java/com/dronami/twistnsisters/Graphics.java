package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public interface Graphics {
    public static enum PixmapFormat {
        ARGB8888, ARGB4444, RGB565
    }

    public Pixmap newPixmap(String fileName, PixmapFormat format);

    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledWidth, int scaledHeight, boolean antiAlias);
    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledWidth, int scaledHeight);

    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledWidth, boolean scaleToWidth, boolean antiAlias);
    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledWidth, boolean scaleToWidth);
    public Pixmap newScaledPixmap(Pixmap pixmap, int newWidth, int newHeight, boolean antiAlias);

    public void clear(int color);

    public void drawPixel(int x, int y, int color);
    public void drawLine(int x, int y, int x2, int y2, int color);

    public void drawRect(int x, int y, int height, int width, int color);
    public void drawRect(Rect rect, Paint paint);
    public void drawRect(Rect rect, int color);

    public void drawPath(Path p, Paint paint);
    public void drawPath(Path p, int color);

    public void drawPixmap(Pixmap pixmap, int x, int y);
    public void drawPixmap(Pixmap pixmap, int x, int y,
                           int srcX, int srcY, int srcWidth, int srcHeight);
    public void drawPixmap(Pixmap pixmap, Rect destRect, Rect srcRect);
    public void drawPixmapColorized(Pixmap pixmap, Rect destRect, Rect srcRect, int color);
    public void drawMatrixPixmap(Pixmap pixmap, int x, int y, float rotation,
                                 float scaleX, float scaleY);

    public void drawText(String text, int x, int y, Paint fontPaint);

    public int getWidth();
    public int getHeight();
}
