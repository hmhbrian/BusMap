package com.example.busmap.busstopnear;

public class BusStation {
    private int id;
    private String name;
    private double lat;
    private double lng;
    private double distance; // Distance from user's location

    public BusStation(int id, String name, double lat, double lng, double distance) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getDistance() {
        return distance;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getFormattedDistance() {
        if (distance < 1000) {
            return String.format("%.0f m", distance);
        } else {
            return String.format("%.1f km", distance / 1000);
        }
    }
}
