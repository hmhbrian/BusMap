package com.example.busmap.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route; // Import lớp route
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StationDetailsDialog {
    private Context context;
    private int stationId;
    private List<route> routeDetails;

    public StationDetailsDialog(Context context, int stationId, List<route> routeDetails) {
        this.context = context;
        this.stationId = stationId;
        this.routeDetails = routeDetails;
    }

    public void show() {
        // Inflate layout từ file XML
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_station_details, null);

        // Lấy tham chiếu các View trong layout
        TextView tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        final ImageView ivFavorite = view.findViewById(R.id.iv_favorite);
        RecyclerView rvRouteDetails = view.findViewById(R.id.rv_route_details);

        // Cập nhật tiêu đề
        tvDialogTitle.setText("Các tuyến qua trạm " + stationId);

        // Set up RecyclerView với adapter
        rvRouteDetails.setLayoutManager(new LinearLayoutManager(context));
        RouteDetailsAdapter adapter = new RouteDetailsAdapter(context, routeDetails); // Truyền context và route vào adapter
        rvRouteDetails.setAdapter(adapter);

        // Tạo AlertDialog sử dụng view trên
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("Đóng", null)
                .create();
        dialog.show();

        // Lấy userId từ FirebaseAuth
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Favorite")
                .child(userId);  // Sử dụng bảng "Favorite" để lưu trạm yêu thích

        // Kiểm tra trạng thái yêu thích và cập nhật icon
        favRef.child("station_" + stationId)  // Lưu trạm theo định dạng "station_{stationId}"
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean isFav = snapshot.exists();
                        if (isFav) {
                            ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                        } else {
                            ivFavorite.setImageResource(R.drawable.ic_heart_outline);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });

        // Xử lý sự kiện click vào nút tim để toggle trạng thái yêu thích
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favRef.child("station_" + stationId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                boolean isFav = snapshot.exists();
                                if (isFav) {
                                    // Nếu đã yêu thích, bỏ yêu thích
                                    favRef.child("station_" + stationId).removeValue();
                                    ivFavorite.setImageResource(R.drawable.ic_heart_outline);  // Trái tim màu xám khi bỏ yêu thích
                                } else {
                                    // Nếu chưa yêu thích, thêm vào yêu thích
                                    favRef.child("station_" + stationId).setValue(true);
                                    ivFavorite.setImageResource(R.drawable.ic_heart_filled);  // Trái tim màu đỏ khi yêu thích
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) { }
                        });
            }
        });
    }
}
