package com.mobileland.sual.client.utils;  // ✅ 제일 위에 위치해야 함

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_IS_DARK = "is_dark";

    public static void applyTheme(Context context) {
        boolean isDark = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_DARK, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void saveTheme(Context context, boolean isDark) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IS_DARK, isDark)
                .apply();
    }

    public static boolean isDark(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_DARK, false);
    }
}