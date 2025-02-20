package com.example.busmap.Route.FindRoute;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.busmap.R;
import com.example.busmap.entities.LocationData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

public class InputFindActivity extends AppCompatActivity {
    private LinearLayout linear_CrtLocation, linear_OnMap;
    private ImageView ImgBack;
    private AutoCompleteTextView edtInput;
    private PlacesClient placesClient;
    private List<String> placeSuggestions = new ArrayList<>();
    private ActivityResultLauncher<Intent> resultLauncher;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLocation;

    void init(){
        linear_CrtLocation = findViewById(R.id.Linear_CrtLocation);
        linear_OnMap = findViewById(R.id.Linear_OnMap);
        edtInput = findViewById(R.id.searchInput);
        ImgBack = findViewById(R.id.imgback);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    void initListener(){
        ImgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentBack = new Intent(InputFindActivity.this,FindRouteActivity.class);
                startActivity(intentBack);
            }
        });
        linear_CrtLocation.setOnClickListener(view -> {
            LocationData currentLocation = new LocationData(userLocation.latitude, userLocation.longitude, "[ Vị trí hiện tại ]");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Selected_Location", currentLocation);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        linear_OnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InputFindActivity.this, FindLocationOnMapActivity.class);
                resultLauncher.launch(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_find);
        init();
        getCurrentLocation();
        initListener();

//        Places.initialize(getApplicationContext(), "AIzaSyCn9kI97-3ktdV8HESIOQsYb5ULODunK8A");
//        placesClient = Places.createClient(this);
//        edtInput.setOnClickListener(v -> edtInput.showDropDown());
//        edtInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (!s.toString().isEmpty()) {
//                    getPlacePredictions(s.toString());
//                }
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Lấy dữ liệu từ Activity FindOnMapActivity
                        LocationData locationData = result.getData().getParcelableExtra("Selected_Location");

                        // Trả kết quả về Activity FindRouteActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("Selected_Location", locationData);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                });

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
            }
        });
    }


//    private void getPlacePredictions(String query) {
//        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
//        FindAutocompletePredictionsRequest request =
//                FindAutocompletePredictionsRequest.builder()
//                        .setSessionToken(token)
//                        .setQuery(query)
//                        .build();
//
//        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
//            placeSuggestions.clear();
//            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
//                placeSuggestions.add(prediction.getPrimaryText(null).toString());
//            }
//            updateAutoCompleteTextView();
//        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//
//    }
//
//    private void updateAutoCompleteTextView() {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, placeSuggestions);
//        edtInput.setAdapter(adapter);
//    }

}
