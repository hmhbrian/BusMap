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
import com.example.busmap.entities.station;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteStationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private GenericFavoriteAdapter<station> adapter;
    private ArrayList<station> favoriteStations = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_stations, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Khởi tạo adapter với danh sách rỗng và gán cho RecyclerView
        adapter = new GenericFavoriteAdapter<>(favoriteStations, (stationItem, isFavorite) -> {
            // Xử lý khi toggle favorite nếu cần
        });
        recyclerView.setAdapter(adapter);

        loadFavoriteStations();

        return view;
    }

    private void loadFavoriteStations() {
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("User")
                .child(userId)
                .child("favorite_stations");

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Xóa danh sách cũ
                favoriteStations.clear();
                // Duyệt qua các node trong favorite_stations
                for (DataSnapshot ds : snapshot.getChildren()) {
                    /*
                     * Nếu bạn lưu ID trạm bằng:
                     *    favRef.child(String.valueOf(stationId)).setValue(stationId);
                     *
                     * thì value của ds sẽ là một Long. Ta lấy value đó dưới dạng Long:
                     */
                    Long stationIdLong = ds.getValue(Long.class);

                    // Nếu value không có (hoặc null), bạn có thể thử lấy key
                    String stationIdStr;
                    if (stationIdLong != null) {
                        stationIdStr = String.valueOf(stationIdLong);
                    } else {
                        stationIdStr = ds.getKey();
                    }

                    if (stationIdStr != null && !stationIdStr.trim().isEmpty()) {
                        try {
                            int stationId = Integer.parseInt(stationIdStr);
                            fetchStationDetail(stationId);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Dữ liệu adapter sẽ được cập nhật trong fetchStationDetail()
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Truy vấn chi tiết của station từ node "station" dựa trên station id
    private void fetchStationDetail(final int stationId) {
        DatabaseReference stationRef = FirebaseDatabase.getInstance().getReference("station");
        Query query = stationRef.orderByChild("id").equalTo(stationId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Nếu có nhiều kết quả (thường chỉ có 1), thêm chúng vào danh sách
                for (DataSnapshot ds : snapshot.getChildren()) {
                    station stationItem = ds.getValue(station.class);
                    if (stationItem != null) {
                        favoriteStations.add(stationItem);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }
}