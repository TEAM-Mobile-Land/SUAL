package com.mobileland.sual.client.model;

public class UserPreference {
    private long id;
    private String noticeCategory;
    private int preferenceScore;
    private String createdAt;
    private String keyword;

    public UserPreference(String noticeCategory, int preferenceScore, String keyword) {
        this.noticeCategory = noticeCategory;
        this.preferenceScore = preferenceScore;
        this.keyword = keyword;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getNoticeCategory() {
        return noticeCategory;
    }

    public int getPreferenceScore() {
        return preferenceScore;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getKeyword() {
        return keyword;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setNoticeCategory(String noticeCategory) {
        this.noticeCategory = noticeCategory;
    }

    public void setPreferenceScore(int preferenceScore) {
        this.preferenceScore = preferenceScore;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}