package com.example.busmap.entities;

import java.io.Serializable;

public class route implements Serializable {
    private String id;
    private String name;
    private int start_station_id;
    private int end_station_id;
    private double price;
    private String operation;

    public route() {
    }

    public route(int end_station_id, String id, String name, String operation, double price, int start_station_id) {
        this.end_station_id = end_station_id;
        this.id = id;
        this.name = name;
        this.operation = operation;
        this.price = price;
        this.start_station_id = start_station_id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getEnd_station_id() {
        return end_station_id;
    }

    public void setEnd_station_id(int end_station_id) {
        this.end_station_id = end_station_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStart_station_id() {
        return start_station_id;
    }

    public void setStart_station_id(int start_station_id) {
        this.start_station_id = start_station_id;
    }
}
