package com.dronami.twistnsisters;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.ArrayList;

public interface Game {
    public Input getInput();
    public FileIO getFileIO();
    public Graphics getGraphics();
    public Audio getAudio();
    public FontManager getFontManager();
    public SharedPreferences getSharedPreferences();

    public void setScreen(Screen screen);
    public Screen getCurrentScreen();
    public Screen getStartScreen();

    public void commitToSharedPrefs(String key, int value);
    public void commitToSharedPrefs(String key, String value);

    static class ColorManager {
        public static final int textColor = 0xFFFBC02D;
        public static final int darkShadowColor = 0xFF282828;
        public static final int lightShadowColor = 0xFF3C3C3C;

        public static final int twistaColor = 0xFFFBC02D;
        public static final int gemColorSets[][] = new int[][] {
                { 0xFF06E7FF, 0xFFA9F34E, 0xFFAE52D4, 0xFFFF5185 }
        };
        public static final int uiColorSets[][] = new int[][] {
                { 0xFF673AB7, 0xFF320B86, 0xFF9A67EA }
        };

        public static int blendColors(int start, int end, float ratio) {
            int sR = Color.red(start);
            int sG = Color.green(start);
            int sB = Color.blue(start);

            int eR = Color.red(end);
            int eG = Color.green(end);
            int eB = Color.blue(end);

            return Color.rgb((int)(sR+((eR-sR)*ratio)), (int)(sG+((eG-sG)*ratio)), (int)(sB+((eB-sB)*ratio)));
        }
    }

    static class SharedPrefData {
        public static final String GAME_MODE_KEY = "gameMode";
        public static final String GAME_LEVEL_KEY = "gameLevel";
        public static final String GAME_SPEED_KEY = "gameSpeed";
        public static final String GAME_GEMTYPE_KEY = "gameGemType";
        public static final String TRANSITION_TYPE_KEY = "transitionType";

        public static final int GAME_MODE_DEFAULT = 0;
        public static final int GAME_LEVEL_DEFAULT = 5;
        public static final int GAME_SPEED_DEFAULT = 1;
        public static final int GAME_GEMTYPE_DEFAULT = 0;
        public static final int TRANSITION_TYPE_DEFAULT = 0;
    }
}
