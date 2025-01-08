package com.example.busmap.Route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.station;

import java.util.ArrayList;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder>{

    private ArrayList<station> Stationlist;


    public StationAdapter(ArrayList<station> Stationlist) {
        this.Stationlist = Stationlist;
    }

    @NonNull
    @Override
    public StationAdapter.StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
        return new StationAdapter.StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationAdapter.StationViewHolder holder, int position) {
        station currentRoute = Stationlist.get(position);
        holder.tvStationName.setText(currentRoute.getName());
    }

    @Override
    public int getItemCount() {
        return Stationlist.size();
    }

    public static class StationViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStationName = itemView.findViewById(R.id.tv_stationName);
        }
    }
}
