package com.example.busmap.Route.FindRoute;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.BusStop;
import com.example.busmap.entities.route;
import com.example.busmap.entities.station;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class ResultFindRouteActivity extends AppCompatActivity {
    private RecyclerView rVRouteList;
    private Button btnFind;
    private EditText edtTo;
    double radiusKm;
    private DatabaseReference database;
    Map<String, String> routeMap = new HashMap<>();  // Lưu route_id -> route_name
    List<BusStop> busStops = new ArrayList<>();
    private ArrayList<route> routeList = new ArrayList<>();

    void init(){
        rVRouteList = findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(ResultFindRouteActivity.this));
        rVRouteList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        btnFind = findViewById(R.id.btnFind);
        edtTo = findViewById(R.id.tv_to);
        database = FirebaseDatabase.getInstance().getReference();;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_route_result);
        init();
        LoadData();
        //fetchRoutesFromFirebase();
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = edtTo.getText().toString();
                getCoordinatesFromAddress(address);
            }
        });

    }

    public void LoadData(){
        // Lấy danh sách route để ánh xạ route_id -> route_name
        database.child("route").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    routeMap.put(id, name);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Lấy danh sách busstop
        database.child("busstop").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BusStop stop = snapshot.getValue(BusStop.class);
                    busStops.add(stop);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

//    private void fetchRoutesFromFirebase(){
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("route");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                routeList.clear();
//                GenericTypeIndicator<List<route>> typeIndicator = new GenericTypeIndicator<List<route>>() {};
//                List<route> firebaseRouteList = snapshot.getValue(typeIndicator); // Lấy danh sách từ Firebase
//                if (firebaseRouteList != null) {
//                    routeList.addAll(firebaseRouteList);// Thêm tất cả vào danh sách
//                    routeAdapter = new RouteAdapter(routeList,route -> {
//                        // Chuyển qua BusRouteActivity
//                        Intent intent = new Intent(FindRoadActivity.this, BusRouteActivity.class);
//                        intent.putExtra("route_id", route.getId()); // Truyền id qua Intent
//                        startActivity(intent);
//                    });
//                    rVRouteList.setAdapter(routeAdapter);
//                }
//                routeAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("FirebaseError", "Error loading routes: " + error.getMessage());
//                Toast.makeText(FindRoadActivity.this, "Đã xảy ra lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    //chuyển địa chỉ thành tọa độ
    public void getCoordinatesFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
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
                //findNearestStationWithIncrementalRadius(userLat, userLng, initialRadiusKm, incrementKm);
                findNearestStationsForTwoPoints(10.964776144481784, 106.6676283098276, userLat, userLng, initialRadiusKm, incrementKm)
                        .thenRun(() -> Log.d("Success", "Successfully found both stations"))
                        .exceptionally(e -> {
                            Log.e("Error", "Failed to find stations", e);
                            return null;
                        });

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

    //Tính khoảng cách giữa 2 tọa độ
    public float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }

    public CompletableFuture<station> findNearestStationWithIncrementalRadius(double userLat, double userLng, double initialRadiusKm, double incrementKm) {
        CompletableFuture<station> future = new CompletableFuture<>();
        radiusKm = initialRadiusKm;

        findStationsInBoundingBox(userLat, userLng, radiusKm, new OnStationsFoundListener() {
            @Override
            public void onStationsFound(List<station> stations) {
                if (!stations.isEmpty()) {
                    station nearestStation = findNearestStation(userLat, userLng, stations);
                    future.complete(nearestStation); // Hoàn thành CompletableFuture
                } else {
                    //Log.d("Stations", "Không tìm thấy trạm trong bán kính " + radiusKm + " km. Mở rộng bán kính.");
                    radiusKm += incrementKm;
                    findStationsInBoundingBox(userLat, userLng, radiusKm, this);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firebase", "Error reading data", e);
            }
        });
        return future;
    }

    public CompletableFuture<Void> findNearestStationsForTwoPoints(
            double lat1, double lng1,
            double lat2, double lng2,
            double initialRadiusKm, double incrementKm) {

        CompletableFuture<station> station1Future = findNearestStationWithIncrementalRadius(lat1, lng1, initialRadiusKm, incrementKm);
        CompletableFuture<station> station2Future = findNearestStationWithIncrementalRadius(lat2, lng2, initialRadiusKm, incrementKm);

        return CompletableFuture.allOf(station1Future, station2Future)
                .thenAccept((Void) -> {
                    station station1 = station1Future.join();
                    station station2 = station2Future.join();
                    //findDirectRoutes(station1, station2);
                    //BusRouteFinder busRouteFinder = new BusRouteFinder();

                    List<List<String>> searchResults = findRoutes(station1.getId(), station2.getId(), busStops);
                    Log.d("Station","station 1: " + station1.getName());
                    Log.d("Station","station 2: " + station2.getName());
                    Log.d("BusStop ","Size busStop: "+busStops.size());
                    Log.d("Result ","Size result: "+searchResults.size());
                    for (List<String> result : searchResults) {
                        StringBuilder sb = new StringBuilder("Route: ");
                        for (String route : result) {
                            sb.append(route).append(" -> ");
                        }
                        Log.d("Result", sb.toString());
                    }

                    RouteFindAdapter adapter = new RouteFindAdapter(ResultFindRouteActivity.this, searchResults);
                    rVRouteList.setAdapter(adapter);

                });
    }

    public List<List<String>> findRoutes(int stationA, int stationB, List<BusStop> busStops) {
        Set<List<String>> uniqueResults = new HashSet<>();
        //List<List<String>> results = new ArrayList<>();
        Map<String, List<BusStop>> routeStopMap = new HashMap<>();

        // Gom các BusStop theo route_id
        for (BusStop stop : busStops) {
            routeStopMap.computeIfAbsent(stop.route_id, k -> new ArrayList<>()).add(stop);
        }

        // Tìm tuyến trực tiếp
        for (String routeId : routeStopMap.keySet()) {
            List<BusStop> stops = routeStopMap.get(routeId);
            stops.sort(Comparator.comparingInt(s -> s.order));

            boolean foundA = false;
            for (BusStop stop : stops) {
                if (stop.station_id == stationA) {
                    foundA = true;
                }
                if (foundA && stop.station_id == stationB) {
                    uniqueResults.add(Collections.singletonList(routeMap.get(routeId)));  // Lưu tên tuyến thay vì ID
                    break;
                }
            }
        }

        // Tìm tuyến có trung chuyển
        for (String routeId1 : routeMap.keySet()) {
            List<BusStop> stops1 = routeStopMap.get(routeId1);
            if (stops1 == null) continue;
            stops1.sort(Comparator.comparingInt(s -> s.order));

            for (BusStop stopA : stops1) {
                if (stopA.station_id != stationA) continue;

                for (BusStop stopC : stops1) {
                    if (stopC.order <= stopA.order) continue; // Đảm bảo C đến sau A

                    // Tìm tuyến thứ 2
                    for (String routeId2 : routeMap.keySet()) {
                        if (routeId1.equals(routeId2)) continue; // Tránh trùng tuyến

                        List<BusStop> stops2 = routeStopMap.get(routeId2);
                        stops2.sort(Comparator.comparingInt(s -> s.order));

                        boolean foundC = false;
                        for (BusStop stop : stops2) {
                            if (stop.station_id == stopC.station_id) {
                                foundC = true;
                            }
                            if (foundC && stop.station_id == stationB) {
                                uniqueResults.add(Arrays.asList(routeMap.get(routeId1), routeMap.get(routeId2)));  // Lưu tên tuyến
                                break;
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(uniqueResults);
    }



//    private void findDirectRoutes(station stationA, station stationB) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference routesRef = database.getReference("busstop");
//
//        routesRef.orderByChild("station_id").equalTo(stationA.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<String> routesFromA = new ArrayList<>();
//                Map<String, Integer> orderFromA = new HashMap<>(); // Lưu order của StationA trên mỗi route
//
//                // Lấy danh sách các tuyến qua trạm A và lưu order
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    String routeId = snapshot.child("route_id").getValue(String.class);
//                    Integer order = snapshot.child("order").getValue(Integer.class);
//
//                    if (routeId != null && order != null) {
//                        routesFromA.add(routeId);
//                        orderFromA.put(routeId, order);
//                    }
//                }
//
//                // Kiểm tra tuyến qua trạm B
//                routesRef.orderByChild("station_id").equalTo(stationB.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        List<String> directRoutes = new ArrayList<>();
//
//                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                            String routeId = snapshot.child("route_id").getValue(String.class);
//                            Integer orderB = snapshot.child("order").getValue(Integer.class);
//
//                            if (routeId != null && orderB != null && routesFromA.contains(routeId)) {
//                                Integer orderA = orderFromA.get(routeId);
//
//                                // Kiểm tra điều kiện order của StationA < StationB
//                                if (orderA != null && orderA < orderB) {
//                                    directRoutes.add(routeId);
//                                }
//                            }
//                        }
//
//                        if (!directRoutes.isEmpty()) {
//                            // Hiển thị danh sách tuyến trực tiếp
//                            showDirectRoutes(directRoutes);
//                        } else {
//                            routeList.clear();
//                            // Không tìm thấy tuyến trực tiếp
//                            showNoDirectRouteMessage();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Log.e("Firebase", "Error fetching data", databaseError.toException());
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("Firebase", "Error fetching data", databaseError.toException());
//            }
//        });
//    }
//
//
//    private void showDirectRoutes(List<String> routeIds) {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("route");
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                routeList.clear();
//                GenericTypeIndicator<List<route>> typeIndicator = new GenericTypeIndicator<List<route>>() {};
//                List<route> firebaseRouteList = snapshot.getValue(typeIndicator); // Lấy danh sách từ Firebase
//                if (firebaseRouteList != null) {
//                    for (route r : firebaseRouteList) {
//                        // Kiểm tra nếu id của route nằm trong routeIds
//                        if (routeIds.contains(r.getId())) {
//                            routeList.add(r); // Thêm vào routeList nếu tìm thấy
//                        }
//                    }
//                    routeAdapter = new RouteAdapter(routeList,route -> {
//                        // Chuyển qua BusRouteActivity
//                        Intent intent = new Intent(FindRoadActivity.this, BusRouteActivity.class);
//                        intent.putExtra("route_id", route.getId()); // Truyền id qua Intent
//                        startActivity(intent);
//                    });
//                    rVRouteList.setAdapter(routeAdapter);
//                }
//                routeAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("FirebaseError", "Error loading routes: " + error.getMessage());
//                Toast.makeText(FindRoadActivity.this, "Đã xảy ra lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void showNoDirectRouteMessage() {
//        Toast.makeText(this, "Không tìm thấy tuyến trực tiếp", Toast.LENGTH_SHORT).show();
//    }

}