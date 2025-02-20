package com.example.busmap.Route;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.busmap.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RadaBusActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private TextView radiusValue;
    private ImageButton btnIncrease, btnDecrease;
    private int radius = 500; // Đơn vị: mét
    private Circle searchCircle;
    private final Map<Integer, Marker> stationMarkers = new HashMap<>();

    private DatabaseReference databaseRef;
    private FusedLocationProviderClient fusedLocationClient;

    private final OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            // Kiểm tra quyền truy cập vị trí trước khi bật my-location layer và lấy vị trí người dùng.
            if (ContextCompat.checkSelfPermission(RadaBusActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                getUserLocation();
            } else {
                ActivityCompat.requestPermissions(RadaBusActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rada_bus); // Đảm bảo layout file có tên như vậy

        // Ánh xạ view
        radiusValue = findViewById(R.id.radiusValue);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        radiusValue.setText(radius + "m");

        // Khởi tạo Firebase và FusedLocationProviderClient
        databaseRef = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Lấy SupportMapFragment ra và đăng ký OnMapReadyCallback
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapReadyCallback);
        }

        // Xử lý event cho các nút zoom
        btnIncrease.setOnClickListener(v -> updateRadius(100));
        btnDecrease.setOnClickListener(v -> updateRadius(-100));
    }

    /**
     * Lấy vị trí người dùng:
     * - Kiểm tra quyền trước khi gọi API.
     * - Nếu không lấy được vị trí, sử dụng fallback location: 10.980566818836298, 106.67545813755937.
     */
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("RadaBusActivity", "Quyền truy cập vị trí chưa được cấp.");
            return;
        }
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                LatLng userLocation;
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                } else {
                    // Fallback location nếu không lấy được vị trí người dùng.
                    userLocation = new LatLng(10.980566818836298, 106.67545813755937);
                    Log.w("RadaBusActivity", "Không lấy được vị trí, sử dụng fallback location.");
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

                // Vẽ vòng tròn hiển thị bán kính tìm kiếm
                if (searchCircle != null) {
                    searchCircle.remove();
                }
                searchCircle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(radius)
                        .strokeWidth(2f)
                        .strokeColor(0xFF0000FF)
                        .fillColor(0x220000FF));

                // Tải và hiển thị các trạm nằm trong bán kính
                loadNearbyStations(userLocation, radius);
            }).addOnFailureListener(e -> Log.e("RadaBusActivity", "Không thể lấy vị trí người dùng.", e));
        } catch (SecurityException se) {
            Log.e("RadaBusActivity", "SecurityException: " + se.getMessage());
        }
    }

    /**
     * Thay đổi bán kính tìm kiếm và cập nhật bản đồ.
     */
    private void updateRadius(int change) {
        radius += change;
        if (radius < 100) {
            radius = 100;
        }
        radiusValue.setText(radius + "m");

        if (mMap != null && searchCircle != null) {
            searchCircle.setRadius(radius);
            LatLng center = searchCircle.getCenter();
            removeMarkers();
            loadNearbyStations(center, radius);
        }
    }

    /**
     * Xóa các marker hiện có.
     */
    private void removeMarkers() {
        for (Marker marker : stationMarkers.values()) {
            marker.remove();
        }
        stationMarkers.clear();
    }

    /**
     * Tải dữ liệu các trạm từ Firebase và hiển thị lên bản đồ nếu nằm trong bán kính.
     */
    private void loadNearbyStations(final LatLng center, final int radius) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id == null || lat == null || lng == null || name == null) {
                        Log.e("Firebase", "Dữ liệu trạm không hợp lệ, bỏ qua.");
                        continue;
                    }

                    LatLng stationLocation = new LatLng(lat, lng);
                    double distance = calculateDistance(center, stationLocation);
                    if (distance <= radius) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (mMap != null) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(stationLocation)
                                        .title(name)
                                        .icon(bitmapDescriptorFromVector(R.drawable.ic_station_big))); // Dùng icon tùy chỉnh
                                if (marker != null) {
                                    stationMarkers.put(id, marker);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu station", error.toException());
            }
        });
    }

    /**
     * Chuyển đổi vector drawable thành BitmapDescriptor.
     */
    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) {
            Log.e("RadaBusActivity", "Không tìm thấy  a1 icon: " + vectorResId);
            return BitmapDescriptorFactory.defaultMarker();
        }
        vectorDrawable.setBounds(0, 0,
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Tính khoảng cách giữa 2 tọa độ sử dụng công thức Haversine.
     */
    private double calculateDistance(LatLng loc1, LatLng loc2) {
        double earthRadius = 6371000; // Đơn vị: mét
        double dLat = Math.toRadians(loc2.latitude - loc1.latitude);
        double dLng = Math.toRadians(loc2.longitude - loc1.longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(loc1.latitude))
                * Math.cos(Math.toRadians(loc2.latitude))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException se) {
                    Log.e("RadaBusActivity", "SecurityException: " + se.getMessage());
                }
            } else {
                Log.e("RadaBusActivity", "Quyền truy cập vị trí bị từ chối.");
            }
        }
    }
}