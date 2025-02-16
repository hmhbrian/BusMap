package com.example.busmap.User;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.example.busmap.entities.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class EditAccountActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtBirthday;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Ánh xạ View
        edtName = findViewById(R.id.etName);
        edtEmail = findViewById(R.id.etEmail);
        edtPhone = findViewById(R.id.etPhone);
        edtBirthday = findViewById(R.id.etBirthday);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnSave = findViewById(R.id.btnSave);

        // Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("User").child(userId);
            loadUserData();
        }

        // DatePicker cho ngày sinh
        edtBirthday.setOnClickListener(v -> showDatePickerDialog());

        // Xử lý sự kiện nút lưu
        btnSave.setOnClickListener(v -> updateUserData());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            edtBirthday.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void loadUserData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user currentUser = snapshot.getValue(user.class);
                    if (currentUser != null) {
                        edtName.setText(currentUser.getName());
                        edtEmail.setText(currentUser.getEmail());
                        edtPhone.setText(currentUser.getPhone());
                        edtBirthday.setText(currentUser.getBirthday());

                        if ("Male".equals(currentUser.getGender())) {
                            rbMale.setChecked(true);
                        } else if ("Female".equals(currentUser.getGender())) {
                            rbFemale.setChecked(true);
                        }
                    }
                }
            }
//test
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditAccountActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        String updatedName = edtName.getText().toString().trim();
        String updatedEmail = edtEmail.getText().toString().trim();
        String updatedPhone = edtPhone.getText().toString().trim();
        String updatedBirthday = edtBirthday.getText().toString().trim();
        int selectedId = rgGender.getCheckedRadioButtonId();
        String updatedGender = (selectedId == R.id.rbMale) ? "Male" : "Female";

        if (TextUtils.isEmpty(updatedName) || TextUtils.isEmpty(updatedEmail) || TextUtils.isEmpty(updatedPhone) || TextUtils.isEmpty(updatedBirthday)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        user updatedUser = new user(updatedName, updatedEmail, updatedPhone, updatedBirthday, updatedGender, "User");

        databaseRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(EditAccountActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditAccountActivity.this, "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show());
    }
}
