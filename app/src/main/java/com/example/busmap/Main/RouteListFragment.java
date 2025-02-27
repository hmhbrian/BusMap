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
import androidx.fragment.app.Fragment;
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

public class RouteListFragment extends Fragment {
    private RecyclerView rVRouteList;
    private RouteAdapter routeAdapter;
    private ArrayList<route> routeList = new ArrayList<>();
    private Set<String> favoriteRoutes = new HashSet<>();
    private DatabaseReference databaseReference;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_route_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rVRouteList = view.findViewById(R.id.rv_routList);
        rVRouteList.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();  // Chỉ gọi getUid() khi user != null
        } else {
            Log.e("FirebaseAuth", "Người dùng chưa đăng nhập!");
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        }
        loadFavoriteRoutes();
        fetchRoutesFromFirebase();
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
                    toggleFavoriteRoute(routeItem.getId(), isFavorite);
                },  route -> {
                    openBusRouteActivity(route.getId());
                });
                rVRouteList.setAdapter(routeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteRoutes() {
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("User")
                .child(userId)
                .child("favorite_routes");
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteRoutes.clear();
                // Lấy key của từng node (chính là id tuyến đã lưu)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    favoriteRoutes.add(dataSnapshot.getKey());
                }
                if (routeAdapter != null) {
                    routeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void toggleFavoriteRoute(String routeId, boolean isFavorite) {
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("User")
                .child(userId)
                .child("favorite_routes");
        if (isFavorite) {
            // Lưu id của tuyến xe thay vì giá trị true
            favRef.child(routeId).setValue(routeId);
        } else {
            favRef.child(routeId).removeValue();
        }
    }

    private void openBusRouteActivity(String route_id) {
        Intent intent = new Intent(getContext(), BusRouteActivity.class);
        intent.putExtra("route_id", route_id);
        startActivity(intent);
    }
}