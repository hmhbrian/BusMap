package com.example.busmap.Route.FindRoute;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.FindRouteHelper.RouteResult;
import com.example.busmap.FindRouteHelper.Tranfers;
import com.example.busmap.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteFindAdapter extends RecyclerView.Adapter<RouteFindAdapter.RouteViewHolder> {
    private List<RouteResult> routes;
    private Context context;

    public RouteFindAdapter(Context context, List<RouteResult> routes) {
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
        RouteResult routeResult  = routes.get(position);
        List<String> routePath = routeResult.getRouteNames();

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
        holder.edtCost.setText(Tranfers.formatCurrency(routeResult.getTotalCost()));
        holder.tv_time.setText(Tranfers.formatTime(routeResult.getTotalTime()));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FindRouteDetailActivity.class);
            HashMap<String, ArrayList<Integer>> stationHashMap = new HashMap<>();

            for (Map.Entry<String, List<Integer>> entry : routeResult.getStationsMap().entrySet()) {
                stationHashMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            // Đưa HashMap vào Intent
            intent.putExtra("stationsMap", stationHashMap);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        Button route1, route2;
        ImageView arrow;
        EditText edtCost;
        TextView tv_time;

        public RouteViewHolder(View itemView) {
            super(itemView);
            route1 = itemView.findViewById(R.id.btnRoute1);
            route2 = itemView.findViewById(R.id.btnRoute2);
            arrow = itemView.findViewById(R.id.imgArrow);
            edtCost = itemView.findViewById(R.id.edt_Totalprice);
            tv_time = itemView.findViewById(R.id.tv_Totaltime);
        }
    }
}
