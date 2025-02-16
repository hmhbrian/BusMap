package com.example.busmap.Favorites;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteRoutesFragment extends Fragment {
    private RecyclerView recyclerView;
    private GenericFavoriteAdapter<route> adapter;
    private ArrayList<route> favoriteRoutes = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_routes, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Khởi tạo adapter với mảng favoriteRoutes ban đầu rỗng
        adapter = new GenericFavoriteAdapter<>(favoriteRoutes, (routeItem, isFavorite) -> { });
        recyclerView.setAdapter(adapter);

        loadFavoriteRoutes();
        return view;
    }

    private void loadFavoriteRoutes() {
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("User")
                .child(userId)
                .child("favorite_routes");
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Xóa danh sách cũ
                favoriteRoutes.clear();
                // Duyệt qua các node trong favorite_routes; lưu id tuyến ở key
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Lấy id tuyến từ key (do khi lưu, key là id tuyến)
                    String routeId = dataSnapshot.getKey();
                    if (routeId != null) {
                        fetchRouteDetails(routeId);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sử dụng query để tìm kiếm một route có thuộc tính "id" bằng routeId
    private void fetchRouteDetails(String routeId) {
        DatabaseReference routeRef = FirebaseDatabase.getInstance().getReference("route");
        Query query = routeRef.orderByChild("id").equalTo(routeId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Vì nếu có kết quả, snapshot luôn trả về 1 hay nhiều node
                for (DataSnapshot ds : snapshot.getChildren()) {
                    route routeItem = ds.getValue(route.class);
                    if (routeItem != null) {
                        // Nếu chưa có trong danh sách, thêm vào
                        if (!favoriteRoutes.contains(routeItem)) {
                            favoriteRoutes.add(routeItem);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }
}