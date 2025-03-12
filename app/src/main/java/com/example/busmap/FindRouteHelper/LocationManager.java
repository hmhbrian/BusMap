package com.example.busmap.FindRouteHelper;

import com.google.android.gms.maps.model.LatLng;

public class LocationManager {
    private static LocationManager instance;
    private LocationData toLocation;
    private LatLng CurrentLocation;

    private LocationManager() { }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    public void setToLocation(LocationData to) {
        this.toLocation = to;
    }

    public LocationData getToLocation() {
        return toLocation;
    }
    public void setLatLng(LatLng latLng) {
        this.CurrentLocation = latLng;
    }

    public LatLng getLatLng() {
        return CurrentLocation;
    }
}
