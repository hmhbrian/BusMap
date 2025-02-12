package com.example.busmap.User;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.example.busmap.entities.user;
import com.example.busmap.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {
    private TextView txtName, txtEmail, txtPhone, txtBirthday, txtGender, txtRole;
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
        txtRole = findViewById(R.id.txtRole);
        loadUserData();

//        // Lấy người dùng hiện tại từ Firebase Authentication
//        user = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (user != null) {
//            String userId = user.getUid(); // Lấy ID của người dùng hiện tại
//
//            // Truy vấn dữ liệu từ Firebase Realtime Database
//            userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);
//            loadUserData();
//        } else {
//            // Nếu không có người dùng nào đăng nhập
//            Toast.makeText(AccountActivity.this, "Không có người dùng nào đăng nhập", Toast.LENGTH_SHORT).show();
//            finish();
//        }
    }

    private void loadUserData() {
        user user_info = UserManager.getUserFromSharedPreferences(this);
        if (user != null) {
            Log.d("User Info", "Tên: " + user_info.getName() + ", Email: " + user_info.getEmail());
        }
        // Hiển thị thông tin người dùng lên giao diện
        txtName.setText(user_info.getName());
        txtEmail.setText(user_info.getEmail());
        txtPhone.setText(user_info.getPhone());
        txtBirthday.setText(user_info.getBirthday());
        txtGender.setText(user_info.getGender());
        txtRole.setText(user_info.getRole());
    }
}
