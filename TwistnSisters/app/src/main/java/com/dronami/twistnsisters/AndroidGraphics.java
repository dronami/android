package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class AndroidGraphics implements Graphics {
    AssetManager assetManager;
    Bitmap frameBuffer;
    Canvas canvas;
    Paint paint;
    Rect srcRect = new Rect();
    Rect dstRect = new Rect();
    Matrix matrix = new Matrix();

    public AndroidGraphics(AssetManager assetManager, Bitmap frameBuffer) {
        this.assetManager = assetManager;
        this.frameBuffer = frameBuffer;
        this.canvas = new Canvas(frameBuffer);
        this.paint = new Paint();
    }

    public Pixmap newPixmap(String fileName, PixmapFormat format) {
        Bitmap bitmap = loadBitmap(fileName, format);

        if (bitmap.getConfig() == Bitmap.Config.RGB_565) {
            format = PixmapFormat.RGB565;
        } else if (bitmap.getConfig() == Bitmap.Config.ARGB_4444) {
            format = PixmapFormat.ARGB4444;
        } else {
            format = PixmapFormat.ARGB8888;
        }

        return new AndroidPixmap(bitmap, format);
    }

    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledWidth, int scaledHeight, boolean antiAlias) {
        Bitmap bitmap = loadBitmap(fileName, format);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, antiAlias);

        if (bitmap.getConfig() == Bitmap.Config.RGB_565) {
            format = PixmapFormat.RGB565;
        } else if (bitmap.getConfig() == Bitmap.Config.ARGB_4444) {
            format = PixmapFormat.ARGB4444;
        } else {
            format = PixmapFormat.ARGB8888;
        }

        return new AndroidPixmap(bitmap, format);
    }

    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledDimension, boolean scaleToWidth, boolean antiAlias) {
        Bitmap bitmap = loadBitmap(fileName, format);
        int scaledWidth;
        int scaledHeight;
        float ratio;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        if (scaleToWidth) {
            ratio = scaledDimension / (float)originalWidth;
            scaledWidth = scaledDimension;
            scaledHeight = (int)(originalHeight * ratio);
        } else {
            ratio = scaledDimension / (float)originalHeight;
            scaledWidth = (int)(originalWidth * ratio);
            scaledHeight = scaledDimension;
        }
        Log.d("Dronami", "Ratio: " + ratio);
        Log.d("Dronami", "Original Dimensions: "+originalWidth+", "+originalHeight );
        Log.d("Dronami", "Scaled Dimensions: "+scaledWidth+", "+scaledHeight );
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight,
                antiAlias);

        return new AndroidPixmap(scaledBitmap, format);
    }

    // Overloaded functions to simulate default parameter for antiAlias (always set to true)
    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledWidth, int scaledHeight) {
        return newScaledPixmap(fileName, format, scaledWidth, scaledHeight, true);
    }
    public Pixmap newScaledPixmap(String fileName, PixmapFormat format,
                                  int scaledDimension, boolean scaleToWidth) {
        return newScaledPixmap(fileName, format, scaledDimension, scaleToWidth, true);
    }

    private Bitmap loadBitmap(String fileName, PixmapFormat format) {
        Bitmap.Config config = null;
        if (format == PixmapFormat.RGB565) {
            config = Bitmap.Config.RGB_565;
        } else if (format == PixmapFormat.ARGB4444) {
            config = Bitmap.Config.ARGB_4444;
        } else {
            config = Bitmap.Config.ARGB_8888;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = config;

        InputStream in = null;
        Bitmap bitmap = null;

        try {
            in = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null) {
                throw new RuntimeException("Couldn't load bitmap from asset <" + fileName + ">");
            }
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load bitmap from asset <" + fileName + ">");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }

        return bitmap;
    }

    public void clear(int color) {
        canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8, (color & 0xff));
    }

    public void drawPixel(int x, int y, int color) {
        paint.setColor(color);
        canvas.drawPoint(x, y, paint);
    }

    public void drawLine(int x, int y, int x2, int y2, int color) {
        paint.setColor(color);
        canvas.drawLine(x, y, x2, y2, paint);
    }

    public void drawRect(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + width - 1, y + width - 1, paint);
    }

    public void drawRect(Rect rect, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect, paint);
    }

    public void drawPixmap(Pixmap pixmap, int x, int y,
                           int srcX, int srcY, int srcWidth, int srcHeight) {
        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth - 1;
        srcRect.bottom = srcY + srcHeight - 1;

        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + srcWidth - 1;
        dstRect.bottom = y + srcHeight - 1;

        canvas.drawBitmap(((AndroidPixmap)pixmap).bitmap, srcRect, dstRect, null);
    }

    public void drawPixmap(Pixmap pixmap, Rect dstRect, Rect srcRect) {
        canvas.drawBitmap(((AndroidPixmap)pixmap).bitmap, srcRect, dstRect, null);
    }

    public void drawPixmap(Pixmap pixmap, int x, int y) {
        canvas.drawBitmap(((AndroidPixmap)pixmap).bitmap, x, y, null);
    }

    public void drawPixmapColorized(Pixmap pixmap, Rect dstRect, Rect srcRect, int color) {
        ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
        paint.setColorFilter(filter);

        canvas.drawBitmap(((AndroidPixmap)pixmap).bitmap, srcRect, dstRect, paint);
        paint.setColorFilter(null);
    }

    public void drawMatrixPixmap(Pixmap pixmap, int x, int y, float rotation,
                                 float scaleX, float scaleY) {
        matrix.reset();
        matrix.postTranslate(-pixmap.getWidth() / 2, -pixmap.getHeight() / 2);
        matrix.postScale(scaleX, scaleY);
        matrix.postRotate(rotation);
        matrix.postTranslate(pixmap.getWidth() / 2, pixmap.getHeight() / 2);
        matrix.postTranslate(x, y);
        canvas.drawBitmap(((AndroidPixmap)pixmap).bitmap, matrix, paint);
    }

    public int getWidth() {
        return frameBuffer.getWidth();
    }

    public int getHeight() {
        return frameBuffer.getHeight();
    }
}
