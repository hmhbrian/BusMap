package com.example.busmap.dialog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.Route.RouteDetail.BusRouteActivity; // Import BusRouteActivity
import com.example.busmap.entities.route; // Import lớp route

import java.util.List;

public class RouteDetailsAdapter extends RecyclerView.Adapter<RouteDetailsAdapter.RouteViewHolder> {
    private List<route> routeDetails; // Sử dụng List<route> thay vì List<String>
    private Context context;

    public RouteDetailsAdapter(Context context, List<route> routeDetails) {
        this.context = context;
        this.routeDetails = routeDetails;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate layout item cho RecyclerView
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_route_detail, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RouteViewHolder holder, int position) {
        // Gán dữ liệu cho từng item
        holder.routeDetailText.setText(routeDetails.get(position).getName()); // Hiển thị tên tuyến
        holder.routeOperation.setText(routeDetails.get(position).getOperation()); // Hiển thị thông tin hoạt động
    }

    @Override
    public int getItemCount() {
        return routeDetails.size();
    }

    // ViewHolder cho từng item trong RecyclerView
    public class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView routeDetailText, routeOperation;

        public RouteViewHolder(View itemView) {
            super(itemView);
            routeDetailText = itemView.findViewById(R.id.tvRouteName);
            routeOperation = itemView.findViewById(R.id.tvRouteOperation);

            // Sự kiện click vào item (tuyến bus)
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Lấy đối tượng route từ danh sách và mở BusRouteActivity
                    openBusRouteActivity(routeDetails.get(position));
                }
            });
        }

        // Mở BusRouteActivity khi nhấn vào tuyến
        private void openBusRouteActivity(route routeItem) {
            Intent intent = new Intent(context, BusRouteActivity.class);
            intent.putExtra("route_id", routeItem.getId()); // Gửi route_id
            intent.putExtra("route_name", routeItem.getName()); // Gửi route_name
            context.startActivity(intent);
        }
    }
}
