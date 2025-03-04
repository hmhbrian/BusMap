package com.example.busmap.Route.FindRoute;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.busmap.R;
import com.example.busmap.FindRouteHelper.LocationData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Arrays;
import java.util.List;

public class FindRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LinearLayout linear_From, linear_To;
    private TextView tvFrom, tvTo;
    private Spinner spn_select_Nroute;
    private Button btnFind;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<Intent> resultLauncher;
    private String clickedLinearId; // Lưu ID của TextView được click
    private LocationData from, to;
    private LatLng userLocation;
    int position = -1;

    void init(){
        linear_From = findViewById(R.id.ll_from);
        linear_To = findViewById(R.id.ll_to);
        spn_select_Nroute = findViewById(R.id.spn_Select_Nroute);
        btnFind = findViewById(R.id.btn_find_road);
        tvFrom = findViewById(R.id.tv_from);
        tvTo = findViewById(R.id.tv_to);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    void initListener(){
        // Sự kiện click chung cho cả 2 linear
        View.OnClickListener listener = v -> {
            clickedLinearId = v.getId() == R.id.ll_from ? "ll_from" : "ll_to"; // Lưu ID của TextView được click
            Intent intent = new Intent(FindRouteActivity.this,InputFindActivity.class);
            intent.putExtra("LINEAR_ID", clickedLinearId);
            resultLauncher.launch(intent);
        };
        linear_From.setOnClickListener(listener);
        linear_To.setOnClickListener(listener);

        spn_select_Nroute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                position = i;
                Toast.makeText(FindRouteActivity.this, "Bạn đã chọn: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì nếu không có gì được chọn
            }
        });

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentResult = new Intent(FindRouteActivity.this, ResultFindRouteActivity.class);
                intentResult.putExtra("From_Location", from);
                intentResult.putExtra("To_Location", to);
                intentResult.putExtra("choice",position);
                resultLauncher.launch(intentResult);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_route);
        init();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fm_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        String[] options = {"Đi tối đa 1 tuyến", "Đi tối đa 2 tuyến", "Đi tối đa 3 tuyến"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spn_select_Nroute.setAdapter(adapter);

        // Khởi tạo ActivityResultLauncher để nhận dữ liệu từ Activity3
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        LocationData locationData = result.getData().getParcelableExtra("Selected_Location");
                        if (locationData != null) {
                             // Gán giá trị vào đúng Linear đã được click
                            if ("ll_from".equals(clickedLinearId)) {
                                if(to != null){
                                    if(locationData.getLongitude() != to.getLongitude() && locationData.getLatitude() != to.getLatitude()){
                                        from = locationData;
                                        tvFrom.setText(from.getName());
                                    }else{
                                        Toast.makeText(this, "Tọa độ điểm xuất phát trùng điểm đến. Vui lòng nhập lại!", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    from = locationData;
                                    tvFrom.setText(from.getName());
                                }
                            } else if ("ll_to".equals(clickedLinearId)) {
                                if(from != null){
                                    if(locationData.getLongitude() != from.getLongitude() && locationData.getLatitude() != from.getLatitude()){
                                        to = locationData;
                                        tvTo.setText(to.getName());
                                    }else{
                                        Toast.makeText(this, "Tọa độ điểm đến trùng điểm xuất phát. Vui lòng nhập lại!", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    to = locationData;
                                    tvTo.setText(to.getName());
                                }
                            }
                            if (from != null && to != null) {
                                updateMap();
                            }
                        }
                    }
                }
        );
        initListener();
    }

    private void updateMap() {
        mMap.clear();
        LatLng pointFrom = new LatLng(from.getLatitude(), from.getLongitude());
        LatLng pointTo = new LatLng(to.getLatitude(), to.getLongitude());

        mMap.addMarker(new MarkerOptions().position(pointFrom).title("From: " + from.getName()));
        mMap.addMarker(new MarkerOptions().position(pointTo).title("To: " + to.getName()));

        // Vẽ đường Polyline giữa hai điểm
        List<LatLng> path = Arrays.asList(pointFrom, pointTo);
        mMap.addPolyline(new PolylineOptions()
                .addAll(path)
                .width(8)
                .color(android.graphics.Color.BLUE));

        // Di chuyển camera đến vị trí trung tâm giữa hai điểm
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pointFrom);
        builder.include(pointTo);
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí của bạn"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }
}

