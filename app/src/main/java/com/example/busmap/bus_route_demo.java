package com.example.busmap;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.MapView;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Waypoint;
import java.util.ArrayList;
import java.util.List;

public class bus_route_demo extends AppCompatActivity {
    private MapView mapView;
    private RoutingEngine routingEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_route_demo);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Load map scene
        mapView.getMapScene().loadScene(MapScene.Layers.NORMAL, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapScene.ErrorCode errorCode) {
                if (errorCode == null) {
                    mapView.getCamera().setTarget(new GeoCoordinates(10.983350, 106.674310));
                    mapView.getCamera().setZoomLevel(14);
                } else {
                    Log.e("HereMap", "Error loading map: " + errorCode.toString());
                }
            }
        });

        // Initialize the routing engine
        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            e.printStackTrace();
        }

        // Request route directions
        getDirections(new GeoCoordinates(10.983350, 106.674310), new GeoCoordinates(10.980180, 106.675613));
    }

    private void getDirections(GeoCoordinates origin, GeoCoordinates destination) {
        List<Waypoint> waypoints = new ArrayList<>();
        waypoints.add(new Waypoint(origin));
        waypoints.add(new Waypoint(destination));

        routingEngine.calculateRoute(
                waypoints,
                new RoutingEngine.CarOptions(),
                new RoutingEngine.Callback() {
                    @Override
                    public void onCalculateRouteFinished(RoutingError routingError, List<Route> routes) {
                        if (routingError == null) {
                            Route route = routes.get(0);
                            // Handle route: you can draw the polyline here or show route info
                            Log.d("Route Info", "Distance: " + route.getLengthInMeters() + " meters");
                        } else {
                            Log.e("HereMap", "Error calculating route: " + routingError.toString());
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
}
