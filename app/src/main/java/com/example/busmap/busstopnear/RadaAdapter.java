package com.example.busmap.busstopnear;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;

import java.util.ArrayList;
import java.util.List;

public class RadaAdapter extends RecyclerView.Adapter<RadaAdapter.StationViewHolder> {

    private List<BusStation> stations = new ArrayList<>();
    private OnStationClickListener listener;
    private OnStationDetailsClickListener detailsListener;

    public interface OnStationClickListener {
        void onStationClick(BusStation station);
    }

    public interface OnStationDetailsClickListener {
        void onStationDetailsClick(int stationId); // Thêm phương thức này để gọi khi nhấn vào nút chi tiết
    }

    public RadaAdapter(OnStationClickListener listener, OnStationDetailsClickListener detailsListener) {
        this.listener = listener;
        this.detailsListener = detailsListener;
    }

    public void setStations(List<BusStation> stations) {
        this.stations = stations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus_station, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        BusStation station = stations.get(position);
        holder.bind(station);
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    class StationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStationName;
        private TextView tvStationId;
        private TextView tvDistance;
        private ImageButton btnStationDetails;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStationName = itemView.findViewById(R.id.tvStationName);
            tvStationId = itemView.findViewById(R.id.tvStationId);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnStationDetails = itemView.findViewById(R.id.btnStationDetails);

            // Sự kiện click vào nút chi tiết
            btnStationDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && detailsListener != null) {
                    detailsListener.onStationDetailsClick(stations.get(position).getId());
                }
            });

            // Sự kiện click vào item để xem thông tin trạm
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStationClick(stations.get(position));
                }
            });
        }

        public void bind(BusStation station) {
            tvStationName.setText(station.getName());
            tvStationId.setText("ID: " + station.getId());
            tvDistance.setText(station.getFormattedDistance());
        }
    }
}
