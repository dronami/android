package com.dronami.twistnsisters;

import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;

import java.util.ArrayList;

public class Sidebar {
    public static enum SidebarType {
        Time(0), Points(1),
        GemA(2), GemB(3), GemC(4), GemD(5),
        Target(6);

        private final int value;
        private SidebarType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    static boolean isInitialized = false;
    static ArrayList<Pixmap> iconPixmaps = new ArrayList<>();

    static Pixmap juicePixmap;
    private static Paint juicePaint;
    private static ColorFilter juiceColorFilter;

    public int sidebarType;
    Rect mainAreaRect;
    Rect iconRect;
    Rect barBorderRect;
    Rect barBGRect;
    Rect barJuiceRect;

    int borderColor;
    static int startColor = 0xFFC62828;
    static int midColor = 0xFFFBC02D;
    static int endColor = 0xFF8BC34A;
    static int juiceColor = 0xFFFBC02D;
    int currentColor = startColor;
    private BitmapShader juiceShader;
    private float lastJuiceOffset = 0.0f;
    private float juiceOffset = 0.0f;
    private float juiceOffsetSpeed = -25.0f;
    private Matrix juiceMatrix = new Matrix();

    float currentRatio = 0.0f;
    static final float JUICE_SPEED = 0.2f;
    float targetRatio;
    private final String filenames[] = { "clock-icon.png", "star-icon.png",
            "gem-0.png", "gem-1.png", "gem-2.png", "gem-3.png",
            "target-icon.png"};

    public Sidebar(int type, Rect mainAreaRect, Game game, int borderColor) {
        sidebarType = type;
        this.mainAreaRect = mainAreaRect;
        this.borderColor = borderColor;

        int marginSize = (int)(mainAreaRect.width() * 0.1f);
        iconRect = new Rect(mainAreaRect.left + marginSize, mainAreaRect.bottom - mainAreaRect.width() + marginSize,
                mainAreaRect.right - marginSize, mainAreaRect.bottom - marginSize);
        barBorderRect = new Rect(mainAreaRect.left + marginSize*2, mainAreaRect.top + marginSize*2,
                mainAreaRect.right - marginSize*2, iconRect.top - marginSize*2);
        barBGRect = new Rect(barBorderRect.left + marginSize, barBorderRect.top + marginSize,
                barBorderRect.right - marginSize, barBorderRect.bottom - marginSize);
        barJuiceRect = new Rect(barBGRect);
        barJuiceRect.top = barJuiceRect.bottom;

        if (!isInitialized) {
            for (int f = 0; f < filenames.length; f++) {
                iconPixmaps.add(game.getGraphics().newScaledPixmap(filenames[f],
                        Graphics.PixmapFormat.ARGB4444, iconRect.width(), false));
            }
            juicePixmap = game.getGraphics().newScaledPixmap("barjuice.png",
                    Graphics.PixmapFormat.ARGB4444, barJuiceRect.width(), false);
            isInitialized = true;
        }

        juicePaint = new Paint();
        juiceShader = new BitmapShader(((AndroidPixmap)juicePixmap).bitmap,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        juicePaint.setShader(juiceShader);
        juiceColorFilter = new PorterDuffColorFilter(juiceColor, PorterDuff.Mode.MULTIPLY);
        juicePaint.setColorFilter(juiceColorFilter);

        if (type == SidebarType.Time.getValue()) {
            setRatio(1.0f, true);
        } else {
            setRatio(0.0f, true);
        }

    }

    public void draw(Graphics g) {
        g.drawRect(barBorderRect, borderColor);
        g.drawRect(barBGRect, Color.BLACK);
        g.drawRect(barJuiceRect, juicePaint);
        g.drawPixmap(iconPixmaps.get(sidebarType), iconRect.left, iconRect.top);
    }

    public void update(float deltaTime) {
        if (isInitialized) {
            lastJuiceOffset = juiceOffset;
            juiceOffset += juiceOffsetSpeed * deltaTime;
            float juiceDiff = juiceOffset - lastJuiceOffset;
            juiceMatrix.postTranslate(0.0f, juiceDiff);
            juiceShader.setLocalMatrix(juiceMatrix);

            if (sidebarType == SidebarType.Time.getValue()) {
                Log.d("Assy", currentRatio +" vs. " +targetRatio);
            }

            if (currentRatio < targetRatio) {
                currentRatio += JUICE_SPEED * deltaTime;
                currentRatio = Math.min(currentRatio, targetRatio);
            } else if (currentRatio > targetRatio) {
                currentRatio -= JUICE_SPEED * deltaTime;
                currentRatio = Math.max(currentRatio, 0.0f);
            }
            barJuiceRect.top = (int)(barJuiceRect.bottom
                    - (barBGRect.bottom - barBGRect.top) * currentRatio);
        }
    }

    public void setRatio(float r) {
        targetRatio = r;
        if (targetRatio > 1.0f) {
            targetRatio = 1.0f;
        } else if (targetRatio < 0.0f) {
            targetRatio = 0.0f;
        }
        if (sidebarType == SidebarType.Time.getValue()) {
            currentRatio = targetRatio;
        }

        /*
        if (targetRatio <= 0.5f) {
            currentColor = blendColors(startColor, midColor, targetRatio/0.5f);
        } else {
            currentColor = blendColors(midColor, endColor, (targetRatio-0.5f)/0.5f);
        }
        */

        //juiceColorFilter = new PorterDuffColorFilter(currentColor, PorterDuff.Mode.MULTIPLY);
        //juicePaint.setColorFilter(juiceColorFilter);
    }

    public void setRatio(float r, boolean initial) {
        setRatio(r);
        if (initial) {
            currentRatio = r;
            barJuiceRect.top = (int)(barJuiceRect.bottom
                    - (barBGRect.bottom - barBGRect.top) * currentRatio);
        }
    }
}
