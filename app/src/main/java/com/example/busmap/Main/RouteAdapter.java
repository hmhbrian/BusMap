package com.example.busmap.Main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;

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
        holder.tvRouteTime.setText(routeItem.getOperation());
        holder.tvRoutePrice.setText(String.valueOf(routeItem.getPrice()));

        // Kiểm tra xem tuyến có nằm trong danh sách yêu thích hay không để set icon tương ứng
        if (favoriteRoutes.contains(routeItem.getId())) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline);
        }

        // Lắng nghe sự kiện click vào icon trái tim
        holder.ivFavorite.setOnClickListener(v -> {
            boolean isCurrentlyFavorite = favoriteRoutes.contains(routeItem.getId());
            boolean newFavoriteState = !isCurrentlyFavorite;
            favoriteClickListener.onFavoriteClick(routeItem, newFavoriteState);
            // Cập nhật giao diện ngay lập tức
            if (newFavoriteState) {
                favoriteRoutes.add(routeItem.getId());
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
            } else {
                favoriteRoutes.remove(routeItem.getId());
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline);
            }
        });

        // Sự kiện click vào cả item (để chuyển sang BusRouteActivity)
        holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(routeItem));
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvRouteName, tvRouteTime, tvRoutePrice;
        public ImageView ivFavorite;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRouteName = itemView.findViewById(R.id.route_name);
            tvRouteTime = itemView.findViewById(R.id.route_time);
            tvRoutePrice = itemView.findViewById(R.id.route_price);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
        }
    }
}