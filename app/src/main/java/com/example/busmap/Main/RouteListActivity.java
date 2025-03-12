package com.example.busmap.Main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.Route.RouteDetail.BusRouteActivity;
import com.example.busmap.User.Login;
import com.example.busmap.entities.route;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RouteListActivity extends AppCompatActivity {
    private RecyclerView rVRouteList;
    private RouteAdapter routeAdapter;
    private ArrayList<route> routeList = new ArrayList<>();
    private Set<String> favoriteRoutes = new HashSet<>();
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);
        rVRouteList = findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(this));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();  // Chỉ gọi getUid() khi user != null
        } else {
            Log.e("FirebaseAuth", "Người dùng chưa đăng nhập!");
            Intent intent = new Intent(RouteListActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
        loadFavoriteRoutes();  // Tải các tuyến yêu thích
        fetchRoutesFromFirebase();  // Tải các tuyến từ Firebase
    }

    private void fetchRoutesFromFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("route");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    route routeItem = dataSnapshot.getValue(route.class);
                    if (routeItem != null) {
                        routeList.add(routeItem);
                    }
                }
                routeAdapter = new RouteAdapter(routeList, favoriteRoutes, (routeItem, isFavorite) -> {
                    toggleFavoriteRoute(routeItem.getId(), isFavorite);  // Lưu trạng thái yêu thích vào bảng Favorite
                }, new RouteAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(route routeItem) {
                        openBusRouteActivity(routeItem);
                        Toast.makeText(RouteListActivity.this, routeItem.getName() + "- id: " + routeItem.getId(), Toast.LENGTH_SHORT).show();
                    }
                });
                rVRouteList.setAdapter(routeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RouteListActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteRoutes() {
        // Truy cập vào bảng "Favorite" thay vì "FavoriteRoutes"
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("Favorite")
                .child(userId);  // Lấy ID người dùng

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteRoutes.clear();
                // Lấy key của từng node (chính là id tuyến đã lưu)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    favoriteRoutes.add(dataSnapshot.getKey());  // Lấy ID của các tuyến yêu thích
                }
                if (routeAdapter != null) {
                    routeAdapter.notifyDataSetChanged();  // Cập nhật lại giao diện khi dữ liệu yêu thích thay đổi
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void toggleFavoriteRoute(String routeId, boolean isFavorite) {
        // Cập nhật trạng thái yêu thích tuyến trong bảng "Favorite"
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("Favorite")
                .child(userId);  // Lưu vào dưới ID người dùng

        if (isFavorite) {
            // Lưu ID tuyến xe vào bảng Favorite dưới ID người dùng
            favRef.child(routeId).setValue(true);
        } else {
            // Nếu không yêu thích nữa, xóa ID tuyến xe khỏi bảng
            favRef.child(routeId).removeValue();
        }
    }

    private void openBusRouteActivity(route routeItem) {
        Intent intent = new Intent(RouteListActivity.this, BusRouteActivity.class);
        intent.putExtra("route_id", routeItem.getId());
        intent.putExtra("route_name", routeItem.getName());
        startActivity(intent);
    }
}
