package com.example.busmap.Route.FindRoute;

import android.content.Intent;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class FindRouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    private List<LatLng> stationLocations = new ArrayList<>();
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ArrayList<station> StationList = new ArrayList<>();
    private Map<Integer, Marker> stationMarkers = new HashMap<>();
    private ViewPager2 viewPager;
    private String firstRouteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_route_detail);

        bottomSheet = findViewById(R.id.bottom_sheet_layout);
        viewPager = findViewById(R.id.viewPager);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maproute);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Intent intent = getIntent();
        HashMap<String, ArrayList<Integer>> stationHashMap = (HashMap<String, ArrayList<Integer>>) intent.getSerializableExtra("stationsMap");

        Map<String, List<Integer>> stationsMap = new HashMap<>();
        if (stationHashMap != null) {
            for (Map.Entry<String, ArrayList<Integer>> entry : stationHashMap.entrySet()) {
                stationsMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        if (!stationsMap.isEmpty()) {
            firstRouteId = stationsMap.keySet().iterator().next(); // Lấy routeId đầu tiên
            List<Integer> firstStationList = stationsMap.get(firstRouteId);

            loadStationDetails(firstStationList);
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        setHalfScreenHeight(); // Đặt Bottom Sheet chiếm 1/2 màn hình
        setupBottomSheetCallback();
        setupTabLayout();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void setHalfScreenHeight() {
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int halfScreenHeight = screenHeight / 3; // Lấy 33% chiều cao màn hình
            bottomSheetBehavior.setPeekHeight(halfScreenHeight); // Mở 1/2 màn hình
        });
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
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        Log.e("FINDDETAIL","Route_id: "+ firstRouteId);
        for(station sta : StationList)
            Log.e("FINDDETAIL",sta.getName());
        RoutePagerFindAdapter adapter = new RoutePagerFindAdapter(this,StationList,firstRouteId);
        viewPager.setAdapter(adapter);


        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Chi tiết cách đi" : "Các trạm đi qua")
        ).attach();

    }

    private void loadStationDetails(List<Integer> stationIds) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                Map<Integer, station> stationMap = new HashMap<>();

                if (FindRouteDetailActivity.this.isFinishing() || mMap == null) {
                    Log.e("BusRouteActivity", "Activity đã bị hủy hoặc Google Map chưa sẵn sàng.");
                    return;
                }

                stationMarkers.clear(); // Xóa các marker cũ trước khi tải mới
                StationList.clear();
                for (DataSnapshot snapshot : stationSnapshot.getChildren()) {
                    Integer id = snapshot.child("id").getValue(Integer.class);
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    // Kiểm tra các giá trị null
                    if (id == null || lat == null || lng == null || name == null) {
                        Log.e("Firebase", "Dữ liệu trạm không hợp lệ, bỏ qua.");
                        continue;
                    }

                    if (stationIds.contains(id)) {
                        // Thêm station vào danh sách
                        stationMap.put(id, new station(id, name, lat, lng));
                        StationList.add(new station(id, name, lat, lng));
                        LatLng location = new LatLng(lat, lng);

                        // Chạy trên UI Thread để cập nhật giao diện
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (mMap != null) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title(name)
                                        .icon(bitmapDescriptorFromVector(R.drawable.ic_station_big))); // Sử dụng icon mới

                                // Kiểm tra và gắn marker vào map
                                if (marker != null) {
                                    stationMarkers.put(id, marker);
                                }
                            }
                        });
                    }
                }

                // Cập nhật danh sách các trạm
//                StationList.clear();
//                for (int id : stationIds) {
//                    if (stationMap.containsKey(id)) {
//                        StationList.add(stationMap.get(id));
//                    }
//                }

                // Cập nhật danh sách tọa độ
                stationLocations.clear();
                for (station sta : StationList) {
                    stationLocations.add(new LatLng(sta.getLat(), sta.getLng()));
                }

                // Vẽ polyline sau khi đã có danh sách trạm dừng
                drawPolyline();

                // Cập nhật camera để hiển thị trạm dừng đầu tiên
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

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) {
            Log.e("BusRouteActivity", "Không tìm thấy icon: " + vectorResId);
            return BitmapDescriptorFactory.defaultMarker(); // Trả về marker mặc định nếu lỗi
        }

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
