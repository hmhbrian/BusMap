package com.example.busmap.busstopnear;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.dialog.StationDetailsDialog;
import com.example.busmap.entities.route;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadaBusActivity extends AppCompatActivity implements RadaAdapter.OnStationClickListener, RadaAdapter.OnStationDetailsClickListener  {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "RadaBusActivity";
    private static final long DEBOUNCE_TIME_MS = 300; // Debounce time in milliseconds

    private GoogleMap mMap;
    private TextView radiusValue;
    private TextView tvNoStations;
    private ImageButton btnIncrease, btnDecrease;
    private RecyclerView recyclerViewStations;
    private RadaAdapter radaAdapter;

    private int radius = 500; // Đơn vị: mét
    private Circle searchCircle;
    private final Map<Integer, Marker> stationMarkers = new HashMap<>();
    private LatLng currentUserLocation;

    private DatabaseReference databaseRef;
    private FusedLocationProviderClient fusedLocationClient;

    // Variables for tracking loading state and debouncing
    private boolean isLoading = false;
    private Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private ValueEventListener currentFirebaseListener;

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
        setContentView(R.layout.activity_rada_bus);

        // Ánh xạ view
        radiusValue = findViewById(R.id.radiusValue);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        recyclerViewStations = findViewById(R.id.recyclerViewStations);
        tvNoStations = findViewById(R.id.tvNoStations);

        radiusValue.setText(radius + "m");

        // Thiết lập RecyclerView và Adapter
        recyclerViewStations.setLayoutManager(new LinearLayoutManager(this));
        radaAdapter = new RadaAdapter(this, this); // Truyền vào listener cho nút chi tiết
        recyclerViewStations.setAdapter(radaAdapter);

        // Thêm đường kẻ giữa các item
        recyclerViewStations.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

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
    @Override
    public void onStationDetailsClick(int stationId) {
        // Khi nhấn vào nút chi tiết, gọi phương thức showRoutesForStation
        showRoutesForStation(stationId);
    }
    /**
     * Lấy vị trí người dùng:
     * - Kiểm tra quyền trước khi gọi API.
     * - Nếu không lấy được vị trí, sử dụng fallback location: 10.980566818836298, 106.67545813755937.
     */
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Quyền truy cập vị trí chưa được cấp.");
            return;
        }
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "Lấy được vị trí người dùng: " + currentUserLocation.latitude + ", " + currentUserLocation.longitude);
                } else {
                    // Fallback location nếu không lấy được vị trí người dùng.
                    currentUserLocation = new LatLng(10.980566818836298, 106.67545813755937);
                    Log.w(TAG, "Không lấy được vị trí, sử dụng fallback location.");
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15));

                // Vẽ vòng tròn hiển thị bán kính tìm kiếm
                if (searchCircle != null) {
                    searchCircle.remove();
                }
                searchCircle = mMap.addCircle(new CircleOptions()
                        .center(currentUserLocation)
                        .radius(radius)
                        .strokeWidth(2f)
                        .strokeColor(0xFF0000FF)
                        .fillColor(0x220000FF));

                // Tải và hiển thị các trạm nằm trong bán kính
                debouncedLoadNearbyStations(currentUserLocation, radius);
            }).addOnFailureListener(e -> Log.e(TAG, "Không thể lấy vị trí người dùng.", e));
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException: " + se.getMessage());
        }
    }

    /**
     * Thay đổi bán kính tìm kiếm và cập nhật bản đồ.
     */
    private void updateRadius(int change) {
        radius += change;
        if (radius < 100) {
            radius = 100;
        } else if (radius > 5000) {
            radius = 5000; // Giới hạn tối đa 5km
        }

        radiusValue.setText(radius + "m");

        if (mMap != null && searchCircle != null && currentUserLocation != null) {
            // Cập nhật bán kính của vòng tròn
            searchCircle.setRadius(radius);

            // Sử dụng debounced loading để tránh gọi quá nhiều request
            debouncedLoadNearbyStations(currentUserLocation, radius);
        }
    }

    /**
     * Sử dụng debounce để tránh gọi quá nhiều request khi người dùng thay đổi bán kính liên tục
     */
    private void debouncedLoadNearbyStations(final LatLng center, final int radius) {
        // Hủy runnable hiện tại nếu có
        if (searchRunnable != null) {
            debounceHandler.removeCallbacks(searchRunnable);
        }

        // Tạo runnable mới
        searchRunnable = () -> {
            // Xóa markers hiện tại trước khi tải mới
            removeMarkers();
            loadNearbyStations(center, radius);
        };

        // Đặt lịch chạy runnable sau khoảng thời gian debounce
        debounceHandler.postDelayed(searchRunnable, DEBOUNCE_TIME_MS);
    }

    /**
     * Xóa các marker hiện có.
     */
    private void removeMarkers() {
        if (mMap == null) return;

        // Xóa tất cả marker khỏi bản đồ
        for (Marker marker : stationMarkers.values()) {
            marker.remove();
        }
        stationMarkers.clear();
        Log.d(TAG, "Đã xóa tất cả marker");
    }

    /**
     * Tải dữ liệu các trạm từ Firebase và hiển thị lên bản đồ nếu nằm trong bán kính.
     */
    private void loadNearbyStations(final LatLng center, final int radius) {
        // Nếu đang tải dữ liệu, hủy bỏ
        if (isLoading) {
            Log.d(TAG, "Đang tải dữ liệu, bỏ qua yêu cầu mới");
            return;
        }

        isLoading = true;

        // Hủy listener trước đó nếu có
        if (currentFirebaseListener != null) {
            databaseRef.child("station").removeEventListener(currentFirebaseListener);
        }

        // Hiển thị loading hoặc thông báo đang tìm kiếm
        tvNoStations.setText("Đang tìm kiếm trạm gần đây...");
        tvNoStations.setVisibility(View.VISIBLE);
        recyclerViewStations.setVisibility(View.GONE);

        // Lưu lại giá trị bán kính hiện tại để kiểm tra sau khi tải xong
        final int currentSearchRadius = radius;

        currentFirebaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                // Kiểm tra xem bán kính tìm kiếm có thay đổi trong quá trình tải dữ liệu không
                if (currentSearchRadius != radius) {
                    Log.d(TAG, "Bán kính đã thay đổi trong quá trình tải dữ liệu, hủy kết quả");
                    isLoading = false;
                    return;
                }

                List<BusStation> nearbyStations = new ArrayList<>();

                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id == null || lat == null || lng == null || name == null) {
                        Log.e(TAG, "Dữ liệu trạm không hợp lệ, bỏ qua.");
                        continue;
                    }

                    LatLng stationLocation = new LatLng(lat, lng);
                    double distance = calculateDistance(center, stationLocation);

                    if (distance <= currentSearchRadius) {
                        // Thêm vào danh sách để hiển thị trong RecyclerView
                        BusStation station = new BusStation(id, name, lat, lng, distance);
                        nearbyStations.add(station);
                    }
                }

                // Sắp xếp các trạm theo khoảng cách gần nhất
                nearbyStations.sort((s1, s2) -> Double.compare(s1.getDistance(), s2.getDistance()));

                // Cập nhật UI trên main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Kiểm tra lại một lần nữa xem bán kính có thay đổi không
                    if (currentSearchRadius != radius) {
                        Log.d(TAG, "Bán kính đã thay đổi trước khi cập nhật UI, hủy kết quả");
                        isLoading = false;
                        return;
                    }

                    // Thêm markers lên bản đồ
                    for (BusStation station : nearbyStations) {
                        LatLng stationLocation = new LatLng(station.getLat(), station.getLng());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(stationLocation)
                                .title(station.getName())
                                .snippet("Khoảng cách: " + station.getFormattedDistance())
                                .icon(bitmapDescriptorFromVector(R.drawable.ic_station_big)));
                        if (marker != null) {
                            stationMarkers.put(station.getId(), marker);
                        }
                    }

                    // Cập nhật RecyclerView
                    radaAdapter.setStations(nearbyStations);

                    // Hiển thị thông báo nếu không tìm thấy trạm nào
                    if (nearbyStations.isEmpty()) {
                        tvNoStations.setText("Không tìm thấy trạm nào trong phạm vi " + currentSearchRadius + "m");
                        tvNoStations.setVisibility(View.VISIBLE);
                        recyclerViewStations.setVisibility(View.GONE);
                    } else {
                        tvNoStations.setVisibility(View.GONE);
                        recyclerViewStations.setVisibility(View.VISIBLE);
                    }

                    isLoading = false;
                    Log.d(TAG, "Đã tìm thấy " + nearbyStations.size() + " trạm trong phạm vi " + currentSearchRadius + "m");
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi khi lấy dữ liệu station", error.toException());
                new Handler(Looper.getMainLooper()).post(() -> {
                    tvNoStations.setText("Đã xảy ra lỗi khi tải dữ liệu trạm");
                    tvNoStations.setVisibility(View.VISIBLE);
                    recyclerViewStations.setVisibility(View.GONE);
                    isLoading = false;
                });
            }
        };

        databaseRef.child("station").addListenerForSingleValueEvent(currentFirebaseListener);
    }

    /**
     * Xử lý khi người dùng nhấn vào một trạm trong danh sách
     */
    @Override
    public void onStationClick(BusStation station) {
        LatLng stationLocation = new LatLng(station.getLat(), station.getLng());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(stationLocation));

        // Hiển thị info window của marker tương ứng
        Marker marker = stationMarkers.get(station.getId());
        if (marker != null) {
            marker.showInfoWindow();
        }

    }

    /**
     * Chuyển đổi vector drawable thành BitmapDescriptor.
     */
    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) {
            Log.e(TAG, "Không tìm thấy icon: " + vectorResId);
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
                    Log.e(TAG, "SecurityException: " + se.getMessage());
                }
            } else {
                Log.e(TAG, "Quyền truy cập vị trí bị từ chối.");
                Toast.makeText(this, "Cần quyền truy cập vị trí để sử dụng tính năng này",
                        Toast.LENGTH_LONG).show();

                // Sử dụng vị trí mặc định nếu không được cấp quyền
                currentUserLocation = new LatLng(10.980566818836298, 106.67545813755937);
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15));

                    if (searchCircle != null) {
                        searchCircle.remove();
                    }
                    searchCircle = mMap.addCircle(new CircleOptions()
                            .center(currentUserLocation)
                            .radius(radius)
                            .strokeWidth(2f)
                            .strokeColor(0xFF0000FF)
                            .fillColor(0x220000FF));

                    debouncedLoadNearbyStations(currentUserLocation, radius);
                }
            }
        }
    }
    private void showRoutesForStation(final int stationId) {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("busstop")
                .orderByChild("station_id")
                .equalTo(stationId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> routeIds = new ArrayList<>();
                        for (DataSnapshot busStopSnapshot : snapshot.getChildren()) {
                            String routeId = busStopSnapshot.child("route_id").getValue(String.class);
                            if (routeId != null) {
                                routeIds.add(routeId);
                            }
                        }
                        fetchRouteDetails(routeIds, stationId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi lấy dữ liệu trạm", error.toException());
                    }
                });
    }

    private void fetchRouteDetails(List<String> routeIds, final int stationId) {
        databaseRef.child("route")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<route> routeDetails = new ArrayList<>(); // Sử dụng List<route> thay vì List<String>

                        for (DataSnapshot routeSnapshot : snapshot.getChildren()) {
                            String id = routeSnapshot.child("id").getValue(String.class);
                            String name = routeSnapshot.child("name").getValue(String.class);
                            String operation = routeSnapshot.child("operation").getValue(String.class);

                            // Các giá trị mặc định cho những thuộc tính chưa có
                            int startStationId = 0; // Giá trị mặc định
                            int endStationId = 0;   // Giá trị mặc định
                            double price = 0.0;      // Giá trị mặc định

                            // Kiểm tra và thêm đối tượng route vào danh sách nếu phù hợp
                            if (id != null && name != null && operation != null && routeIds.contains(id)) {
                                routeDetails.add(new route(endStationId, id, name, operation, price, startStationId));
                            }
                        }

                        // Sử dụng lớp StationDetailsDialog để hiển thị dialog với thông tin chi tiết tuyến bus
                        new StationDetailsDialog(RadaBusActivity.this, stationId, routeDetails).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi lấy dữ liệu tuyến bus", error.toException());
                    }
                });
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Xóa các callbacks để tránh memory leak
        if (searchRunnable != null) {
            debounceHandler.removeCallbacks(searchRunnable);
        }

        // Xóa listener Firebase nếu có
        if (currentFirebaseListener != null) {
            databaseRef.child("station").removeEventListener(currentFirebaseListener);
        }
    }
}
