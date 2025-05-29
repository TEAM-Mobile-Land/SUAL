package com.mobileland.sual.client.manager;

import android.content.Context;
import com.mobileland.sual.client.database.PreferenceDao;
import com.mobileland.sual.client.model.UserPreference;
import java.util.List;

public class PreferenceManager {
    private PreferenceDao preferenceDao;

    public PreferenceManager(Context context) {
        preferenceDao = new PreferenceDao(context);
    }

    public void updateUserPreference(String category, int score, String keyword) {
        preferenceDao.open();
        UserPreference preference = preferenceDao.getPreferenceByCategory(category);

        if (preference == null) {
            preference = new UserPreference(category, score, keyword);
            preferenceDao.insertPreference(preference);
        } else {
            preference.setPreferenceScore(score);
            preference.setKeyword(keyword);
            preferenceDao.updatePreference(preference);
        }
        preferenceDao.close();
    }

    public int getPreferenceScore(String category) {
        preferenceDao.open();
        UserPreference preference = preferenceDao.getPreferenceByCategory(category);
        preferenceDao.close();

        return preference != null ? preference.getPreferenceScore() : 0;
    }

    public List<UserPreference> getPreferencesByKeyword(String keyword) {
        preferenceDao.open();
        List<UserPreference> preferences = preferenceDao.getPreferencesByKeyword(keyword);
        preferenceDao.close();
        return preferences;
    }
}