package com.mobileland.sual.client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "syu_notices.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_PREFERENCE_TABLE =
            "CREATE TABLE user_preferences (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "notice_category TEXT NOT NULL, " +
                    "preference_score INTEGER DEFAULT 0, " +
                    "keyword TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PREFERENCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user_preferences");
        onCreate(db);
    }
}