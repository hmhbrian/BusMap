package com.example.busmap.Route;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationListFragment extends Fragment {
    private RecyclerView recyclerView;
    private StationAdapter stationAdapter;
    private ArrayList<station> stationList = new ArrayList<>();
    private DatabaseReference databaseRef;
    private String routeId;

    public static StationListFragment newInstance(String routeId) {
        StationListFragment fragment = new StationListFragment();
        Bundle args = new Bundle();
        args.putString("route_id", routeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeId = getArguments().getString("route_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        stationAdapter = new StationAdapter(stationList);
        recyclerView.setAdapter(stationAdapter);

        // Handle item click
        stationAdapter.setOnStationClickListener(new StationAdapter.OnStationClickListener() {
            @Override
            public void onItemClick(station station) {
                if (getActivity() instanceof BusRouteActivity) {
                    ((BusRouteActivity) getActivity()).moveToStation(station.getLatitude(), station.getLongitude(), station.getId());
                }
            }

            @Override
            public void onDetailsClick(int stationId) {
                showRoutesForStation(stationId);
            }
        });

        if (routeId != null) {
            fetchStationsForRoute(routeId);
        } else {
            Log.e("StationListFragment", "Không tìm thấy route_id");
        }

        return view;
    }

    private void fetchStationsForRoute(String routeId) {
        databaseRef = FirebaseDatabase.getInstance().getReference();

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

                        fetchStationDetails(stationIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Không thể lấy dữ liệu busstop", error.toException());
                    }
                });
    }

    private void fetchStationDetails(List<Integer> stationIds) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                Map<Integer, station> stationMap = new HashMap<>();
                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id == null || lat == null || lng == null || name == null) {
                        Log.e("Firebase", "Dữ liệu station bị lỗi, bỏ qua trạm này.");
                        continue;
                    }

                    if (stationIds.contains(id)) {
                        station station = new station(id, name, lat, lng);
                        stationMap.put(id, station);
                    }
                }

                stationList.clear();
                for (int id : stationIds) {
                    if (stationMap.containsKey(id)) {
                        stationList.add(stationMap.get(id));
                    }
                }

                stationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Không thể lấy dữ liệu station", error.toException());
            }
        });
    }

//    private void showRoutesForStation(int stationId) {
//        databaseRef.child("busstop").orderByChild("station_id").equalTo(stationId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        List<String> routeIds = new ArrayList<>();
//                        for (DataSnapshot busStopSnapshot : snapshot.getChildren()) {
//                            String routeId = busStopSnapshot.child("route_id").getValue(String.class);
//                            if (routeId != null) {
//                                routeIds.add(routeId);
//                            }
//                        }
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                        builder.setTitle("Các tuyến xe chạy qua trạm");
//                        builder.setMessage("Tuyến xe: " + String.join(", ", routeIds));
//                        builder.setPositiveButton("OK", null);
//                        builder.show();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.e("Firebase", "Không thể lấy dữ liệu route cho trạm", error.toException());
//                    }
//                });
//    }
    // Lấy danh sách tuyến bus đi qua trạm
    private void showRoutesForStation(int stationId) {
        databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("busstop").orderByChild("station_id").equalTo(stationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> routeIds = new ArrayList<>();
                        for (DataSnapshot busStopSnapshot : snapshot.getChildren()) {
                            String routeId = busStopSnapshot.child("route_id").getValue(String.class);
                            if (routeId != null) {
                                routeIds.add(routeId);
                            }
                        }
                        fetchRouteDetails(routeIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi lấy dữ liệu trạm", error.toException());
                    }
                });
    }

    // Lấy chi tiết tuyến bus
    private void fetchRouteDetails(List<String> routeIds) {
        databaseRef.child("route").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> routeDetails = new ArrayList<>();
                for (DataSnapshot routeSnapshot : snapshot.getChildren()) {
                    String id = routeSnapshot.child("id").getValue(String.class);
                    String name = routeSnapshot.child("name").getValue(String.class);
                    String operation = routeSnapshot.child("operation").getValue(String.class);

                    if (id != null && name != null && operation != null && routeIds.contains(id)) {
                        routeDetails.add("🚏 " + name + " (Hoạt động: " + operation + ")");
                    }
                }

                // Hiển thị danh sách tuyến bus
                showRouteDialog(routeDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi lấy dữ liệu tuyến bus", error.toException());
            }
        });
    }

    // Hiển thị danh sách tuyến bus trong `AlertDialog`
    private void showRouteDialog(List<String> routeDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Các tuyến bus đi qua trạm");

        if (routeDetails.isEmpty()) {
            builder.setMessage("Không có tuyến bus nào đi qua trạm này.");
        } else {
            String[] routesArray = routeDetails.toArray(new String[0]);
            builder.setItems(routesArray, null);
        }

        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
