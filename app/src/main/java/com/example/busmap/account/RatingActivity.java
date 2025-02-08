package com.example.busmap.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.example.busmap.User.ProfileActivity;

public class RatingActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextView feedbackTextView;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rating_app);

        ratingBar = findViewById(R.id.rating_bar);
        feedbackTextView = findViewById(R.id.tv_rating_feedback);
        submitButton = findViewById(R.id.btn_submit_rating);

        // Thiết lập sự kiện khi người dùng thay đổi giá trị RatingBar
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                feedbackTextView.setText("Bạn đã đánh giá: " + rating + " sao");
            }
        });

        // Xử lý khi người dùng nhấn nút "Gửi"
        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            Toast.makeText(RatingActivity.this, "Cảm ơn bạn đã đánh giá " + rating + " sao!", Toast.LENGTH_SHORT).show();
            // Xử lý logic gửi đánh giá lên server hoặc lưu trữ
        });
        // Nút "Đóng"
        Button closeButton = findViewById(R.id.btn_close);

        // Xử lý sự kiện khi nhấn vào nút "Đóng"
        closeButton.setOnClickListener(v -> {
            // Tạo Intent để chuyển sang MainActivity (hoặc activity khác)
            Intent intent = new Intent(RatingActivity.this, ProfileActivity.class);
            // Xóa activity hiện tại khỏi stack và quay lại activity mới
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            // Đóng activity hiện tại
            finish();
        });
    }
}
