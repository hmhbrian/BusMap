package com.example.busmap.Main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Set;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private ArrayList<route> routeList;
    private Set<String> favoriteRoutes;
    private OnFavoriteClickListener favoriteClickListener;
    private OnItemClickListener itemClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(route routeItem, boolean isFavorite);
    }

    public interface OnItemClickListener {
        void onItemClick(route routeItem);
    }

    public RouteAdapter(ArrayList<route> routeList,
                        Set<String> favoriteRoutes,
                        OnFavoriteClickListener favoriteClickListener,
                        OnItemClickListener itemClickListener) {
        this.routeList = routeList;
        this.favoriteRoutes = favoriteRoutes;
        this.favoriteClickListener = favoriteClickListener;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        route routeItem = routeList.get(position);
        holder.tvRouteName.setText(routeItem.getName());

        // Kiểm tra xem tuyến có nằm trong danh sách yêu thích hay không để set icon tương ứng
        checkIfFavorite(holder, routeItem);

        // Lắng nghe sự kiện click vào icon trái tim để thay đổi trạng thái yêu thích
        holder.ivFavorite.setOnClickListener(v -> {
            boolean isCurrentlyFavorite = favoriteRoutes.contains(routeItem.getId());
            boolean newFavoriteState = !isCurrentlyFavorite;
            favoriteClickListener.onFavoriteClick(routeItem, newFavoriteState);
            // Cập nhật giao diện ngay lập tức
            if (newFavoriteState) {
                // Cập nhật Firebase để thêm tuyến yêu thích vào bảng Favorite
                toggleFavoriteRoute(routeItem.getId(), true);
                favoriteRoutes.add(routeItem.getId());
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled); // Trái tim màu đỏ khi yêu thích
            } else {
                // Cập nhật Firebase để bỏ yêu thích tuyến
                toggleFavoriteRoute(routeItem.getId(), false);
                favoriteRoutes.remove(routeItem.getId());
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline); // Trái tim màu xám khi không yêu thích
            }
        });

        // Sự kiện click vào cả item (để chuyển sang BusRouteActivity)
        holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(routeItem));
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    // Kiểm tra xem tuyến xe có trong bảng Favorite của người dùng không
    private void checkIfFavorite(ViewHolder holder, route routeItem) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("Favorite")
                .child(userId)
                .child(routeItem.getId()); // Lấy ID tuyến từ bảng Favorite

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu tuyến xe có trong bảng, trái tim sẽ màu đỏ
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                } else {
                    // Nếu tuyến xe không có trong bảng, trái tim sẽ màu xám
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    // Cập nhật trạng thái yêu thích tuyến trong Firebase
    private void toggleFavoriteRoute(String routeId, boolean isFavorite) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favRef = FirebaseDatabase.getInstance()
                .getReference("Favorite")
                .child(userId); // Lấy ID người dùng

        if (isFavorite) {
            // Lưu tuyến vào bảng Favorite của người dùng
            favRef.child(routeId).setValue(true);
        } else {
            // Xóa tuyến khỏi bảng Favorite của người dùng
            favRef.child(routeId).removeValue();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvRouteName;
        public ImageView ivFavorite;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRouteName = itemView.findViewById(R.id.tv_routeName);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}
