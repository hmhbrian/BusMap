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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeTableFragment extends Fragment {
    private RecyclerView recyclerView;
    private TimeTableAdapter timeTableAdapter;
    private List<String> arrivalTimes = new ArrayList<>();
    private DatabaseReference databaseRef;
    private String routeId;

    public static TimeTableFragment newInstance(String routeId) {
        TimeTableFragment fragment = new TimeTableFragment();
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
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        timeTableAdapter = new TimeTableAdapter(arrivalTimes);
        recyclerView.setAdapter(timeTableAdapter);

        if (routeId != null) {
            fetchTimeTable(routeId);
        } else {
            Log.e("TimeTableFragment", "Không tìm thấy route_id");
        }

        return view;
    }

    private void fetchTimeTable(String routeId) {
        databaseRef = FirebaseDatabase.getInstance().getReference("busstop");

        databaseRef.orderByChild("route_id").equalTo(routeId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot busStopSnapshot) {
                        arrivalTimes.clear();
                        for (DataSnapshot snapshot : busStopSnapshot.getChildren()) {
                            String arrivalTime = snapshot.child("arrival_time").getValue(String.class);
                            arrivalTimes.add(arrivalTime);
                        }

                        // Sắp xếp thời gian theo thứ tự
                        Collections.sort(arrivalTimes);

                        // Cập nhật RecyclerView
                        timeTableAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Không thể lấy dữ liệu bảng giờ", error.toException());
                    }
                });
    }
}
