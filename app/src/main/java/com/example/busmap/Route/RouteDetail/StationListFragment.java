package com.example.busmap.Route.RouteDetail;

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
import com.example.busmap.busstopnear.RadaBusActivity;
import com.example.busmap.dialog.StationDetailsDialog;
import com.example.busmap.entities.route;
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

    // Factory method tạo instance theo routeId
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        stationAdapter = new StationAdapter(stationList);
        recyclerView.setAdapter(stationAdapter);

        // Đặt callback cho StationAdapter
        stationAdapter.setOnStationClickListener(new StationAdapter.OnStationClickListener() {
            @Override
            public void onItemClick(station station) {
                // Ví dụ: Di chuyển đến vị trí của trạm khi click item (được xử lý trong BusRouteActivity)
                if (getActivity() instanceof BusRouteActivity) {
                    ((BusRouteActivity) getActivity()).moveToStation(
                            station.getLat(),
                            station.getLng(),
                            station.getId());
                }
            }

            @Override
            public void onDetailsClick(int stationId) {
                // Khi nhấn nút "Chi tiết", hiển thị dialog thông tin các tuyến
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

    // Lấy danh sách busstop theo route được chọn, thu thập danh sách station_ids
    private void fetchStationsForRoute(String routeId) {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("busstop")
                .orderByChild("route_id")
                .equalTo(routeId)
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

    // Dựa vào danh sách id trạm, truy vấn chi tiết đầy đủ từ node "station"
    private void fetchStationDetails(List<Integer> stationIds) {
        databaseRef.child("station")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                        Map<Integer, station> stationMap = new HashMap<>();
                        for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                            Integer id = snapshot.child("id").getValue(Integer.class);
                            Double lat = snapshot.child("lat").getValue(Double.class);
                            Double lng = snapshot.child("lng").getValue(Double.class);
                            String name = snapshot.child("name").getValue(String.class);

                            if (id == null || lat == null || lng == null || name == null) {
                                Log.e("Firebase", "Dữ liệu station bị lỗi, bỏ qua.");
                                continue;
                            }

                            if (stationIds.contains(id)) {
                                station stationObj = new station(id, name, lat, lng);
                                stationMap.put(id, stationObj);
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

    // Lấy danh sách các busstop có station_id = stationId, thu thập danh sách route_ids
    private void showRoutesForStation(final int stationId) {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("busstop")
                .orderByChild("station_id")
                .equalTo(stationId)
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
                        fetchRouteDetails(routeIds, stationId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi lấy dữ liệu trạm", error.toException());
                    }
                });
    }

    // Truy vấn thông tin chi tiết của các tuyến bus theo danh sách routeIds
    private void fetchRouteDetails(List<String> routeIds, final int stationId) {
        databaseRef.child("route")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<route> routeDetails = new ArrayList<>(); // Sử dụng List<route> thay vì List<String>

                        for (DataSnapshot routeSnapshot : snapshot.getChildren()) {
                            String id = routeSnapshot.child("id").getValue(String.class);
                            String name = routeSnapshot.child("name").getValue(String.class);
                            String operation = routeSnapshot.child("operation").getValue(String.class);

                            // Các giá trị mặc định cho những thuộc tính chưa có
                            int startStationId = 0; // Giá trị mặc định
                            int endStationId = 0;   // Giá trị mặc định
                            double price = 0.0;      // Giá trị mặc định

                            // Kiểm tra và thêm đối tượng route vào danh sách nếu phù hợp
                            if (id != null && name != null && operation != null && routeIds.contains(id)) {
                                routeDetails.add(new route(endStationId, id, name, operation, price, startStationId));
                            }
                        }

                        // Sử dụng lớp StationDetailsDialog để hiển thị dialog với thông tin chi tiết tuyến bus
                        new StationDetailsDialog(getContext(), stationId, routeDetails).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi lấy dữ liệu tuyến bus", error.toException());
                    }
                });
    }
}