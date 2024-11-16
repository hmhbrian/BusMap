package com.example.busmap.Route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.route;

import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {
    private ArrayList<route> routeList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(route route);
    }
    public RouteAdapter(ArrayList<route> routeList, OnItemClickListener onItemClickListener) {
        this.routeList = routeList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        route currentRoute = routeList.get(position);
        holder.tvRouteName.setText(currentRoute.getName());
        //holder.tvRouteDetails.setText("Price: " + route.getPrice() + " VND\nOperation: " + route.getOperation());
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(currentRoute));
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteName, tvRouteDetails;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteName = itemView.findViewById(R.id.tv_routeName);
            //tvRouteDetails = itemView.findViewById(R.id.tv_routeDetails);
        }
    }
}
