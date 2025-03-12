package com.example.busmap.Notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.busmap.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewFragment extends Fragment {
    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<NewsModel> newsList;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        newsList = new ArrayList<>();
        adapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(adapter);

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("news");
        fetchNews();

        return view;
    }

    private void fetchNews() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NewsModel news = dataSnapshot.getValue(NewsModel.class);
                    if (news != null && news.getDate() != null) {
                        newsList.add(news);
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Collections.sort(newsList, new Comparator<NewsModel>() {
                    @Override
                    public int compare(NewsModel n1, NewsModel n2) {
                        try {
                            if (n1.getDate() == null || n1.getDate().isEmpty()) return 1;
                            if (n2.getDate() == null || n2.getDate().isEmpty()) return -1;
                            Date date1 = sdf.parse(n1.getDate());
                            Date date2 = sdf.parse(n2.getDate());
                            return date2.compareTo(date1); // Sắp xếp giảm dần
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
