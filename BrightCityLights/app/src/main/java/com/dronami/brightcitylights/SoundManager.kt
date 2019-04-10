package com.dronami.brightcitylights

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build

object SoundManager {
    lateinit var soundPool: SoundPool
    lateinit var musicList: List<MediaPlayer>
    lateinit var musicPositions: MutableList<Int>
    var soundIds: MutableList<Int> = mutableListOf()
    var initialized: Boolean = false
    var silenced: Boolean = false

    public enum class Sounds (val value: Int){
        LIGHT_SELECT(0),
        SWITCH1(1),
        SWITCH2(2),
        OKAY(3),
        CANCEL(4),
        WHOOSH(5),
        STOPLIGHT(6),
        SELECT (7),
        SELECT_MISSION(8),
        GAME_WIN(9),
        GAME_LOSE(10)
    }

    fun initSoundManager(context: Context) {
        if (initialized) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = SoundPool.Builder()
                .setMaxStreams(10)
                .build()
        } else {
            soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 1)
        }
        soundIds.add(soundPool.load(context, R.raw.snap, 1))
        soundIds.add(soundPool.load(context, R.raw.switch1, 1))
        soundIds.add(soundPool.load(context, R.raw.switch2, 1))
        soundIds.add(soundPool.load(context, R.raw.wobble, 1))
        soundIds.add(soundPool.load(context, R.raw.cancel, 1))
        soundIds.add(soundPool.load(context, R.raw.whoosh, 1))
        soundIds.add(soundPool.load(context, R.raw.stoplight, 1))
        soundIds.add(soundPool.load(context, R.raw.blap, 1))
        soundIds.add(soundPool.load(context, R.raw.ufo, 1))
        soundIds.add(soundPool.load(context, R.raw.gamewon, 1))
        soundIds.add(soundPool.load(context, R.raw.gameover, 1))

        musicList = listOf(
            MediaPlayer.create(context, R.raw.music1),
            MediaPlayer.create(context, R.raw.music2))
        for (m in musicList) {
            m.isLooping = true
        }
        musicList[0].setVolume(0.2f, 0.2f)
        musicList[1].setVolume(0.55f, 0.55f)
        musicPositions = mutableListOf(0, 0)

        initialized = true
    }

    fun playSound(index: Int) {
        if (silenced) {
            return
        }
        soundPool.play(soundIds[index], 1f, 1f, 1, 0, 1.0f)
    }

    fun playMusic(index: Int) {
        if (silenced) {
            return
        }
        musicList[index].start()
    }

    fun resumeMusic(index: Int) {
        if (silenced || musicList[index].isPlaying) {
            return
        }
        musicList[index].seekTo(musicPositions[index])
        musicList[index].start()
    }

    fun pauseMusic(index: Int) {
        musicList[index].pause()
        musicPositions[index] = musicList[index].currentPosition
    }

}