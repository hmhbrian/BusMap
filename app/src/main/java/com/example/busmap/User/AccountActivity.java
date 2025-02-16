package com.example.busmap.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.example.busmap.entities.user;
import com.example.busmap.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtBirthday, txtGender;
    private Button btnEdit;
    private DatabaseReference userRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Ánh xạ các TextView
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtBirthday = findViewById(R.id.txtBirthday);
        txtGender = findViewById(R.id.txtGender);
        btnEdit = findViewById(R.id.btnEdit);

        // Load dữ liệu người dùng
        loadUserData();

        // Set sự kiện khi người dùng ấn vào nút "Sửa thông tin"
        btnEdit.setOnClickListener(v -> openEditAccountActivity());
    }

    private void loadUserData() {
        // Lấy thông tin người dùng từ SharedPreferences hoặc Firebase
        user user_info = UserManager.getUserFromSharedPreferences(this);

        // Kiểm tra nếu có thông tin người dùng
        if (user_info != null) {
            // Hiển thị thông tin người dùng lên giao diện
            txtName.setText(user_info.getName());
            txtEmail.setText(user_info.getEmail());
            txtPhone.setText(user_info.getPhone());
            txtBirthday.setText(user_info.getBirthday());
            txtGender.setText(user_info.getGender());

        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    private void openEditAccountActivity() {
        // Chuyển đến EditAccountActivity để chỉnh sửa thông tin người dùng
        Intent intent = new Intent(AccountActivity.this, EditAccountActivity.class);

        // Nếu bạn muốn chuyển thông tin người dùng sang EditAccountActivity, bạn có thể dùng Intent extras.
        // Ví dụ:
        intent.putExtra("name", txtName.getText().toString());
        intent.putExtra("email", txtEmail.getText().toString());
        intent.putExtra("phone", txtPhone.getText().toString());
        intent.putExtra("birthday", txtBirthday.getText().toString());
        intent.putExtra("gender", txtGender.getText().toString());

        // Mở EditAccountActivity
        startActivity(intent);
    }
}
