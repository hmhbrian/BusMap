package com.example.busmap.Notification;

public class NewsModel {
    private String title;
    private String content;
    private String date;

    public NewsModel() {}  // Constructor mặc định cho Firebase

    public NewsModel(String content, String date,String title) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDate() { return date; }
}
