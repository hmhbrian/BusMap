package com.example.busmap.Route;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RouteListFragment extends Fragment {
    private RecyclerView rVRouteList;
    private RouteAdapter routeAdapter;
    private ArrayList<route> routeList = new ArrayList<>();
    private DatabaseReference databaseReference;
    void init(View view){
        rVRouteList = view.findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(getContext()));

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_route_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        fetchRoutesFromFirebase();

    }
    private void fetchRoutesFromFirebase(){
        databaseReference = FirebaseDatabase.getInstance().getReference("route");
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
                    routeAdapter = new RouteAdapter(routeList, route -> {
                        // Chuyển qua BusRouteActivity
                        Intent intent = new Intent(getContext(), BusRouteActivity.class);
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
                Toast.makeText(getContext(), "Đã xảy ra lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
