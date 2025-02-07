package com.example.busmap.Route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.station;

import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {
    private List<station> stationList;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Không giữ vị trí cố định

    public interface OnItemClickListener {
        void onItemClick(station station);
    }

    public StationAdapter(List<station> stationList) {
        this.stationList = stationList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
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
        station currentStation = stationList.get(holder.getAdapterPosition()); // Lấy vị trí động
        holder.stationName.setText(currentStation.getName());
        holder.stationNumber.setText(String.valueOf(holder.getAdapterPosition() + 1)); // Số thứ tự trạm

        // Đổi màu nếu được chọn
        if (selectedPosition == holder.getAdapterPosition()) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.teal_700));
            holder.stationName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.stationName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentStation);
            }

            // Cập nhật vị trí được chọn
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Cập nhật lại chỉ hai item thay đổi, tránh lỗi RecyclerView
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
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
