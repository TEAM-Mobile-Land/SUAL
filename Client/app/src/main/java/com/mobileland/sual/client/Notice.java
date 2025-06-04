package com.mobileland.sual.client;

public class Notice {
    private String title;
    private String date;
    private String aiSummary;
    private String url;

    // 생성자
    public Notice(String title, String date, String aiSummary, String url) {
        this.title = title;
        this.date = date;
        this.aiSummary = aiSummary;
        this.url = url;
    }

    // Getter
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getAiSummary() { return aiSummary; }
    public String getUrl() { return url; }

    // Setter
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public void setUrl(String url) { this.url = url; }
}
