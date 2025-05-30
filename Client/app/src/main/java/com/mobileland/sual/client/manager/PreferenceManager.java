package com.mobileland.sual.client.manager;

import android.content.Context;
import android.os.AsyncTask;
import com.mobileland.sual.client.database.PreferenceDao;
import com.mobileland.sual.client.model.UserPreference;
import java.util.List;

@SuppressWarnings("deprecation")
public class PreferenceManager {
    private PreferenceDao preferenceDao;
    private Context context;

    public PreferenceManager(Context context) {
        this.context = context;
        preferenceDao = new PreferenceDao(context);
    }

    public void updateUserPreference(final String category, final int score, final String keyword) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                preferenceDao.open();
                try {
                    UserPreference preference = preferenceDao.getPreferenceByCategory(category);
                    if (preference == null) {
                        preference = new UserPreference(category, score, keyword);
                        preferenceDao.insertPreference(preference);
                    } else {
                        preference.setPreferenceScore(score);
                        preference.setKeyword(keyword);
                        preferenceDao.updatePreference(preference);
                    }
                } finally {
                    preferenceDao.close();
                }
                return null;
            }
        }.execute();
    }

    public interface OnPreferenceLoadedListener {
        void onPreferenceLoaded(int score);
    }

    public void getPreferenceScore(final String category, final OnPreferenceLoadedListener listener) {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                preferenceDao.open();
                try {
                    UserPreference preference = preferenceDao.getPreferenceByCategory(category);
                    return preference != null ? preference.getPreferenceScore() : 0;
                } finally {
                    preferenceDao.close();
                }
            }

            @Override
            protected void onPostExecute(Integer score) {
                if (listener != null) {
                    listener.onPreferenceLoaded(score);
                }
            }
        }.execute();
    }

    public void getPreferencesByKeyword(final String keyword, final OnPreferencesLoadedListener listener) {
        new AsyncTask<Void, Void, List<UserPreference>>() {
            @Override
            protected List<UserPreference> doInBackground(Void... voids) {
                preferenceDao.open();
                try {
                    return preferenceDao.getPreferencesByKeyword(keyword);
                } finally {
                    preferenceDao.close();
                }
            }

            @Override
            protected void onPostExecute(List<UserPreference> preferences) {
                if (listener != null) {
                    listener.onPreferencesLoaded(preferences);
                }
            }
        }.execute();
    }

    public interface OnPreferencesLoadedListener {
        void onPreferencesLoaded(List<UserPreference> preferences);
    }
}