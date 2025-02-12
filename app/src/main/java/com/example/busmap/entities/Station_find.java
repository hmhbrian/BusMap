package com.example.busmap.entities;

import java.util.List;

public class Station_find {
    String id;            // Mã trạm
    String name;          // Tên trạm
    List<String> routes;  // Danh sách tuyến xe đi qua trạm

    public Station_find(String id, String name, List<String> routes) {
        this.id = id;
        this.name = name;
        this.routes = routes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getRoutes() {
        return routes;
    }
}
