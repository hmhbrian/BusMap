package com.example.busmap.Notification;

public class NewsModel {
    private String title;
    private String content;
    private String date;

    public NewsModel() {}  // Constructor mặc định cho Firebase

    public NewsModel(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
}
