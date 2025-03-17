// SearchHistoryAdapter.java
package com.example.busmap.Route.FindRoute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.entities.SearchHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
    private List<SearchHistory> historyList;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(SearchHistory history);
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(SearchHistory history);
    }

    public SearchHistoryAdapter(OnItemClickListener itemListener, OnDeleteClickListener deleteListener) {
        this.historyList = new ArrayList<>();
        this.listener = itemListener;
        this.deleteListener = deleteListener;
    }

    public void setHistoryList(List<SearchHistory> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchHistory history = historyList.get(position);
        holder.tvStationName.setText(history.getStation().getName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String timestamp = sdf.format(history.getTimestamp());
        holder.tvTimestamp.setText(timestamp);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(history));
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(history);
        });

        // Hiển thị nút xóa khi hover hoặc long click (tùy chọn)
        holder.itemView.setOnLongClickListener(v -> {
            holder.btnDelete.setVisibility(View.VISIBLE);
            return true;
        });}

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName, tvTimestamp;
        ImageView iconStation, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvStationName = itemView.findViewById(R.id.tv_station_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            iconStation = itemView.findViewById(R.id.icon_station);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}