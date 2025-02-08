package com.example.busmap.User;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
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

        // Lấy người dùng hiện tại từ Firebase Authentication
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid(); // Lấy ID của người dùng hiện tại

            // Truy vấn dữ liệu từ Firebase Realtime Database
            userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);
            loadUserData();
        } else {
            // Nếu không có người dùng nào đăng nhập
            Toast.makeText(AccountActivity.this, "Không có người dùng nào đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu từ Firebase và hiển thị lên TextView
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String birthday = snapshot.child("birthday").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);

                    // Hiển thị thông tin người dùng lên giao diện
                    txtName.setText(name != null ? name : "Không có tên");
                    txtEmail.setText(email != null ? email : "Không có email");
                    txtPhone.setText(phone != null ? phone : "Không có số điện thoại");
                    txtBirthday.setText(birthday != null ? birthday : "Không có ngày sinh");
                    txtGender.setText(gender != null ? gender : "Không xác định");
                    txtRole.setText(role != null ? role : "Không có vai trò");

                } else {
                    Toast.makeText(AccountActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AccountActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
