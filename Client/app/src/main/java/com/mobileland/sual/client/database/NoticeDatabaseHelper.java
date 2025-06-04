// NoticeDatabaseHelper.java
package com.mobileland.sual.client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoticeDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "notices.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "notice";

    public NoticeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "content TEXT, " +
                "date TEXT, " +
                "url TEXT UNIQUE, " +
                "type TEXT, " +
                "aiSummary TEXT, " +
                "deadline TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
