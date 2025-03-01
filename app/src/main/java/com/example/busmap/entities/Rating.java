package com.example.busmap.entities;

public class Rating {
    private String name;
    private int starRating;
    private String time;
    private String note;

    public Rating() {
        // Default constructor required for calls to DataSnapshot.getValue(Rating.class)
    }

    public Rating(String name, int starRating, String time, String note) {
        this.name = name;
        this.starRating = starRating;
        this.time = time;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStarRating() {
        return starRating;
    }

    public void setStarRating(int starRating) {
        this.starRating = starRating;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
