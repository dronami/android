package com.dronami.brightcitylights

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

public enum class PuzzleTypes(val value: Int) {
    NORMAL(0),
    X(1),
    TWOCOLOR(2),
    LITONLY(3),
    LITONLYX(4)
}

object SettingsContract {
    object SettingsEntry: BaseColumns {
        const val TABLE_NAME = "settings"
        const val SETTINGS_SOUND = "sound"
        const val SETTINGS_SIZE = "size"
        const val SETTINGS_EXPERT = "map"

        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${SettingsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${SettingsEntry.SETTINGS_SOUND} INT," +
                    "${SettingsEntry.SETTINGS_SIZE} INT," +
                    "${SettingsEntry.SETTINGS_EXPERT} TINYINT)"

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SettingsEntry.TABLE_NAME}"
    }
}

object PuzzlesContract {
    object PuzzleEntry: BaseColumns {
        const val TABLE_NAME = "puzzles"
        const val PUZZLE_SIZE = "size"
        const val PUZZLE_TYPE = "type"
        const val PUZZLE_MAP = "map"
        const val PUZZLE_MAP2 = "map2"

        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${PuzzleEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${PuzzleEntry.PUZZLE_SIZE} INT," +
                    "${PuzzleEntry.PUZZLE_TYPE} INT," +
                    "${PuzzleEntry.PUZZLE_MAP} BIGINT," +
                    "${PuzzleEntry.PUZZLE_MAP2} BIGINT)"

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${PuzzleEntry.TABLE_NAME}"
    }
}

object MissionsContract {
    object MissionEntry: BaseColumns {
        const val TABLE_NAME = "missions"
        const val PUZZLE_ID = "puzzleid"
        const val MOVE_LIMIT = "movelimit"
        const val TIME_LIMIT = "timelimit"
        const val COMPLETED = "completed"

        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${MissionEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${MissionEntry.PUZZLE_ID} INT," +
                    "${MissionEntry.MOVE_LIMIT} INT," +
                    "${MissionEntry.TIME_LIMIT} BIGINT," +
                    "${MissionEntry.COMPLETED} TINYINT," +
                    "FOREIGN KEY(${MissionEntry.PUZZLE_ID}) REFERENCES ${PuzzlesContract.PuzzleEntry.TABLE_NAME}(${BaseColumns._ID}))"

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${MissionEntry.TABLE_NAME}"
    }
}

class PuzzlesDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(PuzzlesContract.PuzzleEntry.SQL_CREATE_ENTRIES)
        db.execSQL(MissionsContract.MissionEntry.SQL_CREATE_ENTRIES)
        db.execSQL(SettingsContract.SettingsEntry.SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(PuzzlesContract.PuzzleEntry.SQL_DELETE_ENTRIES)
        db.execSQL(MissionsContract.MissionEntry.SQL_DELETE_ENTRIES)
        db.execSQL(SettingsContract.SettingsEntry.SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Puzzles.db"
    }
}