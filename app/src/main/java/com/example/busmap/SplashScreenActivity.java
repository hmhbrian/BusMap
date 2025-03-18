package com.example.busmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.Main.MainActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Đặt thời gian chờ để màn hình Splash hiển thị trong 3 giây
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Sau khi hết thời gian, chuyển sang MainActivity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // Đóng màn hình Splash sau khi chuyển
            }
        }, 3000); // 3 giây delay
    }
}
