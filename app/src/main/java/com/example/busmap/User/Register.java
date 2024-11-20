package com.example.busmap.User;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.example.busmap.entities.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    private EditText edtName, edtEmail, edtPhone, edtBirthday, edtPass, edtConfirmPass;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnRegister;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    void init(){
        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPhone = findViewById(R.id.edt_phone);
        edtBirthday = findViewById(R.id.edt_birthday);
        edtPass = findViewById(R.id.edt_pass);
        edtConfirmPass = findViewById(R.id.edt_confirmpass);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        btnRegister = findViewById(R.id.btn_register);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangki);
        init();
        // Khởi tạo FirebaseAuth và DatabaseReference
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("User");
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String birthday = edtBirthday.getText().toString().trim();
        String password = edtPass.getText().toString().trim();
        String confirmPassword = edtConfirmPass.getText().toString().trim();
        int selectedId = rgGender.getCheckedRadioButtonId();
        String gender;

        if (selectedId == R.id.rb_male) {
            gender = "Male";
        } else if (selectedId == R.id.rb_female) {
            gender = "Female";
        } else {
            gender = "Not Selected";
        }

        // Kiểm tra dữ liệu đầu vào
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(birthday) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký người dùng với Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Lấy User ID
                String userId = auth.getCurrentUser().getUid();
                String role = "user";

                // Tạo đối tượng người dùng để lưu vào Realtime Database
                user user = new user(name, email, phone, birthday, gender, role);
                // Lưu thông tin người dùng vào Realtime Database

                databaseRef.child(userId).setValue(user).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(Register.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this,Login.class));
                        finish();
                    } else {
                        Toast.makeText(Register.this, "Lỗi lưu dữ liệu: " + dbTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(Register.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
