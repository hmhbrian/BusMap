package com.example.busmap.User;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.example.busmap.UserManager;
import com.example.busmap.account.RatingActivity;
import com.example.busmap.account.infocompany;
import com.example.busmap.entities.user;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout btnUpgrade, rechargePopup;
    private TextView txtPersonalInfo, txtRateApp, txtCompanyInfo, txtTitle, txtRecharge;
    private Button btnConfirmRecharge;

    void init(){
        btnUpgrade = findViewById(R.id.btnUpgrade);
        txtPersonalInfo = findViewById(R.id.txtPersonalInfo);
        txtRateApp = findViewById(R.id.txtRateApp);
        txtCompanyInfo = findViewById(R.id.txtCompanyInfo);
        txtTitle = findViewById(R.id.txtTitle);
        txtRecharge = findViewById(R.id.txtRecharge);
        rechargePopup = findViewById(R.id.rechargePopup);
        btnConfirmRecharge = findViewById(R.id.btnConfirmRecharge);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        user user_info = UserManager.getUserFromSharedPreferences(this);
        txtTitle.setText(user_info.getName());

        btnUpgrade.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng nâng cấp chưa khả dụng", Toast.LENGTH_SHORT).show();
        });

        txtPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AccountActivity.class);
            startActivity(intent);
        });


        txtRateApp.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RatingActivity.class);
            startActivity(intent);
        });

        txtCompanyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, infocompany.class);
            startActivity(intent);
        });

        txtRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rechargePopup.getVisibility() == View.GONE) {
                    rechargePopup.setVisibility(View.VISIBLE);
                } else {
                    rechargePopup.setVisibility(View.GONE);
                }
            }
        });

        btnConfirmRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editRechargeAmount = findViewById(R.id.editRechargeAmount);
                String amount = editRechargeAmount.getText().toString();
                // Xử lý logic nạp tiền ở đây
                rechargePopup.setVisibility(View.GONE);
            }
        });
    }
}
