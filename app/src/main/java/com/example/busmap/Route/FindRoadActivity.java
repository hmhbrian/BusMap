package com.example.busmap.Route;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;
import com.example.busmap.entities.station;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FindRoadActivity extends AppCompatActivity {
    private RecyclerView rVRouteList;
    private RouteAdapter routeAdapter;
    private Button btnFind;
    private EditText edtTo;
    double radiusKm;
    private ArrayList<route> routeList = new ArrayList<>();
    void init(){
        rVRouteList = findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(this));
        btnFind = findViewById(R.id.btnFind);
        edtTo = findViewById(R.id.tv_to);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_road_result);
        init();
        fetchRoutesFromFirebase();
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = edtTo.getText().toString();
                getCoordinatesFromAddress(address,FindRoadActivity.this);
            }
        });
    }
    private void fetchRoutesFromFirebase(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("route");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routeList.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    route route = dataSnapshot.getValue(route.class);
//                    if (route != null) {
//                        routeList.add(route);
//                    }
//                }
                GenericTypeIndicator<List<route>> typeIndicator = new GenericTypeIndicator<List<route>>() {};
                List<route> firebaseRouteList = snapshot.getValue(typeIndicator); // Lấy danh sách từ Firebase
                if (firebaseRouteList != null) {
                    routeList.addAll(firebaseRouteList);// Thêm tất cả vào danh sách
                    routeAdapter = new RouteAdapter(routeList,route -> {
                        // Chuyển qua BusRouteActivity
                        Intent intent = new Intent(FindRoadActivity.this, BusRouteActivity.class);
                        intent.putExtra("route_id", route.getId()); // Truyền id qua Intent
                        startActivity(intent);
                    });
                    rVRouteList.setAdapter(routeAdapter);
                }
                routeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error loading routes: " + error.getMessage());
                Toast.makeText(FindRoadActivity.this, "Đã xảy ra lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getCoordinatesFromAddress(String address, Context context) {
        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();
                double initialRadiusKm = 2.0; // Bán kính ban đầu
                double incrementKm = 3.0;     // Bước mở rộng bán kính
                //Toast.makeText(FindRoadActivity.this,"Latitude: " + latitude + ", Longitude: " + longitude,Toast.LENGTH_SHORT).show();
                Log.d("Coordinates", "Latitude: " + userLat + ", Longitude: " + userLng);
                findNearestStationWithIncrementalRadius(userLat, userLng, initialRadiusKm, incrementKm);
            } else {
                Log.e("Geocoding", "Không tìm thấy tọa độ cho địa chỉ: " + address);
            }
        } catch (IOException e) {
            Log.e("Geocoding", "Lỗi khi xử lý Geocoder: ", e);
        }
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
    public void findStationsInBoundingBox(double userLat, double userLng, double radiusKm, OnStationsFoundListener listener) {
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

                listener.onStationsFound(nearbyStations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onError(databaseError.toException());
            }
        });
    }

    public interface OnStationsFoundListener {
        void onStationsFound(List<station> stations);
        void onError(Exception e);
    }

    //tìm trạm gần nhất từ danh sách đã lọc
    public station findNearestStation(double userLat, double userLng, List<station> stations) {
        station nearestStation = null;
        float minDistance = Float.MAX_VALUE;

        for (station sta : stations) {
            float distance = calculateDistance(userLat, userLng, sta.getLatitude(), sta.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                nearestStation = sta;
            }
        }

        return nearestStation;
    }

    public float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }

    public void findNearestStationWithIncrementalRadius(double userLat, double userLng, double initialRadiusKm, double incrementKm) {
        radiusKm = initialRadiusKm;

        findStationsInBoundingBox(userLat, userLng, radiusKm, new OnStationsFoundListener() {
            @Override
            public void onStationsFound(List<station> stations) {
                if (!stations.isEmpty()) {
                    station nearestStation = findNearestStation(userLat, userLng, stations);
                    Toast.makeText(FindRoadActivity.this,"Name: " + nearestStation.getName() + ", Lat: " + nearestStation.getLatitude() +
                            ", Lng: " + nearestStation.getLongitude(),Toast.LENGTH_SHORT).show();
                    Log.d("Nearest Station", "Name: " + nearestStation.getName() +
                            ", Lat: " + nearestStation.getLatitude() +
                            ", Lng: " + nearestStation.getLongitude());
                } else {
                    Log.d("Stations", "Không tìm thấy trạm trong bán kính " + radiusKm + " km. Mở rộng bán kính.");
                    radiusKm += incrementKm;
                    findStationsInBoundingBox(userLat, userLng, radiusKm, this);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firebase", "Error reading data", e);
            }
        });
    }

}