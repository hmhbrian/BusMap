package com.example.busmap.FindRouteHelper;

public class RouteInfo {
    String routeName;
    double cost;

    public RouteInfo(String routeName, double cost) {
        this.routeName = routeName;
        this.cost = cost;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
