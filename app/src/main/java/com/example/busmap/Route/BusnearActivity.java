package com.example.busmap.Route;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;
import com.example.busmap.entities.station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BusnearActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RouteAdapterBN routeAdapter;
    private List<route> nearbyRoutes;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView noRoutesMessage;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_nearhere);

        // Ánh xạ UI
        recyclerView = findViewById(R.id.busnear);
        noRoutesMessage = findViewById(R.id.noRoutesMessage);
        backButton = findViewById(R.id.back_button);

        // Sự kiện quay lại
        backButton.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        nearbyRoutes = new ArrayList<>();
        routeAdapter = new RouteAdapterBN(nearbyRoutes);
        recyclerView.setAdapter(routeAdapter);

        // Lấy vị trí người dùng
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getUserLocation();
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();
                findStationsInBoundingBox(10.980745592815111, 106.67550668123549,2.0);
            } else {
                Toast.makeText(this, "Không thể lấy vị trí", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Tính giới hạn tọa độ
    public double[] getBoundingBox(double userLat, double userLng, double radiusKm) {
        double earthRadius = 6371.0; // Bán kính Trái Đất (km)
        double deltaLat = radiusKm / earthRadius;
        double deltaLng = radiusKm / (earthRadius * Math.cos(Math.toRadians(userLat)));

        double latMin = userLat - Math.toDegrees(deltaLat);
        double latMax = userLat + Math.toDegrees(deltaLat);
        double lngMin = userLng - Math.toDegrees(deltaLng);
        double lngMax = userLng + Math.toDegrees(deltaLng);

        return new double[]{latMin, latMax, lngMin, lngMax};
    }

    //Lọc các trạm trong giới hạn tọa độ
    public void findStationsInBoundingBox(double userLat, double userLng, double radiusKm) {
        double[] boundingBox = getBoundingBox(userLat, userLng, radiusKm);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("station");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<station> nearbyStations = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double lat = snapshot.child("lat").getValue(Double.class);
                    double lng = snapshot.child("lng").getValue(Double.class);

                    if (lat >= boundingBox[0] && lat <= boundingBox[1]
                            && lng >= boundingBox[2] && lng <= boundingBox[3]) {
                        int id = snapshot.child("id").getValue(Integer.class);
                        String name = snapshot.child("name").getValue(String.class);
                        nearbyStations.add(new station(id, name, lat, lng));
                    }
                }
                fetchRoutesForStations(nearbyStations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error reading stations", databaseError.toException());
                Toast.makeText(BusnearActivity.this, "Lỗi kết nối với Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    private void findNearestRoutes(double userLat, double userLng) {
//        DatabaseReference stationsRef = FirebaseDatabase.getInstance().getReference("station");
//        stationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                List<station> nearbyStations = new ArrayList<>();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    // Debug dữ liệu Firebase
//                    Log.d("Firebase", "Dữ liệu nhận từ Firebase: " + snapshot.getValue());
//
//                    Double latValue = snapshot.child("lat").getValue(Double.class);
//                    Double lngValue = snapshot.child("lng").getValue(Double.class);
//                    Integer stationId = snapshot.child("id").getValue(Integer.class);
//                    String stationName = snapshot.child("name").getValue(String.class);
//
//                    // Kiểm tra nếu dữ liệu bị null
//                    if (latValue == null || lngValue == null) {
//                        // Thử lấy dưới dạng String và chuyển thành Double
//                        String latStr = snapshot.child("latitude").getValue(String.class);
//                        String lngStr = snapshot.child("longitude").getValue(String.class);
//
//                        if (latStr != null && lngStr != null) {
//                            try {
//                                latValue = Double.parseDouble(latStr);
//                                lngValue = Double.parseDouble(lngStr);
//                            } catch (NumberFormatException e) {
//                                Log.w("BusnearActivity", "⚠️ Không thể chuyển latitude/longitude từ String sang Double: " + snapshot.getKey());
//                                continue;
//                            }
//                        } else {
//                            Log.w("BusnearActivity", "❌ Latitude or Longitude is null for station: " + snapshot.getKey());
//                            continue;
//                        }
//                    }
//
//                    // Kiểm tra nếu trạm xe nằm trong bán kính 140m
//                    if (isWithinRadius(userLat, userLng, latValue, lngValue, 0.14)) {
//                        nearbyStations.add(new station(stationId, stationName, latValue, lngValue));
//                    }
//                }
//                fetchRoutesForStations(nearbyStations);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e("Firebase", "Error reading stations", databaseError.toException());
//                Toast.makeText(BusnearActivity.this, "Lỗi kết nối với Firebase", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private boolean isWithinRadius(double userLat, double userLng, double stationLat, double stationLng, double radiusKm) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, stationLat, stationLng, results);
        return results[0] <= radiusKm * 1000; // Chuyển km thành mét
    }

    private void fetchRoutesForStations(List<station> nearbyStations) {
        DatabaseReference routesRef = FirebaseDatabase.getInstance().getReference("route");
        routesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nearbyRoutes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    route route = snapshot.getValue(route.class);
                    for (station station : nearbyStations) {
                        if (route.getStart_station_id() == station.getId() || route.getEnd_station_id() == station.getId()) {
                            nearbyRoutes.add(route);
                            break;
                        }
                    }
                }

                // Hiển thị thông báo nếu không có tuyến xe gần
                if (nearbyRoutes.isEmpty()) {
                    noRoutesMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noRoutesMessage.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    routeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error reading routes", databaseError.toException());
                Toast.makeText(BusnearActivity.this, "Lỗi kết nối với Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
