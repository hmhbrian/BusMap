// SearchHistory.java
package com.example.busmap.entities;

import java.io.Serializable;

public class SearchHistory implements Serializable {
    private station station;
    private long timestamp;
    private String userId;

    public SearchHistory() {
    }

    public SearchHistory(String userId, station station) {
        this.userId = userId;
        this.station = station;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public station getStation() {
        return station;
    }

    public void setStation(station station) {
        this.station = station;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}