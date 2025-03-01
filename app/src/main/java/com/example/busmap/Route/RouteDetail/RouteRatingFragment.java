package com.example.busmap.Route.RouteDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.busmap.R;
import com.example.busmap.entities.Rating;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// ...

public class RouteRatingFragment extends Fragment {

    private int countStar5 = 0;
    private int countStar4 = 0;
    private int countStar3 = 0;
    private int countStar2 = 0;
    private int countStar1 = 0;

    private DatabaseReference databaseRef;
    private String routeId;
    private String userId;
    private String userName;

    private EditText edtRating;
    private RatingBar starRating;
    private Button btnSubmit;

    private ListView listView;
    private RatingAdapter ratingAdapter;
    private List<Rating> ratingList = new ArrayList<>();

    public static RouteRatingFragment newInstance(String routeId) {
        RouteRatingFragment fragment = new RouteRatingFragment();
        Bundle args = new Bundle();
        args.putString("route_id", routeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeId = getArguments().getString("route_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_rating, container, false);

        // Khởi tạo các thành phần giao diện
        edtRating = view.findViewById(R.id.edt_rating);
        starRating = view.findViewById(R.id.star_rating);
        btnSubmit = view.findViewById(R.id.rating_button);
        listView = view.findViewById(R.id.listViewReviews);

        // Khởi tạo DatabaseReference
        databaseRef = FirebaseDatabase.getInstance().getReference("Ratings");

        // Lấy userId từ FirebaseAuth
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy tên người dùng từ bảng User
        fetchUserName();

        // Khởi tạo adapter và kết nối với ListView
        ratingAdapter = new RatingAdapter(getContext(), ratingList);
        listView.setAdapter(ratingAdapter);

        btnSubmit.setOnClickListener(v -> submitRating());

        // Tải đánh giá
        loadRatings();

        return view;
    }

    private void fetchUserName() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener () {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userName = dataSnapshot.child("name").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi lấy tên người dùng: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitRating() {
        String note = edtRating.getText().toString().trim();
        float star = starRating.getRating();
        String time = String.valueOf(System.currentTimeMillis());

        if (note.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập ghi chú!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userName == null || userName.isEmpty()) {
            Toast.makeText(getContext(), "Tên người dùng không xác định!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng đánh giá
        Rating rating = new Rating(userName, (int) star, time, note);

        // Lưu vào Firebase dưới userId
        databaseRef.child(routeId).child(userId).push().setValue(rating).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                // Xóa dữ liệu sau khi lưu thành công
                edtRating.setText("");
                starRating.setRating(0);
            } else {
                Toast.makeText(getContext(), "Lỗi lưu dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadRatings() {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings").child(routeId);
        ratingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ratingList.clear();
                countStar5 = 0;
                countStar4 = 0;
                countStar3 = 0;
                countStar2 = 0;
                countStar1 = 0;

                int totalStars = 0;
                int ratingCount = 0;

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot : userSnapshot.getChildren()) {
                        Rating rating = snapshot.getValue(Rating.class);
                        if (rating != null) {
                            ratingList.add(rating);
                            totalStars += rating.getStarRating();
                            ratingCount++;

                            switch (rating.getStarRating()) {
                                case 5:
                                    countStar5++;
                                    break;
                                case 4:
                                    countStar4++;
                                    break;
                                case 3:
                                    countStar3++;
                                    break;
                                case 2:
                                    countStar2++;
                                    break;
                                case 1:
                                    countStar1++;
                                    break;
                            }
                        }
                    }
                }
                ratingAdapter.notifyDataSetChanged();
                updateStarCounts();

                if (ratingCount > 0) {
                    float averageRating = (float) totalStars / ratingCount;
                    updateAverageRating(averageRating);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu đánh giá!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateAverageRating(float averageRating) {
        RatingBar totalStar = getView().findViewById(R.id.total_star);
        TextView edtTotalStar = getView().findViewById(R.id.edt_total_star);

        totalStar.setRating(averageRating);
        edtTotalStar.setText(String.format("%.1f", averageRating));
    }

    private void updateStarCounts() {
        TextView star5 = getView().findViewById(R.id.star5);
        TextView star4 = getView().findViewById(R.id.star4);
        TextView star3 = getView().findViewById(R.id.star3);
        TextView star2 = getView().findViewById(R.id.star2);
        TextView star1 = getView().findViewById(R.id.star1);

        star5.setText(String.valueOf(countStar5));
        star4.setText(String.valueOf(countStar4));
        star3.setText(String.valueOf(countStar3));
        star2.setText(String.valueOf(countStar2));
        star1.setText(String.valueOf(countStar1));

        ProgressBar progress5 = getView().findViewById(R.id.progress5);
        ProgressBar progress4 = getView().findViewById(R.id.progress4);
        ProgressBar progress3 = getView().findViewById(R.id.progress3);
        ProgressBar progress2 = getView().findViewById(R.id.progress2);
        ProgressBar progress1 = getView().findViewById(R.id.progress1);

        int totalRatings = countStar5 + countStar4 + countStar3 + countStar2 + countStar1;

        if (totalRatings > 0) {
            setProgressBar(progress5, (float) countStar5 / totalRatings);
            setProgressBar(progress4, (float) countStar4 / totalRatings);
            setProgressBar(progress3, (float) countStar3 / totalRatings);
            setProgressBar(progress2, (float) countStar2 / totalRatings);
            setProgressBar(progress1, (float) countStar1 / totalRatings);
        }

        star5.setText(String.valueOf(countStar5));
        star4.setText(String.valueOf(countStar4));
        star3.setText(String.valueOf(countStar3));
        star2.setText(String.valueOf(countStar2));
        star1.setText(String.valueOf(countStar1));
    }

    private void setProgressBar(ProgressBar progressBar, float percentage) {
        progressBar.setProgress((int) (percentage * 100));
    }

}
