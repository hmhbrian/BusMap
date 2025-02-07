package com.example.busmap.Route;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    private String routeId;
    private List<LatLng> stationLocations = new ArrayList<>();
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ViewPager2 viewPager;
    private RoutePagerAdapter pagerAdapter;
    private ArrayList<station> StationList = new ArrayList<>();

    void init() {
        viewPager = findViewById(R.id.view_pager);
        bottomSheet = findViewById(R.id.bottom_sheet_layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        init();
        routeId = getIntent().getStringExtra("route_id");
        databaseRef = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maproute);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        setHalfScreenHeight(); // Đặt Bottom Sheet chiếm 1/2 màn hình
        setupBottomSheetCallback();
        setupTabLayout();
    }
    private void setHalfScreenHeight() {
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int halfScreenHeight = screenHeight / 2; // Lấy 50% chiều cao màn hình
            bottomSheetBehavior.setPeekHeight(halfScreenHeight); // Mở 1/2 màn hình
        });
    }

    public void moveToStation(double latitude, double longitude) {
        if (mMap != null) {
            LatLng stationLocation = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stationLocation, 15)); // Zoom gần vào trạm
        } else {
            Log.e("BusRouteActivity", "Google Map chưa sẵn sàng!");
        }
    }

    private void setupBottomSheetCallback() {
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    Log.d("BottomSheet", "Mở toàn màn hình");
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    Log.d("BottomSheet", "Thu nhỏ");
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (mMap != null) {
                    mMap.setPadding(0, 0, 0, (int) (slideOffset * bottomSheet.getHeight() / 2));
                }
            }
        });
    }



    private void setupTabLayout() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // Lấy routeId từ Intent
        String routeId = getIntent().getStringExtra("route_id");
        if (routeId == null) {
            Log.e("BusRouteActivity", "routeId không tìm thấy");
            return;
        }

        // Truyền routeId vào Adapter
        RoutePagerAdapter adapter = new RoutePagerAdapter(this, routeId);
        viewPager.setAdapter(adapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Biểu đồ giờ");
                    break;
                case 1:
                    tab.setText("Trạm dừng");
                    break;
                case 2:
                    tab.setText("Thông tin");
                    break;
            }
        }).attach();
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadRouteStations();
    }

    private void loadRouteStations() {
        databaseRef.child("busstop").orderByChild("route_id").equalTo(routeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot busStopSnapshot) {
                        List<Integer> stationIds = new ArrayList<>();
                        for (DataSnapshot snapshot : busStopSnapshot.getChildren()) {
                            int stationId = snapshot.child("station_id").getValue(Integer.class);
                            stationIds.add(stationId);
                        }
                        loadStationDetails(stationIds);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Failed to fetch bus stops", error.toException());
                    }
                });
    }

    private void loadStationDetails(List<Integer> stationIds) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                Map<Integer, station> stationMap = new HashMap<>();

                // Kiểm tra nếu Activity đã bị hủy hoặc Google Map chưa sẵn sàng
                if (BusRouteActivity.this.isFinishing() || mMap == null) {
                    Log.e("BusRouteActivity", "Activity đã bị hủy hoặc Google Map chưa sẵn sàng.");
                    return;
                }

                // Lấy icon trạm dừng từ drawable
                Drawable drawable = ContextCompat.getDrawable(BusRouteActivity.this, R.drawable.ic_station_big);
                if (drawable == null) {
                    Log.e("BusRouteActivity", "Không tìm thấy ic_station_big trong drawable!");
                    return;
                }

                // Chuyển drawable thành Bitmap
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);

                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    if (id == null || lat == null || lng == null || name == null) {
                        Log.e("Firebase", "Dữ liệu trạm không hợp lệ, bỏ qua.");
                        continue;
                    }

                    if (stationIds.contains(id)) {
                        stationMap.put(id, new station(id, name, lat, lng));

                        LatLng location = new LatLng(lat, lng);

                        // Chạy trên UI Thread để tránh lỗi Firebase
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (mMap != null) {
                                mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(name)
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))); // Sử dụng icon mới
                            }
                        });
                    }
                }

                // Cập nhật danh sách trạm dừng
                StationList.clear();
                for (int id : stationIds) {
                    if (stationMap.containsKey(id)) {
                        StationList.add(stationMap.get(id));
                    }
                }

                // Cập nhật danh sách vị trí các trạm
                stationLocations.clear();
                for (station sta : StationList) {
                    stationLocations.add(new LatLng(sta.getLatitude(), sta.getLongitude()));
                }

                // Vẽ tuyến đường sau khi có danh sách trạm
                drawPolyline();

                // Di chuyển camera đến trạm đầu tiên
                if (!stationLocations.isEmpty()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stationLocations.get(0), 14));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch stations", error.toException());
            }
        });
    }


    private void drawPolyline() {
        if (stationLocations.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(stationLocations)
                    .width(8)
                    .color(ContextCompat.getColor(this, R.color.green));  // Use ContextCompat for compatibility
            mMap.addPolyline(polylineOptions);
        }
    }

}