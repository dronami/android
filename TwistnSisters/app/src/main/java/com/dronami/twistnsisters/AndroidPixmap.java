package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.graphics.Bitmap;

public class AndroidPixmap implements Pixmap {
    Bitmap bitmap;
    Graphics.PixmapFormat format;

    public AndroidPixmap(Bitmap bitmap, Graphics.PixmapFormat format) {
        this.bitmap = bitmap;
        this.format = format;
    }

    public int getWidth() {
        return bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap.getHeight();
    }

    public Graphics.PixmapFormat getFormat() {
        return format;
    }

    public void dispose() {
        bitmap.recycle();
    }
}
