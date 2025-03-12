package com.example.busmap.Route.FindRoute;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

import com.example.busmap.FindRouteHelper.LocationData;
import com.example.busmap.FindRouteHelper.LocationManager;
import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindRouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseReference databaseRef;
    //private List<LatLng> stationLocations = new ArrayList<>();
    private Map<String, List<LatLng>> stationLocationsMap = new HashMap<>();
    private Map<String, List<station>> stationOfRoute = new HashMap<>();
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private Map<Integer, Marker> stationMarkers = new HashMap<>();
    private ViewPager2 viewPager;

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

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        setHalfScreenHeight(); // Đặt Bottom Sheet chiếm 1/2 màn hình
        setupBottomSheetCallback();
        if (!stationsMap.isEmpty()) {
            loadStationDetails(stationsMap);
        }
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

    private void setupTabLayout(Map<String, List<station>> StationOfRoute) {
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        List<String> routeNames = new ArrayList<>(StationOfRoute.keySet());
        Log.e("FINDDETAIL",String.valueOf(routeNames.size()));
        RoutePagerFindAdapter adapter = new RoutePagerFindAdapter(this,StationOfRoute);
        viewPager.setAdapter(adapter);


        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Chi tiết cách đi" : "Các trạm đi qua")
        ).attach();

    }

    private void loadStationDetails(Map<String, List<Integer>> stationsMap) {
        databaseRef.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot stationSnapshot) {
                //Map<Integer, station> stationMap = new HashMap<>();

                if (FindRouteDetailActivity.this.isFinishing() || mMap == null) {
                    Log.e("BusRouteActivity", "Activity đã bị hủy hoặc Google Map chưa sẵn sàng.");
                    return;
                }

                stationMarkers.clear(); // Xóa các marker cũ trước khi tải mới
                stationLocationsMap.clear(); // Xóa danh sách tuyến cũ

                for (String routeName : stationsMap.keySet()) {
                    List<Integer> stationIds = stationsMap.get(routeName);
                    List<LatLng> locations = new ArrayList<>();
                    List<station> stations = new ArrayList<>();

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
                            LatLng location = new LatLng(lat, lng);
                            locations.add(location);
                            stations.add(new station(id,name,lat,lng));

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
                    // Lưu danh sách tọa độ theo tuyến
                    stationLocationsMap.put(routeName, locations);
                    stationOfRoute.put(routeName,stations);
                }
                LatLng currentLocation = LocationManager.getInstance().getLatLng();
                LocationData to = LocationManager.getInstance().getToLocation();


                // Vẽ polyline sau khi đã có danh sách trạm dừng
                drawPolyline(to);

                // Cập nhật camera để hiển thị trạm dừng đầu tiên
                if (!stationLocationsMap.isEmpty()) {
                    String firstRoute = stationLocationsMap.keySet().iterator().next();
                    if (!stationLocationsMap.get(firstRoute).isEmpty()) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stationLocationsMap.get(firstRoute).get(0), 14));
                    }
                }
                setupTabLayout(stationOfRoute);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to fetch stations", error.toException());
            }
        });
    }

    private void drawPolyline( LocationData to) {
        int[] colors = {Color.GREEN,Color.RED, Color.BLUE, Color.YELLOW}; // Các màu khác nhau
        int colorIndex = 0;

        LatLng lastPoint = null;
        for (String routeName : stationLocationsMap.keySet()) {
            List<LatLng> locations = stationLocationsMap.get(routeName);

            if (locations.size() > 1) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(locations)
                        .width(8)
                        .color(colors[colorIndex % colors.length]); // Gán màu theo thứ tự

                mMap.addPolyline(polylineOptions);
                colorIndex++; // Chuyển sang màu tiếp theo
                lastPoint = locations.get(locations.size() - 1);
            }
        }
        LatLng ToPoint = new LatLng(to.getLatitude(),to.getLongitude());
        if (lastPoint != null && lastPoint != ToPoint) {
            PolylineOptions dashedLine = new PolylineOptions()
                    .add(lastPoint, ToPoint) // Nối từ điểm cuối của tuyến cuối cùng đến điểm mới
                    .width(8)
                    .color(Color.BLACK)
                    .pattern(Arrays.asList(new Dot(), new Gap(10))); // Nét đứt

            mMap.addPolyline(dashedLine);

            // Thêm marker màu đỏ tại điểm mới
            mMap.addMarker(new MarkerOptions()
                    .position(ToPoint)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("Điểm đến"));
        }
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


}
