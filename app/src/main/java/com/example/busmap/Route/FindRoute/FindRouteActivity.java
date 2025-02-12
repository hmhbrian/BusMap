package com.example.busmap.Route.FindRoute;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class FindRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LinearLayout linear_From, linear_To;
    private EditText edtFrom, edtTo;
    private Button btnFind;

    void init(){
        linear_From = findViewById(R.id.ll_from);
        linear_To = findViewById(R.id.ll_to);
        btnFind = findViewById(R.id.btn_find_road);
        edtFrom = findViewById(R.id.tv_from);
        edtTo = findViewById(R.id.tv_to);
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
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentResult = new Intent(FindRouteActivity.this, ResultFindRouteActivity.class);
                startActivity(intentResult);
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
}

