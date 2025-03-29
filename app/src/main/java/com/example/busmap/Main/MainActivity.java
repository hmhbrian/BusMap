package com.example.busmap.Main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.busmap.Favorites.FavoriteFragment;
import com.example.busmap.Notification.NotificationFragment;
import com.example.busmap.SettingFragment;
import com.example.busmap.R;
import com.example.busmap.ShareFragment;
import com.example.busmap.User.Login;
import com.example.busmap.User.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int QR_SCAN_REQUEST_CODE = 1;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Kiểm tra trạng thái đăng nhập
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Log.d("FirebaseAuth", "Người dùng vẫn đăng nhập: " + user.getEmail());
            }

        }else {
            Log.d("FirebaseAuth", "đăng nhập ");
            startActivity(new Intent(MainActivity.this, Login.class));
        }

        // Gán view cho DrawerLayout, Toolbar và BottomNavigationView
        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load fragment đầu tiên nếu chưa có
        if (savedInstanceState == null) {
            //Hiện Map đầu tiên
            replaceFragment(new MapsFragment());
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        //Thay thế Fragment theo lựa chọn của NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                replaceFragment(new SettingFragment());
            }else if(item.getItemId() == R.id.nav_share){
                replaceFragment(new ShareFragment());
            }else if(item.getItemId() == R.id.nav_about){
                replaceFragment(new AboutFragment());
            }else if(item.getItemId() == R.id.nav_logout){
                Logout();
                startActivity(new Intent(MainActivity.this, Login.class)); // Chuyển về màn hình đăng nhập
                finish();
            }else if(item.getItemId() == R.id.nav_home){
                replaceFragment(new MapsFragment());
            }
            return true;
        });

        // Thay thế Fragment theo lựa chọn của BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.home)
            {
                replaceFragment(new MapsFragment());
            } else if (item.getItemId() == R.id.notify) {
                replaceFragment(new NotificationFragment ());
            }else if (item.getItemId() == R.id.favorite) {
                replaceFragment(new FavoriteFragment());
            }
            else if (item.getItemId() == R.id.info) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return true;
        });
        checkUserAndInitializeBalance();
//        DatabaseReference databaseB = FirebaseDatabase.getInstance().getReference("busstop");
//
//        databaseB.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    Map<String, Object> newStationData = new HashMap<>();
//                    for (DataSnapshot child : snapshot.getChildren()) {
//                        String id = child.child("route_id").getValue(String.class);
//                        Integer order = child.child("order").getValue(Integer.class);
//                        Integer idR = Integer.parseInt(id);
//                        if (idR != null && order != null) {
//                            String orderB =  order < 10 ? "0" + order : String.valueOf(order);
//                            String idB = idR < 10 ? "0" + id : id;
//                            String key = idB + "_" + orderB;
//                            newStationData.put(key, child.getValue());
//                        }
//                    }
//
//                    // Ghi lại dữ liệu dưới dạng đối tượng JSON
//                    databaseB.setValue(newStationData)
//                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Dữ liệu đã chuyển đổi thành công!"))
//                            .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi ghi dữ liệu!", e));
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Firebase", "Lỗi truy vấn!", error.toException());
//            }
//        });
        

    }

    private void checkUserAndInitializeBalance() {
        if (user != null) {
            userId = user.getUid(); // Lấy user_id từ Firebase Authentication
            DatabaseReference userBalanceRef = databaseReference.child("user_balance").child(userId);

            // Kiểm tra xem user_balance đã tồn tại chưa
            userBalanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Nếu chưa tồn tại, thêm bản ghi mới
                        Map<String, Object> balanceData = new HashMap<>();
                        balanceData.put("balance", 50000); // Số dư ban đầu
                        balanceData.put("last_updated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                        userBalanceRef.setValue(balanceData, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error != null) {
                                    Toast.makeText(MainActivity.this, "Lỗi khởi tạo số dư: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Đã khởi tạo số dư 50,000", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Lỗi kiểm tra số dư: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Người dùng chưa đăng nhập
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng ứng dụng", Toast.LENGTH_SHORT).show();
            // Có thể chuyển hướng đến màn hình đăng nhập nếu cần
        }
    }

    // Gắn menu vào Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Hàm thay thế Fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, fragment);
        fragmentTransaction.commit();

    }

    public void Logout(){
        FirebaseAuth.getInstance().signOut();
        // Xóa trạng thái đăng nhập
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
    }

    // Xử lý sự kiện khi nhấn item trong menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_scan_qr) {
            // Kiểm tra quyền camera trước khi quét
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                startQRScanner();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Khởi động quét mã QR
//    private void startQRScanner() {
//        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//        integrator.setPrompt("Quét mã QR trên xe bus");
//        integrator.setCameraId(0);
//        integrator.setBeepEnabled(true);
//        integrator.setBarcodeImageEnabled(false);
//        integrator.setOrientationLocked(false);
//        integrator.initiateScan();
//    }
    private void startQRScanner() {
        Intent intent = new Intent(this, QRScannerActivity.class);
        startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
    }

    // Xử lý kết quả quét mã QR
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null) {
//            if (result.getContents() == null) {
//                Toast.makeText(this, "Hủy quét mã QR", Toast.LENGTH_SHORT).show();
//            } else {
//                String qrData = result.getContents(); // Ví dụ: "route_id=1"
//                processQRData(qrData);
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String scanResult = data.getStringExtra("SCAN_RESULT");
            processQRData(scanResult);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Cần quyền camera để quét mã QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Xử lý dữ liệu từ mã QR
    private void processQRData(String qrData) {
        try {
            String[] parts = qrData.split("=");
            if (parts.length != 2 || !parts[0].equals("route_id")) {
                Toast.makeText(this, "Mã QR không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            String routeId = parts[1];
            saveTripAndUpdateBalance(routeId);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Lưu thông tin chuyến đi và cập nhật số dư
    private void saveTripAndUpdateBalance(final String routeId) {
        DatabaseReference routeRef = databaseReference.child("route");

        // Lấy dữ liệu từ route
        routeRef.orderByChild("id").equalTo(routeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot routeSnapshot) {
                if (routeSnapshot.exists()) {
                    for (DataSnapshot snapshot : routeSnapshot.getChildren()) {
                        Long routePriceLong = snapshot.child("price").getValue(Long.class);
                        String routeName = snapshot.child("name").getValue(String.class);
                        String operation = snapshot.child("operation").getValue(String.class);
                        if (routePriceLong != null) {
                            double routePrice = routePriceLong.doubleValue();
                            if (isRouteOperational(operation)) {
                                String scanTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                                String tripId = databaseReference.child("trip").push().getKey();
                                Map<String, Object> tripData = new HashMap<>();
                                tripData.put("user_id", userId);
                                tripData.put("route_id", routeId);
                                tripData.put("scan_time", scanTime);
                                tripData.put("price", routePrice);

                                // Lưu vào bảng trip
                                databaseReference.child("trip").child(tripId).setValue(tripData, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error != null) {
                                            Toast.makeText(MainActivity.this, "Lỗi lưu chuyến đi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            updateUserBalance(routeName, routePrice, scanTime);
                                        }
                                    }
                                });
                            }else {
                                Toast.makeText(MainActivity.this,  routeName + " không hoạt động vào thời điểm này", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Không tìm thấy giá tiền cho " + routeName, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else{
                    Toast.makeText(MainActivity.this, "Tuyến không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi truy vấn tuyến: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isRouteOperational(String operation) {
        if (operation == null) return false;

        try {
            String[] times = operation.split("-");
            String startTime = times[0];
            String endTime = times[1];

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String currentTime = timeFormat.format(new Date());

            int startMinutes = timeToMinutes(startTime);
            int endMinutes = timeToMinutes(endTime);
            int currentMinutes = timeToMinutes(currentTime);
            //return true;
            return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
        } catch (Exception e) {
            return false;
        }
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    // Cập nhật số dư tài khoản người dùng
    private void updateUserBalance(String routeName, double routePrice, String scanTime) {
        final DatabaseReference balanceRef = databaseReference.child("user_balance").child(userId);

        balanceRef.child("balance").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Double currentBalance = task.getResult().getValue(Double.class);
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }

                if (currentBalance < routePrice) {
                    Toast.makeText(MainActivity.this, "Số dư không đủ", Toast.LENGTH_SHORT).show();
                } else {
                    double newBalance = currentBalance - routePrice;
                    Map<String, Object> balanceUpdate = new HashMap<>();
                    balanceUpdate.put("balance", newBalance);
                    balanceUpdate.put("last_updated", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                    balanceRef.setValue(balanceUpdate, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Toast.makeText(MainActivity.this, "Lỗi cập nhật số dư: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                showScanSuccessDialog(routeName, routePrice, scanTime);
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(MainActivity.this, "Lỗi lấy số dư: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showScanSuccessDialog(String routeName, double price, String scanTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_scan_success, null);
        builder.setView(dialogView);

        TextView tvRouteName = dialogView.findViewById(R.id.tv_route_name);
        TextView tvPrice = dialogView.findViewById(R.id.tv_price);
        TextView tvScanTime = dialogView.findViewById(R.id.tv_scan_time);
        Button btnOk = dialogView.findViewById(R.id.btn_ok);

        tvRouteName.setText(routeName);
        tvPrice.setText("Giá tiền: " + String.format("%,.0f", price) + " VNĐ");
        tvScanTime.setText("Thời gian: " + scanTime);

        AlertDialog dialog = builder.create();

        // Làm mờ hình nền
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.dimAmount = 0.75f;
            window.setAttributes(layoutParams);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialog.show();

        btnOk.setOnClickListener(v -> dialog.dismiss());
    }
}
