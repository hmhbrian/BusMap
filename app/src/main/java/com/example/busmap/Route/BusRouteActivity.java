package com.example.busmap.Route;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusRouteActivity extends AppCompatActivity implements OnMapReadyCallback{
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    private String routeId;
    private List<LatLng> stationLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_route);
        routeId = getIntent().getStringExtra("route_id");

        databaseRef = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maproute);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadRouteStations();
    }
    private void loadRouteStations() {
        databaseRef.child("busstop").orderByChild("route_id").equalTo(routeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot busStopSnapshot) {
                        List<Integer> stationIds = new ArrayList<>();

                        for (DataSnapshot snapshot : busStopSnapshot.getChildren()) {
                            int stationId = snapshot.child("station_id").getValue(Integer.class);
                            stationIds.add(stationId);
                        }

                        // Sort by order
                        Collections.sort(stationIds);

                        // Fetch station details
                        loadStationDetails(stationIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Failed to fetch bus stops", error.toException());
                    }
                });
    }

    private void loadStationDetails(List<Integer> stationIds) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    int id = snapshot.child("id").getValue(Integer.class);

                    if (stationIds.contains(id)) {
                        double lat = snapshot.child("lat").getValue(Double.class);
                        double lng = snapshot.child("lng").getValue(Double.class);
                        String name = snapshot.child("name").getValue(String.class);

                        LatLng location = new LatLng(lat, lng);
                        stationLocations.add(location);

                        // Add marker
                        mMap.addMarker(new MarkerOptions().position(location).title(name));
                    }
                }

                // Draw polyline
                drawPolyline();

                // Move camera to the first station
                if (!stationLocations.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stationLocations.get(0), 14));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch stations", error.toException());
            }
        });
    }
    private void drawPolyline() {
        if (stationLocations.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(stationLocations)
                    .width(8)
                    .color(getResources().getColor(R.color.red)); // Set màu tùy ý
            mMap.addPolyline(polylineOptions);
        }
    }
}