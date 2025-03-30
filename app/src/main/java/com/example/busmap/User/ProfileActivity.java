package com.example.busmap.User;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.example.busmap.zalopay.Api.CreateOrder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout btnUpgrade, rechargePopup;
    private TextView txtPersonalInfo, txtRateApp, txtCompanyInfo, txtTitle, txtRecharge, txtbalance;
    private Button btnConfirmRecharge;
    private FirebaseAuth auth;
    private DatabaseReference database;
    private String userId;

    void init(){
        btnUpgrade = findViewById(R.id.btnUpgrade);
        txtPersonalInfo = findViewById(R.id.txtPersonalInfo);
        txtRateApp = findViewById(R.id.txtRateApp);
        txtCompanyInfo = findViewById(R.id.txtCompanyInfo);
        txtTitle = findViewById(R.id.txtTitle);
        txtbalance = findViewById(R.id.txtBalance);
        txtRecharge = findViewById(R.id.txtRecharge);
        rechargePopup = findViewById(R.id.rechargePopup);
        btnConfirmRecharge = findViewById(R.id.btnConfirmRecharge);
    }

    void initListener(){
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
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("user_balance");
        userId = auth.getCurrentUser().getUid();
        init();

        getUserBalance(userId);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ZaloPaySDK.init(2553, Environment.SANDBOX);

        user user_info = UserManager.getUserFromSharedPreferences(this);
        txtTitle.setText(user_info.getName());

        initListener();

        btnConfirmRecharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editRechargeAmount = findViewById(R.id.editRechargeAmount);
                String amountStr = editRechargeAmount.getText().toString();
                if (!amountStr.isEmpty()) {
                    long amount = Long.parseLong(amountStr);
                    if (amount > 0) {
                        createZaloPayOrder(amount);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                }
                rechargePopup.setVisibility(View.GONE);
            }
        });

    }

    public void createZaloPayOrder(long amount){
        CreateOrder orderApi = new CreateOrder();

        try {
            JSONObject data = orderApi.createOrder(String.valueOf(amount));
            String code = data.getString("return_code");

            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                ZaloPaySDK.getInstance().payOrder(ProfileActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String s, String s1, String s2) {
                        updateUserBalance(userId, amount);
                        Toast.makeText(ProfileActivity.this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Toast.makeText(ProfileActivity.this, "Giao dịch bị hủy", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Toast.makeText(ProfileActivity.this, "Lỗi: " + zaloPayError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(ProfileActivity.this, "Không thể tạo giao dịch", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUserBalance(String userId) {
        final DatabaseReference userBalanceRef = database.child(userId);
        userBalanceRef.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentBalance = dataSnapshot.getValue(Long.class);
                if (currentBalance == null) currentBalance = 0L;
                NumberFormat formatter = new DecimalFormat("#,###");
                txtbalance.setText("Số dư: " + formatter.format(currentBalance) + " vnđ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Lỗi cập nhật số dư", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserBalance(String userId, long amount) {
        final DatabaseReference userBalanceRef = database.child(userId);
        userBalanceRef.child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currentBalance = dataSnapshot.getValue(Long.class);
                if (currentBalance == null) currentBalance = 0L;
                long newBalance = currentBalance + amount;
                userBalanceRef.child("balance").setValue(newBalance);
                userBalanceRef.child("last_update").setValue(System.currentTimeMillis());
                NumberFormat formatter = new DecimalFormat("#,###");
                txtbalance.setText("Số dư: " + formatter.format(newBalance) + " vnđ");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Lỗi cập nhật số dư", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
