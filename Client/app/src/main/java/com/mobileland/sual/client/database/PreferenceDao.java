package com.mobileland.sual.client.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.mobileland.sual.client.model.UserPreference;
import java.util.ArrayList;
import java.util.List;

public class PreferenceDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public PreferenceDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertPreference(UserPreference preference) {
        ContentValues values = new ContentValues();
        values.put("notice_category", preference.getNoticeCategory());
        values.put("preference_score", preference.getPreferenceScore());
        values.put("keyword", preference.getKeyword());

        return database.insert("user_preferences", null, values);
    }

    public boolean updatePreference(UserPreference preference) {
        ContentValues values = new ContentValues();
        values.put("preference_score", preference.getPreferenceScore());
        values.put("keyword", preference.getKeyword());

        return database.update("user_preferences",
                values,
                "notice_category = ?",
                new String[]{preference.getNoticeCategory()}) > 0;
    }

    public UserPreference getPreferenceByCategory(String category) {
        Cursor cursor = database.query("user_preferences",
                null,
                "notice_category = ?",
                new String[]{category},
                null, null, null);

        UserPreference preference = null;
        if (cursor != null && cursor.moveToFirst()) {
            preference = cursorToPreference(cursor);
            cursor.close();
        }
        return preference;
    }

    public List<UserPreference> getPreferencesByKeyword(String keyword) {
        List<UserPreference> preferences = new ArrayList<>();
        Cursor cursor = database.query("user_preferences",
                null,
                "keyword LIKE ?",
                new String[]{"%" + keyword + "%"},
                null, null, "preference_score DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                preferences.add(cursorToPreference(cursor));
            }
            cursor.close();
        }
        return preferences;
    }

    private UserPreference cursorToPreference(Cursor cursor) {
        String category = cursor.getString(cursor.getColumnIndex("notice_category"));
        int score = cursor.getInt(cursor.getColumnIndex("preference_score"));
        String keyword = cursor.getString(cursor.getColumnIndex("keyword"));
        UserPreference preference = new UserPreference(category, score, keyword);
        preference.setId(cursor.getLong(cursor.getColumnIndex("id")));
        preference.setCreatedAt(cursor.getString(cursor.getColumnIndex("created_at")));
        return preference;
    }
}