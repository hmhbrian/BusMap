package com.example.busmap.Route.FindRoute;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.busmap.R;

import java.util.List;

public class RouteFindAdapter extends RecyclerView.Adapter<RouteFindAdapter.RouteViewHolder> {
    private List<List<String>> routes;
    private Context context;

    public RouteFindAdapter(Context context, List<List<String>> routes) {
        this.context = context;
        this.routes = routes;
    }
    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_find, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        List<String> routePath = routes.get(position);

        if (routePath.size() == 1) {
            holder.route1.setText(routePath.get(0));  // Hiển thị route_name
            holder.arrow.setVisibility(View.GONE);
            holder.route2.setVisibility(View.GONE);
        } else {
            holder.route1.setText(routePath.get(0));
            holder.arrow.setVisibility(View.VISIBLE);
            holder.route2.setVisibility(View.VISIBLE);
            holder.route2.setText(routePath.get(1));
        }
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        Button route1, route2;
        ImageView arrow;

        public RouteViewHolder(View itemView) {
            super(itemView);
            route1 = itemView.findViewById(R.id.btnRoute1);
            route2 = itemView.findViewById(R.id.btnRoute2);
            arrow = itemView.findViewById(R.id.imgArrow);
        }
    }
}
