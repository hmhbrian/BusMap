package com.example.busmap.Route;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    private String routeId;
    private List<LatLng> stationLocations = new ArrayList<>();
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private RecyclerView rVStationList;
    private StationAdapter StationAdapter;
    private ArrayList<station> StationList = new ArrayList<>();

    void init(){
        rVStationList = findViewById(R.id.rv_RouteDetail);
        rVStationList.setLayoutManager(new LinearLayoutManager(this));
        bottomSheet = findViewById(R.id.bottom_sheet_layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        init();
        routeId = getIntent().getStringExtra("route_id");
        databaseRef = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maproute);
        mapFragment.getMapAsync(this);


        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setPeekHeight(350);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(true);

        setupBottomSheetCallback();
    }

    private void setupBottomSheetCallback() {
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d("BottomSheet", "Expanded");
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d("BottomSheet", "Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d("BottomSheet", "Hidden");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (mMap != null) {
                    mMap.setPadding(0, 0, 0, (int) (slideOffset * bottomSheet.getHeight()));
                }
            }
        });
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
                Map<Integer, station> stationMap = new HashMap<>();
                Drawable drawable = getDrawable(R.drawable.ic_station_big);
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);
                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    int id = snapshot.child("id").getValue(Integer.class);

                    if (stationIds.contains(id)) {
                        double lat = snapshot.child("lat").getValue(Double.class);
                        double lng = snapshot.child("lng").getValue(Double.class);
                        String name = snapshot.child("name").getValue(String.class);

                        station station = new station(id, name, lat, lng);
                        stationMap.put(id, station);

                        LatLng location = new LatLng(lat, lng);
                        // Add marker
//                        mMap.addMarker(new MarkerOptions().position(location).title(name));
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(name)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    }
                }

                for (int id : stationIds) {
                    if (stationMap.containsKey(id)) {
                        StationList.add(stationMap.get(id));
                    }
                }
                StationAdapter = new StationAdapter(StationList);
                rVStationList.setAdapter(StationAdapter);

                stationLocations.clear(); // Làm trống danh sách trước
                for (station sta : StationList) {
                    stationLocations.add(new LatLng(sta.getLatitude(), sta.getLongitude()));
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
                    .color(getResources().getColor(R.color.primary_600)); // Set màu tùy ý
            mMap.addPolyline(polylineOptions);
        }
    }
}
