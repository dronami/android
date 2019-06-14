package com.dronami.twistnsisters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

public class AndroidGame extends Activity implements Game {
    static final boolean STRETCH_SCREEN = false;
    static final int TARGET_SCREEN_SHORT = 320;
    static final int TARGET_SCREEN_LONG = 480;

    private static final String SHARED_PREF_NAME = "TwistnPrefs";

    AndroidFastRenderView renderView;
    View.OnTouchListener touchListener;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    PowerManager.WakeLock wakeLock;
    FontManager fontManager;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        boolean isLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        int frameBufferWidth;
        int frameBufferHeight;
        if (STRETCH_SCREEN) {
            frameBufferWidth = isLandscape ? TARGET_SCREEN_LONG : TARGET_SCREEN_SHORT;
            frameBufferHeight = isLandscape ? TARGET_SCREEN_SHORT : TARGET_SCREEN_LONG;
        } else {
            frameBufferWidth = isLandscape ? getWindowManager().getDefaultDisplay().getHeight()
                    : getWindowManager().getDefaultDisplay().getWidth();
            frameBufferHeight = isLandscape ? getWindowManager().getDefaultDisplay().getWidth()
                    : getWindowManager().getDefaultDisplay().getHeight();
        }
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth, frameBufferHeight,
                Bitmap.Config.RGB_565);

        fontManager = FontManager.getInstance();
        fontManager.initializeFont(this, "futur.otf");

        float scaleX = (float)frameBufferWidth / getWindowManager().getDefaultDisplay().getWidth();
        float scaleY = (float)frameBufferHeight / getWindowManager().getDefaultDisplay().getHeight();

        renderView = new AndroidFastRenderView(this, frameBuffer);
        touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouched(v, event);

                return true;
            }
        };
        renderView.setOnTouchListener(touchListener);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        //input = new AndroidInput(this, renderView, scaleX, scaleY);
        screen = getStartScreen();
        setContentView(renderView);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Dronami:GLGame");

        sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        screen.resume();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();

        if (isFinishing()) {
            screen.dispose();
        }
    }

    public Input getInput() {
        return input;
    }

    public FileIO getFileIO() {
        return fileIO;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public Audio getAudio() {
        return audio;
    }

    public FontManager getFontManager() {
        if (fontManager == null) {
            fontManager = FontManager.getInstance();
        }
        return fontManager;
    }

    public SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREF_NAME, 0);
        }
        return sharedPreferences;
    }

    public void commitToSharedPrefs(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void commitToSharedPrefs(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setScreen(Screen screen) {
        if (screen == null) {
            throw new IllegalArgumentException("Screen must not be null");
        }

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }

    public Screen getCurrentScreen() {
        return screen;
    }

    public Screen getStartScreen() {
        return null;
    }


    public boolean onTouched(View v, MotionEvent event) {
        screen.handleTouchEvent(event);

        return true;
    }
}
