package com.dronami.gametemplate;

// Based on the framework described in
// Beginning Android Games (Second Edition)
// by Mario Zechner and Robert Green

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

import java.io.IOException;

public class AndroidAudio implements Audio {
    AssetManager assetManager;
    SoundPool soundPool;

    public AndroidAudio(Activity activity) {
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.assetManager = activity.getAssets();
        this.soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
    }

    public Music newMusic(String fileName) {
        try {
            AssetFileDescriptor assetDescriptor = assetManager.openFd(fileName);
            return new AndroidMusic(assetDescriptor);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load music <" + fileName + ">");
        }
    }

    public Sound newSound(String fileName) {
        try {
            AssetFileDescriptor assetDescriptor = assetManager.openFd(fileName);
            int soundId = soundPool.load(assetDescriptor, 0);
            return new AndroidSound(soundPool, soundId);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load sound <" + fileName +">");
        }
    }
}
