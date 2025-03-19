package com.example.busmap.Route.RouteDetail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.busmap.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RouteInfoFragment extends Fragment {
    private TextView txtRouteName, txtOperationTime, txtTicketPrice, txtGoRoute, txtReturnRoute;
    private LinearLayout layoutGoRoute, layoutReturnRoute;
    private DatabaseReference databaseRef;
    private String routeId;

    public static RouteInfoFragment newInstance(String routeId) {
        RouteInfoFragment fragment = new RouteInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_route_info, container, false);

        // Ánh xạ TextView và Layout
        txtRouteName = view.findViewById(R.id.txtRouteName);
        txtOperationTime = view.findViewById(R.id.txtOperationTime);
        txtTicketPrice = view.findViewById(R.id.txtTicketPrice);
        txtGoRoute = view.findViewById(R.id.txtGoRoute);
        txtReturnRoute = view.findViewById(R.id.txtReturnRoute);
        layoutGoRoute = view.findViewById(R.id.layoutGoRoute);
        layoutReturnRoute = view.findViewById(R.id.layoutReturnRoute);


        if (routeId != null) {
            fetchRouteInfo(routeId);
        } else {
            Log.e("RouteInfoFragment", "Không tìm thấy route_id");
        }
        // Hiệu ứng hiển thị
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(500).start();

        return view;
    }

    private void fetchRouteInfo(String routeId) {
        databaseRef = FirebaseDatabase.getInstance().getReference();

        Log.d("RouteInfoFragment", "Đang lấy dữ liệu cho route_id: " + routeId);

        databaseRef.child("route").orderByChild("id").equalTo(routeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot routeSnapshot) {
                        if (!routeSnapshot.exists()) {
                            Log.e("Firebase", "Không có dữ liệu cho route_id: " + routeId);
                            return;
                        }

                        for (DataSnapshot snapshot : routeSnapshot.getChildren()) {
                            String routeName = snapshot.child("name").getValue(String.class);
                            String operationTime = snapshot.child("operation").getValue(String.class);
                            Long ticketPrice = snapshot.child("price").getValue(Long.class);

                            Log.d("Firebase", "Tên tuyến: " + routeName);
                            Log.d("Firebase", "Giờ hoạt động: " + operationTime);
                            Log.d("Firebase", "Giá vé: " + ticketPrice);

                            if (routeName != null) txtRouteName.setText(routeName);
                            if (operationTime != null) txtOperationTime.setText(operationTime);
                            if (ticketPrice != null) txtTicketPrice.setText(ticketPrice + " VND");

                            fetchStationsForRoute(routeId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Không thể lấy dữ liệu route", error.toException());
                    }
                });
    }


    private void fetchStationsForRoute(String routeId) {
        databaseRef.child("busstop").orderByChild("route_id").equalTo(routeId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot busStopSnapshot) {
                        List<Integer> stationIds = new ArrayList<>();
                        for (DataSnapshot snapshot : busStopSnapshot.getChildren()) {
                            Integer stationId = snapshot.child("station_id").getValue(Integer.class);
                            if (stationId != null) {
                                stationIds.add(stationId);
                            }
                        }

                        // Gọi hàm lấy chi tiết trạm dừng
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
                List<String> stationNames = new ArrayList<>();
                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id != null && name != null && stationIds.contains(id)) {
                        stationNames.add(name);
                    }
                }

                // Sắp xếp theo thứ tự của danh sách stationIds
                List<String> orderedGoRoute = new ArrayList<>();
                for (int id : stationIds) {
                    for (String name : stationNames) {
                        if (stationNames.indexOf(name) == stationIds.indexOf(id)) {
                            orderedGoRoute.add(name);
                            break;
                        }
                    }
                }

                // Định dạng danh sách trạm thành chuỗi
                if (!orderedGoRoute.isEmpty()) {
                    txtGoRoute.setText(String.join(" - ", orderedGoRoute));
                    layoutGoRoute.setVisibility(View.VISIBLE);

                    // Lượt về là ngược lại lượt đi
                    Collections.reverse(orderedGoRoute);
                    txtReturnRoute.setText(String.join(" - ", orderedGoRoute));
                    layoutReturnRoute.setVisibility(View.VISIBLE);
                } else {
                    layoutGoRoute.setVisibility(View.GONE);
                    layoutReturnRoute.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Không thể lấy dữ liệu station", error.toException());
            }
        });
    }
}
