package com.example.busmap.Route;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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


public class FindRoadActivity extends AppCompatActivity {
    private RecyclerView rVRouteList;
    private RouteAdapter routeAdapter;
    private ArrayList<route> routeList = new ArrayList<>();
    void init(){
        rVRouteList = findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_road_result);
        init();
        fetchRoutesFromFirebase();
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
                    routeAdapter = new RouteAdapter(routeList);
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
}