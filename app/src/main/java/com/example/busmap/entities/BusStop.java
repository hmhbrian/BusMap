package com.example.busmap.entities;

import java.io.Serializable;

public class BusStop implements Serializable {
    public String route_id;
    public int station_id;
    public int order;
    public String arrival_time;

    public BusStop() {
    }

    public BusStop(String arrival_time, int order, String route_id, int station_id) {
        this.route_id = route_id;
        this.station_id = station_id;
        this.order = order;
        this.arrival_time = arrival_time;
    }

    public String getRoute_id() {
        return route_id;
    }

    public int getStation_id() {
        return station_id;
    }

    public int getOrder() {
        return order;
    }

    public String getArrival_time() {
        return arrival_time;
    }
}
