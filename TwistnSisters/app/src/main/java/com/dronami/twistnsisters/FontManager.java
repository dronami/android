package com.dronami.twistnsisters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import java.util.ArrayList;

public class FontManager {
    private static FontManager singletonInstance;
    public static Paint fontPaint;
    public static ArrayList<Typeface> typefaces;

    // Singleton design pattern
    private FontManager() {
        fontPaint = new Paint();
        typefaces = new ArrayList<Typeface>();
    }

    public static FontManager getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new FontManager();
        }

        return singletonInstance;
    }

    public static void initializeFont(Context context, String fontName) {
        typefaces.add(Typeface.createFromAsset(context.getAssets(), fontName));
    }

    public static void initializeFonts(Context context, ArrayList<String> fontNames) {
        for (int f = 0; f < fontNames.size(); f++) {
            initializeFont(context, fontNames.get(f));
        }
    }

    public static Typeface getTypeface(int typefaceIndex) {
        return typefaces.get(typefaceIndex);
    }

    public float getBiggestFontSize(int typefaceIndex, int width, String text) {
        float fontSize = 100.0f;
        fontPaint.setTypeface(typefaces.get(typefaceIndex));
        fontPaint.setTextSize(fontSize);
        float textWidth = fontPaint.measureText(text);
        while (textWidth > width && fontSize > 0.0f) {
            fontSize -= 0.5f;
            fontPaint.setTextSize(fontSize);
            textWidth = fontPaint.measureText(text);
        }

        return fontSize;
    }

    public float getTextWidth(int typefaceIndex, float size, String text) {
        fontPaint.setTypeface(typefaces.get(typefaceIndex));
        fontPaint.setTextSize(size);
        return fontPaint.measureText(text);
    }

    public float getTextHeight(int typefaceIndex, float size, String text) {
        fontPaint.setTypeface(typefaces.get(typefaceIndex));
        fontPaint.setTextSize(size);
        Rect bounds = new Rect();
        fontPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }
}
