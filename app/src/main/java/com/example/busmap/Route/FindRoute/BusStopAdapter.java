package com.example.busmap.Route.FindRoute;

import static com.example.busmap.FindRouteHelper.Tranfers.StringNumberExtractor;

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
import java.util.Map;

public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.ViewHolder> {
    private Map<String, List<station>> stationOfRoute;
    private List<StationItem> stationItems = new ArrayList<>();
    private List<String> routeList = new ArrayList<>();

    public BusStopAdapter(Map<String, List<station>> stationOfRoute) {
        this.stationOfRoute = stationOfRoute;
        this.routeList.addAll(stationOfRoute.keySet());
        populateStationItems();
    }

    private void populateStationItems() {
        stationItems.clear();
        for (String routeId : routeList) {
            List<station> stations = stationOfRoute.get(routeId);
            if (stations != null) {
                for (station s : stations) {
                    stationItems.add(new StationItem(routeId, s));
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_busstop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StationItem stationItem = stationItems.get(position);
        holder.stationName.setText(stationItem.station.getName());
        holder.stationNumber.setText(StringNumberExtractor(stationItem.routeId));

        // Định nghĩa màu sắc cho từng tuyến đường
        int[] routeColors = {R.color.green,R.color.red, R.color.orange, R.color.primary_400};
        int colorIndex = routeList.indexOf(stationItem.routeId) % routeColors.length;
        int routeColor = holder.itemView.getContext().getResources().getColor(routeColors[colorIndex]);
        holder.stationNumber.setTextColor(routeColor);
        holder.txtSymbol.setBackgroundColor(routeColor);
    }

    @Override
    public int getItemCount() {
        return stationItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationName, stationNumber, txtSymbol;

        public ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.txtStationName);
            stationNumber = itemView.findViewById(R.id.txtStationNumber);
            txtSymbol = itemView.findViewById(R.id.txtSymbol);
        }
    }

    private static class StationItem {
        String routeId;
        station station;

        public StationItem(String routeId, station station) {
            this.routeId = routeId;
            this.station = station;
        }
    }
}
