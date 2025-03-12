package com.example.busmap.Route.FindRoute;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import com.example.busmap.FindRouteHelper.LocationManager;
import com.example.busmap.R;
import com.example.busmap.FindRouteHelper.LocationData;
import com.example.busmap.entities.station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InputFindActivity extends AppCompatActivity {
    private LinearLayout linear_CrtLocation, linear_OnMap;
    private ImageView ImgBack;
    private AutoCompleteTextView edtInput;
    private ActivityResultLauncher<Intent> resultLauncher;
    //private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference database;
    private ArrayAdapter<String> adapter;
    private List<station> stationList = new ArrayList<>();
    private station selectedStation;

    void init(){
        linear_CrtLocation = findViewById(R.id.Linear_CrtLocation);
        linear_OnMap = findViewById(R.id.Linear_OnMap);
        edtInput = findViewById(R.id.searchInput);
        ImgBack = findViewById(R.id.imgback);
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        database = FirebaseDatabase.getInstance().getReference();
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
            LatLng userLocation = LocationManager.getInstance().getLatLng();
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
        initListener();

        // Khởi tạo adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        edtInput.setAdapter(adapter);

        fetchStationsFromFirebase();

        edtInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedName = (String) adapterView.getItemAtPosition(i);
                for (station station : stationList) {
                    if (station.getName().equals(selectedName)) {
                        selectedStation = station; // Lưu trạm được chọn
                        break;

                    }
                }
               Toast.makeText(InputFindActivity.this, "lat: " + selectedStation.getLat() + "\nlng: "+ selectedStation.getLng()+"\nName: "+ selectedStation.getName(), Toast.LENGTH_SHORT).show();
                LocationData DreamLocation = new LocationData(selectedStation.getLat(), selectedStation.getLng(), selectedStation.getName());
                Intent resultIntent = new Intent();
                resultIntent.putExtra("Selected_Location", DreamLocation);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

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


    private void fetchStationsFromFirebase() {
        database.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> stationNames = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    station station = dataSnapshot.getValue(station.class);
                    if (station != null) {
                        stationList.add(station);
                        stationNames.add(station.getName());
                    }
                }
                adapter.clear();
                adapter.addAll(stationNames);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InputFindActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
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
