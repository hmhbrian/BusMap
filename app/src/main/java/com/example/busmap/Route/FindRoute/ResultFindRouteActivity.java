package com.example.busmap.Route.FindRoute;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.FindRouteHelper.RouteInfo;
import com.example.busmap.FindRouteHelper.RouteResult;
import com.example.busmap.R;
import com.example.busmap.entities.BusStop;
import com.example.busmap.FindRouteHelper.LocationData;
import com.example.busmap.entities.route;
import com.example.busmap.entities.station;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private LinearLayout Lnr_NoRoute;
    private EditText edtTo, edtFrom;
    double radiusKm;
    private DatabaseReference database;
    Map<Integer, station> stationMap = new HashMap<>();
    Map<String, RouteInfo> routeMap = new HashMap<>();
    //Map<String, String> routeMap = new HashMap<>();  // Lưu route_id -> route_name
    List<BusStop> busStops = new ArrayList<>();
    LocationData from_location, to_location;
    int choice;
    private ArrayList<route> routeList = new ArrayList<>();

    void init(){
        rVRouteList = findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(ResultFindRouteActivity.this));
        rVRouteList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        Lnr_NoRoute = findViewById(R.id.lnr_NoRoute);
        edtTo = findViewById(R.id.tv_to);
        edtFrom = findViewById(R.id.tv_from);
        database = FirebaseDatabase.getInstance().getReference();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_route_result);
        init();
        LoadData();
        //fetchRoutesFromFirebase();
        from_location = getIntent().getParcelableExtra("From_Location");
        to_location = getIntent().getParcelableExtra("To_Location");
        choice = getIntent().getIntExtra("choice",0);
        if(from_location != null && to_location != null){
            edtFrom.setText(from_location.getName());
            edtTo.setText(to_location.getName());
            FindRouteBetween2Points(from_location,to_location);
        }

    }

    public void LoadData(){
        // Lấy danh sách station
        database.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    int id = snapshot.child("id").getValue(Integer.class);
                    String name = snapshot.child("name").getValue(String.class);
                    double lat = snapshot.child("lat").getValue(Double.class);
                    double lng = snapshot.child("lng").getValue(Double.class);
                    stationMap.put(id, new station(id, name, lat, lng));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Lấy danh sách route để ánh xạ route_id -> route_name
        database.child("route").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    double cost = snapshot.child("price").getValue(Double.class);
                    routeMap.put(id, new RouteInfo(name, cost));
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

    public void FindRouteBetween2Points(LocationData from, LocationData to) {
        try {
            if (from != null && to != null) {
                double initialRadiusKm = 2.0; // Bán kính ban đầu
                double incrementKm = 3.0;     // Bước mở rộng bán kính
                //Log.d("Coordinates", "Latitude: " + userLat + ", Longitude: " + userLng);
                //findNearestStationWithIncrementalRadius(userLat, userLng, initialRadiusKm, incrementKm);
                findNearestStationsForTwoPoints(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), initialRadiusKm, incrementKm)
                        .thenRun(() -> Log.d("Success", "Successfully found both stations"))
                        .exceptionally(e -> {
                            Log.e("Error", "Failed to find stations", e);
                            return null;
                        });

            }
        } catch (Exception e) {
            Log.e("ERROR", "Lỗi khi xử lý : ", e);
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
        Double minDistance = Double.MAX_VALUE;

        for (station sta : stations) {
            Double distance = calculateDistance(userLat, userLng, sta.getLat(), sta.getLng());
            if (distance < minDistance) {
                minDistance = distance;
                nearestStation = sta;
            }
        }

        return nearestStation;
    }

    //Tính khoảng cách giữa 2 tọa độ
//    public float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
//        float[] results = new float[1];
//        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
//        return results[0]/1000;
//    }
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in kilometers
    }

    public double calculateTime(double distance, double speed) {
        return distance / speed; // time in hours
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

                    List<RouteResult> searchResults = findRoutes(station1.getId(), station2.getId(), busStops);
                    Log.d("Station","station 1: " + station1.getName());
                    Log.d("Station","station 2: " + station2.getName());
                    Log.d("BusStop ","Size busStop: "+busStops.size());
                    Log.d("Result ","Size result: "+searchResults.size());
                    for (RouteResult result : searchResults) {
                        StringBuilder sb = new StringBuilder("Route: ");
                        for (String route : result.getRouteNames()
                        ) {
                            sb.append(route).append(" -> ");
                        }
                        Log.d("Result", sb.toString());
                    }
//                    rVRouteList.setVisibility(View.VISIBLE);
//                    RouteFindAdapter adapter = new RouteFindAdapter(ResultFindRouteActivity.this, searchResults);
//                    rVRouteList.setAdapter(adapter);
                    if(searchResults.size() > 0){
                        rVRouteList.setVisibility(View.VISIBLE);
                        RouteFindAdapter adapter = new RouteFindAdapter(ResultFindRouteActivity.this, searchResults);
                        rVRouteList.setAdapter(adapter);
                    }else{
                        Lnr_NoRoute.setVisibility(View.VISIBLE);
                    }
                });
    }

    public List<RouteResult> findRoutes(int stationA, int stationB, List<BusStop> busStops) {
        //Set<List<String>> uniqueResults = new HashSet<>();
        List<RouteResult> results = new ArrayList<>();
        Map<String, List<BusStop>> routeStopMap = new HashMap<>();

        // Gom các BusStop theo route_id
        for (BusStop stop : busStops) {
            routeStopMap.computeIfAbsent(stop.route_id, k -> new ArrayList<>()).add(stop);
        }

        if(choice == 0){ // Tìm tuyến trực tiếp
            results = find_OneRoutes(stationA,stationB, routeStopMap);
        } else if (choice == 1) { // Tìm tuyến có trung chuyển
            results = find_TwoRoutes(stationA,stationB, routeStopMap);
        }
        return results;
    }

    public List<RouteResult> find_OneRoutes(int stationA, int stationB, Map<String, List<BusStop>> routeStopMap) {
        List<RouteResult> results = new ArrayList<>();
        final double averageSpeed = 30.0; // km/h

        for (String routeId : routeStopMap.keySet()) {
            List<BusStop> stops = routeStopMap.get(routeId);
            stops.sort(Comparator.comparingInt(s -> s.order));

            boolean foundA = false;
            double totalDistance = 0;
            double totalTime = 0;

            //Lưu danh sách trạm theo routeId
            Map<String, List<Integer>> stationMapForRoute = new HashMap<>();
            List<Integer> stationsList = new ArrayList<>();;

            for (int i = 0; i < stops.size() - 1; i++) {
                BusStop currentStop = stops.get(i);
                BusStop nextStop = stops.get(i + 1);
                if (currentStop.station_id == stationA) {
                    foundA = true;
                    stationsList.add(currentStop.station_id);
                }
                if (foundA) {
                    station currentStation = stationMap.get(currentStop.station_id);
                    station nextStation = stationMap.get(nextStop.station_id);
                    stationsList.add(nextStation.getId());

                    double distance = calculateDistance(currentStation.getLat(), currentStation.getLng(), nextStation.getLat(), nextStation.getLng());
                    totalDistance += distance;
                    totalTime += calculateTime(distance, averageSpeed);
                }
                if (foundA && nextStop.station_id == stationB) {
                    RouteInfo routeInfo = routeMap.get(routeId);
                    //Toast.makeText(ResultFindRouteActivity.this,"RouteId:" + routeId.toString(), Toast.LENGTH_SHORT).show();

                    stationMapForRoute.put(routeInfo.getRouteName(), new ArrayList<>(stationsList));

                    results.add(new RouteResult(
                            Collections.singletonList(routeInfo.getRouteName()),
                            routeInfo.getCost(),
                            totalDistance,
                            totalTime,
                            stationMapForRoute));
                    break;
                }
            }
        }
        return results;
    }

    public List<RouteResult> find_TwoRoutes(int stationA, int stationB, Map<String, List<BusStop>> routeStopMap) {
        List<RouteResult> results = new ArrayList<>();
        final double averageSpeed = 30.0; // km/h

        results = find_OneRoutes(stationA,stationB, routeStopMap);

        for (String routeId1 : routeMap.keySet()) {
            List<BusStop> stops1 = routeStopMap.get(routeId1);
            if (stops1 == null) continue;
            stops1.sort(Comparator.comparingInt(s -> s.order));

            for (BusStop stopA : stops1) {
                if (stopA.station_id != stationA) continue;

                double distanceToC = 0;
                double timeToC = 0;
                boolean foundC = false;

                List<Integer> stationsList1 = new ArrayList<>();
                stationsList1.add(stopA.station_id);

                for (BusStop stopC : stops1) {
                    if (stopC.order <= stopA.order) continue;

                    station station_A = stationMap.get(stopA.station_id);
                    station station_C = stationMap.get(stopC.station_id);
                    stationsList1.add(stopC.station_id);

                    distanceToC += calculateDistance(station_A.getLat(), station_A.getLng(), station_C.getLat(), station_C.getLng());
                    timeToC = calculateTime(distanceToC, averageSpeed);

                    for (String routeId2 : routeMap.keySet()) {
                        if (routeId1.equals(routeId2)) continue;

                        List<BusStop> stops2 = routeStopMap.get(routeId2);
                        if (stops2 == null) continue;
                        stops2.sort(Comparator.comparingInt(s -> s.order));

                        double distanceToB = 0;
                        double timeToB = 0;
                        boolean foundB = false;

                        List<Integer> stationsList2 = new ArrayList<>();
                        station station_C2, station_B;
                        for (int i = 0; i < stops2.size(); i++) {
                            BusStop stop = stops2.get(i);
                            if (stop.station_id == stopC.station_id) {
                                foundC = true;
                                stationsList2.add(stop.station_id);
                            }
                            if (foundC) {
                                stationsList2.add(stop.station_id);
                                if(stop.station_id != stopC.station_id) {
                                    BusStop Previous_stop = stops2.get(i-1);
                                    station_C2 = stationMap.get(Previous_stop.station_id);
                                    station_B = stationMap.get(stop.station_id);
                                    distanceToB += calculateDistance(station_C2.getLat(), station_C2.getLng(), station_B.getLat(), station_B.getLng());
                                }else{
                                    station_C2 = stationMap.get(stopC.station_id);
                                    station_B = stationMap.get(stop.station_id);
                                    distanceToB += calculateDistance(station_C2.getLat(), station_C2.getLng(), station_B.getLat(), station_B.getLng());
                                }
                                timeToB = calculateTime(distanceToB, averageSpeed);
                            }
                            if (foundC && stop.station_id == stationB) {
                                RouteInfo routeInfo1 = routeMap.get(routeId1);
                                RouteInfo routeInfo2 = routeMap.get(routeId2);
                                double totalCost = routeInfo1.getCost() + routeInfo2.getCost();
                                double totalDistance = distanceToC + distanceToB;
                                double totalTime = timeToC + timeToB;

                                // Lưu danh sách các trạm theo tuyến
                                Map<String, List<Integer>> stationMapForRoutes = new HashMap<>();
                                stationMapForRoutes.put(routeInfo1.getRouteName(), new ArrayList<>(stationsList1));
                                stationMapForRoutes.put(routeInfo2.getRouteName(), new ArrayList<>(stationsList2));


                                results.add(new RouteResult(
                                        Arrays.asList(routeInfo1.getRouteName(), routeInfo2.getRouteName()),
                                        totalCost, totalDistance, totalTime, stationMapForRoutes));

                                foundB = true;
                                break;
                            }
                        }
                        if (foundB) break;
                    }
                    if (foundC) break;
                }
            }
        }
        return results;
    }
}