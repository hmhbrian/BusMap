package com.example.busmap.Route.RouteDetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.busmap.R;
import com.example.busmap.entities.Rating;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RatingAdapter extends ArrayAdapter<Rating> {

    public RatingAdapter(Context context, List<Rating> ratings) {
        super(context, 0, ratings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Rating rating = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_rating, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.textViewName);
        RatingBar itemRatingBar = convertView.findViewById(R.id.itemRatingBar);
        TextView textViewNote = convertView.findViewById(R.id.textViewNote);
        TextView textViewDate = convertView.findViewById(R.id.textViewDate);

        if (rating != null) {
            textViewName.setText(rating.getName());
            itemRatingBar.setRating(rating.getStarRating());
            textViewNote.setText(rating.getNote());
            textViewDate.setText(formatDate(rating.getTime()));
        }

        return convertView;
    }

    private String formatDate(String time) {
        try {
            long timestamp = Long.parseLong(time);
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "Invalid date";
        }
    }
}
