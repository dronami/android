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
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class SliderBar {
    Rect borderRect;
    Rect bgRect;
    Rect juiceRect;

    int borderColor;
    static int startColor = 0xFFC62828;
    static int midColor = 0xFFFBC02D;
    static int endColor = 0xFF8BC34A;
    static int juiceColor = 0xFFFBC02D;
    static int bgColor = Game.ColorManager.darkShadowColor;
    int currentColor = startColor;
    private Paint juicePaint;
    private Paint tickPaint;
    private BitmapShader juiceShader;
    private PorterDuffColorFilter juiceColorFilter;
    private float lastJuiceOffset = 0.0f;
    private float juiceOffset = 0.0f;
    private float juiceOffsetSpeed = 40.0f;
    private Matrix juiceMatrix = new Matrix();
    private boolean barTouched = false;

    float currentRatio = 0.5f;

    ArrayList<Integer> tickXs = new ArrayList<>();

    static final float MARGIN_RATIO = 0.15f;

    public SliderBar(Rect borderRect, int borderColor, int numTicks, Game game) {
        this.borderRect = borderRect;
        this.borderColor = borderColor;
        int margin = (int)(borderRect.height() * MARGIN_RATIO);
        bgRect = new Rect(borderRect.left + margin, borderRect.top + margin,
                borderRect.right - margin, borderRect.bottom - margin);
        juiceRect = new Rect(bgRect);

        if (numTicks > 0) {
            for (int t = 0; t < numTicks; t++) {
                tickXs.add(t * (bgRect.width()/numTicks));
            }
        }

        Pixmap juicePixmap = game.getGraphics().newScaledPixmap("barjuice.png",
                Graphics.PixmapFormat.ARGB4444, juiceRect.height(), false);

        juicePaint = new Paint();
        juiceShader = new BitmapShader(((AndroidPixmap)juicePixmap).bitmap,
        Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            juicePaint.setShader(juiceShader);
        juiceColorFilter = new PorterDuffColorFilter(juiceColor, PorterDuff.Mode.MULTIPLY);
        juicePaint.setColorFilter(juiceColorFilter);
        juiceMatrix = new Matrix();
        juiceMatrix.postRotate(90.0f);
        juiceMatrix.postTranslate(juiceRect.left, juiceRect.top);
        juiceShader.setLocalMatrix(juiceMatrix);

        tickPaint = new Paint();
        tickPaint.setColor(Color.BLACK);

        setRatio(currentRatio);
    }

    public void update(float deltaTime) {
        lastJuiceOffset = juiceOffset;
        juiceOffset += juiceOffsetSpeed * deltaTime;
        float juiceDiff = juiceOffset - lastJuiceOffset;
        juiceMatrix.postTranslate(0.0f, juiceDiff);
        juiceShader.setLocalMatrix(juiceMatrix);
    }

    public void setRatio (float currentRatio) {
        this.currentRatio = currentRatio;

        if (currentRatio <= 0.5f) {
            currentColor = Game.ColorManager.blendColors(startColor, midColor, currentRatio/0.5f);
        } else {
            currentColor = Game.ColorManager.blendColors(midColor, endColor, (currentRatio-0.5f)/0.5f);
        }

        juiceColorFilter = new PorterDuffColorFilter(currentColor, PorterDuff.Mode.MULTIPLY);
        juicePaint.setColorFilter(juiceColorFilter);

        juiceRect.right = (int)(bgRect.left + (bgRect.width() * currentRatio));
    }

    public void draw(Graphics g) {
        g.drawRect(borderRect, borderColor);
        g.drawRect(bgRect, bgColor);

        for (int t = 1; t < tickXs.size(); t++) {
            g.drawLine(bgRect.left + tickXs.get(t), bgRect.top,
                    bgRect.left + tickXs.get(t), bgRect.bottom, Color.BLACK);
        }

        g.drawRect(juiceRect, juicePaint);
    }

    public float handleTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (bgRect.contains((int)event.getX(), (int)event.getY())) {
                barTouched = true;
                setRatio((event.getX() - bgRect.left) / (bgRect.right-bgRect.left));
                return currentRatio;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            barTouched = false;
        } else if (barTouched && event.getAction() == MotionEvent.ACTION_MOVE) {
            float touchedX = event.getX();
            if (touchedX < bgRect.left) {
                touchedX = bgRect.left;
            } else if (touchedX > bgRect.right) {
                touchedX = bgRect.right;
            }

            setRatio((touchedX - bgRect.left) / (bgRect.right-bgRect.left));
            return currentRatio;
        }

        return -1.0f;
    }
}
