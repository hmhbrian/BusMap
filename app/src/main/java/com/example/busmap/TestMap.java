package com.example.busmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMap extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    private String routeId;
    private List<LatLng> stationLocations = new ArrayList<>();
    private ArrayList<station> StationList = new ArrayList<>();
    private Map<Integer, Marker> stationMarkers = new HashMap<>();
    private int selectedStationId = -1; // Lưu ID của trạm đang chọn


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        routeId = getIntent().getStringExtra("route_id");
        databaseRef = FirebaseDatabase.getInstance().getReference();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maproute);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    private void getCurrentLocation() {
        LatLng currentLocation = new LatLng(10.98335004747691, 106.67431075125997);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Zoom level 15
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) {
            Log.e("TestMap", "Không tìm thấy icon: " + vectorResId);
            return BitmapDescriptorFactory.defaultMarker(); // Trả về marker mặc định nếu lỗi
        }

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();
        loadAllRoutes();
    }

    private void loadAllRoutes() {
        databaseRef.child("route").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot routeSnapshot) {
                for (DataSnapshot route : routeSnapshot.getChildren()) {
                    String routeId = route.child("id").getValue(String.class);
                    if (routeId != null) {
                        loadRouteStations(routeId);  // Load trạm của từng tuyến
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch routes", error.toException());
            }
        });
    }

    private void loadRouteStations(String routeId) {
        databaseRef.child("busstop").orderByChild("route_id").equalTo(routeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot busStopSnapshot) {
                        List<Integer> stationIds = new ArrayList<>();
                        for (DataSnapshot snapshot : busStopSnapshot.getChildren()) {
                            Integer stationId = snapshot.child("station_id").getValue(Integer.class);
                            if (stationId != null) {
                                stationIds.add(stationId);
                            }
                        }
                        loadStationDetails(routeId, stationIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Failed to fetch bus stops", error.toException());
                    }
                });
    }


    private void loadStationDetails(String routeId, List<Integer> stationIds) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                Map<Integer, station> stationMap = new HashMap<>();
                List<LatLng> stationLocations = new ArrayList<>();

                if (TestMap.this.isFinishing() || mMap == null) {
                    Log.e("TestMap", "Activity đã bị hủy hoặc Google Map chưa sẵn sàng.");
                    return;
                }

                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id != null && lat != null && lng != null && name != null && stationIds.contains(id)) {
                        stationMap.put(id, new station(id, name, lat, lng));
                        stationLocations.add(new LatLng(lat, lng));

                        // Hiển thị marker trên bản đồ
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (mMap != null) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lat, lng))
                                        .title(name)
                                        .icon(bitmapDescriptorFromVector(R.drawable.ic_station_big)));
                                if (marker != null) {
                                    stationMarkers.put(id, marker);
                                }
                            }
                        });
                    }
                }

                // Vẽ tuyến đường với màu khác nhau
                drawPolyline(routeId, stationLocations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch stations", error.toException());
            }
        });
    }


    private void drawPolyline(String routeId, List<LatLng> stationLocations) {
        if (stationLocations.size() > 1) {
            int color = getRouteColor(routeId);  // Lấy màu cho tuyến

            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(stationLocations)
                    .width(8)
                    .color(color);

            mMap.addPolyline(polylineOptions);
        }
    }

    private int getRouteColor(String routeId) {
        int[] colors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA,
                Color.LTGRAY, Color.DKGRAY, Color.BLACK, Color.rgb(255, 165, 0) // Orange
        };
        int index = Integer.parseInt(routeId);
        return colors[index];
    }

}
