package com.example.busmap.Route;

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

import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private List<station> stationList;
    private OnStationClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Vị trí item được chọn

    // Interface để bắt sự kiện click
    public interface OnStationClickListener {
        void onItemClick(station station);
        void onDetailsClick(int stationId);
    }

    public StationAdapter(List<station> stationList) {
        this.stationList = stationList;
    }

    public void setOnStationClickListener(OnStationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_station, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        station currentStation = stationList.get(holder.getAdapterPosition());
        holder.stationName.setText(currentStation.getName());
        holder.stationNumber.setText(String.valueOf(holder.getAdapterPosition() + 1)); // Số thứ tự trạm

        // Ẩn nút "Chi tiết" mặc định
        holder.btnDetails.setVisibility(selectedPosition == position ? View.VISIBLE : View.GONE);

        // Bắt sự kiện khi nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentStation);
            }

            // Nếu nhấn vào cùng item đang chọn, ẩn nút "Chi tiết"
            if (selectedPosition == position) {
                selectedPosition = RecyclerView.NO_POSITION;
            } else {
                int previousPosition = selectedPosition;
                selectedPosition = position;

                // Cập nhật lại chỉ hai item thay đổi
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
            }
        });

        // Bắt sự kiện khi nhấn vào nút "Chi tiết"
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(currentStation.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stationName, stationNumber;
        CardView cardView;
        Button btnDetails; // Thêm tham chiếu nút "Chi tiết"

        public ViewHolder(View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.txtStationName);
            stationNumber = itemView.findViewById(R.id.txtStationNumber);
            cardView = (CardView) itemView;
            btnDetails = itemView.findViewById(R.id.btnDetails); // Ánh xạ nút "Chi tiết"
        }
    }
}
