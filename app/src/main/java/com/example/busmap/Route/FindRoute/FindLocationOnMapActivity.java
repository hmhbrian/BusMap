package com.example.busmap.Route.FindRoute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindLocationOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton btnBack, btnMyLocation;
    private Button btnSelect;
    private Marker currentMarker;
    private LocationData selected_coordinate;
    private String LinearId;
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();
        // Lắng nghe sự kiện khi người dùng bấm vào bản đồ
        mMap.setOnMapClickListener(latLng -> {
            // Xóa marker cũ nếu có
            if (currentMarker != null) {
                currentMarker.remove();
            }

            // Thêm marker tại vị trí mới
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Tọa độ: " + latLng.latitude + ", " + latLng.longitude));

            // Di chuyển camera đến vị trí chọn
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            selected_coordinate = new LocationData(latLng.latitude,latLng.longitude,"[ Tọa độ điểm ]");
            // Hiển thị tọa độ trong Toast
            //Toast.makeText(this, "Latitude: " + latLng.latitude + "\nLongitude: " + latLng.longitude, Toast.LENGTH_SHORT).show();
        });
    }

    void init(){
        btnBack = findViewById(R.id.btnBack);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        btnSelect = findViewById(R.id.btnSelectLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    void initListener(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentResult = new Intent(FindLocationOnMapActivity.this, InputFindActivity.class);
                startActivity(intentResult);
            }
        });
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });
        btnSelect.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Selected_Location", selected_coordinate);
            //resultIntent.putExtra("LINEAR_ID",LinearId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_onmap);
        init();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        LinearId = getIntent().getStringExtra("LINEAR_ID");
        initListener();
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
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí của bạn"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
        });
    }

}
