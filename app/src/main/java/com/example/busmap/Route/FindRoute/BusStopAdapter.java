package com.example.busmap.Route.FindRoute;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.station;

import java.util.ArrayList;
import java.util.List;

public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.ViewHolder> {
    private ArrayList<station> stationList;
    private String routeId;


    public BusStopAdapter(ArrayList<station> stationList, String routeId) {
        this.stationList = stationList;
        this.routeId = routeId;
    }


    @NonNull
    @Override
    public BusStopAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_busstop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        station currentStation = stationList.get(holder.getAdapterPosition());
        Log.d("ADAPTER_BusStop", "Binding: " + currentStation.getName());
        holder.stationName.setText(currentStation.getName());
        holder.stationNumber.setText(routeId.toString());

    }

    @Override
    public int getItemCount() {
        Log.d("ADAPTER_BusStop", "Item count: " + stationList.size());
        return stationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationName, stationNumber;
        CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.txtStationName);
            stationNumber = itemView.findViewById(R.id.txtStationNumber);
            cardView = (CardView) itemView;
        }
    }
}
