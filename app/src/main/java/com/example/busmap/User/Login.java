package com.example.busmap.User;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busmap.Main.MainActivity;
import com.example.busmap.R;
import com.example.busmap.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                            //Lưu trạng thái đăng nhập
                            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();
                            // Đăng nhập thành công, chuyển đến MainActivity
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish(); // Đóng màn hình đăng nhập
                        }
                        UserManager.fetchAndSaveUser(this);
                    } else {
                        // Đăng nhập thất bại, hiển thị thông báo lỗi
                        Toast.makeText(Login.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
