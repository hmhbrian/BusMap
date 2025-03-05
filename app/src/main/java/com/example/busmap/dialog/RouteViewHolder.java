package com.example.busmap.dialog;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;

public class RouteViewHolder extends RecyclerView.ViewHolder {
    TextView tvRouteName, tvRouteOperation;

    public RouteViewHolder(View itemView) {
        super(itemView);
        tvRouteName = itemView.findViewById(R.id.tvRouteName);
        tvRouteOperation = itemView.findViewById(R.id.tvRouteOperation);
    }
}
