package com.example.busmap.FindRouteHelper;

import java.util.List;
import java.util.Map;

public class RouteResult {
    List<String> routeNames;
    double totalCost;
    double totalDistance;
    double totalTime;
    private Map<String, List<Integer>> stationsMap;

    public RouteResult( List<String> routeNames, double totalCost, double totalDistance, double totalTime, Map<String, List<Integer>> stationsMap) {
        this.routeNames = routeNames;
        this.totalCost = totalCost;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.stationsMap = stationsMap;
    }

    public List<String> getRouteNames() {
        return routeNames;
    }

    public void setRouteNames(List<String> routeNames) {
        this.routeNames = routeNames;
    }

    public Map<String, List<Integer>> getStationsMap() {
        return stationsMap;
    }

    public void setStationsMap(Map<String, List<Integer>> stationsMap) {
        this.stationsMap = stationsMap;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
}
