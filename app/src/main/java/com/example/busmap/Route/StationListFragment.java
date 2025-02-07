package com.example.busmap.Route;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        // Xử lý khi nhấn vào một trạm
        stationAdapter.setOnItemClickListener(station -> {
            if (getActivity() instanceof BusRouteActivity) {
                ((BusRouteActivity) getActivity()).moveToStation(station.getLatitude(), station.getLongitude(), station.getId());
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

        // Bước 1: Lấy danh sách station_id theo route_id từ busstop
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

                        // Gọi bước 2: Lấy chi tiết thông tin các trạm từ "station"
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

                // Cập nhật danh sách stationList
                stationList.clear();
                for (int id : stationIds) {
                    if (stationMap.containsKey(id)) {
                        stationList.add(stationMap.get(id));
                    }
                }

                // Cập nhật RecyclerView
                stationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Không thể lấy dữ liệu station", error.toException());
            }
        });
    }
}
