package com.example.busmap.User;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.busmap.MainActivity;
import com.example.busmap.R;
import com.example.busmap.UserManager;
import com.example.busmap.entities.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Login extends Activity {
    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("User");

        btnLogin.setOnClickListener(view -> {
            // Gọi phương thức signIn khi nhấn nút đăng nhập
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            signIn(email, password); // Gọi phương thức đăng nhập
        });

        tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, Register.class));
        });
    }

    // Phương thức đăng nhập nhận email và mật khẩu
    private void signIn(String email, String password) {
        // Kiểm tra các trường hợp nhập liệu
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng nhập người dùng với Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Đăng nhập thành công, chuyển đến MainActivity
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish(); // Đóng màn hình đăng nhập
                        }
                        Log.d("Information","Id: " + user.getUid() + user.getDisplayName() + user.getPhoneNumber());
                        //fetchInformation(user.getUid());
                        UserManager.fetchAndSaveUser(this);
                    } else {
                        // Đăng nhập thất bại, hiển thị thông báo lỗi
                        Toast.makeText(Login.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchInformation(String userId){
        databaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user user = snapshot.getValue(user.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String birthday = snapshot.child("birthday").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);

                    Log.d("UserInfo", "Name: " + name);
                    Log.d("UserInfo", "Email: " + email);
                    Log.d("UserInfo", "Phone: " + phone);
                    Log.d("UserInfo", "Birthday: " + birthday);
                    Log.d("UserInfo", "Gender: " + gender);
                    Log.d("UserInfo", "Role: " + role);
                } else {
                    Log.d("UserInfo", "Không tìm thấy thông tin người dùng!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu", error.toException());
            }
        });
    }
}
