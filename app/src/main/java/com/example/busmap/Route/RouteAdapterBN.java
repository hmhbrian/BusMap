package com.example.busmap.Route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;

import java.util.List;

public class RouteAdapterBN extends RecyclerView.Adapter<RouteAdapterBN.RouteViewHolder> {
    private List<route> routes;  // Sử dụng đúng lớp 'route'

    public RouteAdapterBN(List<route> routes) {
        this.routes = routes;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        route currentRoute = routes.get(position);  // Lấy route hiện tại
        holder.routeName.setText(currentRoute.getName());  // Hiển thị tên tuyến
        holder.routeTime.setText("Giờ hoạt động: " + currentRoute.getOperation());  // Nếu cần hiển thị giờ hoạt động
        holder.routePrice.setText("Giá: " + currentRoute.getPrice());  // Hiển thị giá tuyến xe
    }

    @Override
    public int getItemCount() {
        return routes.size();  // Trả về số lượng tuyến xe
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeName, routeTime, routePrice;

        public RouteViewHolder(View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.route_name);
            routeTime = itemView.findViewById(R.id.route_time);
            routePrice = itemView.findViewById(R.id.route_price);
        }
    }
}
