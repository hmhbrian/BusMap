package com.example.busmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.busmap.Route.FindRoadActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private DatabaseReference databaseReference;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if(mMap != null){
                //Lấy vị trí hện tại
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                //Bật nút zoomout/zoomin
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                setMapStyle();
                getCurrentLocation();
                loadBusStations();
            }else{
                Log.e("MapError", "GoogleMap is null.");
            }
            //addBusMarkers();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        LinearLayout FindRouteLayout = view.findViewById(R.id.linearFindRoute);
        FindRouteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FindRoadActivity.class);
                startActivity(intent);

            }
        });
    }

    private void loadBusStations() {
        databaseReference.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LatLng> busStations = new ArrayList<>();

                // Chuyển drawable thành Bitmap
                Drawable drawable = getContext().getDrawable(R.drawable.ic_station_big);
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    double lat = snapshot1.child("lat").getValue(Double.class);
                    double lng = snapshot1.child("lng").getValue(Double.class);
                    String name = snapshot1.child("name").getValue(String.class);

                    // Thêm station vào List
                    LatLng stationLocation = new LatLng(lat, lng);
                    busStations.add(stationLocation);

                    // Thêm marker với bitmap đã chuyển đổi
                    mMap.addMarker(new MarkerOptions()
                            .position(stationLocation)
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error loading bus stations", error.toException());
            }
        });
    }


    private void setMapStyle() {
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.map_style));
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }
    }
    private void getCurrentLocation() {
        LatLng currentLocation = new LatLng(10.98335004747691, 106.67431075125997);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // Zoom level 15
    }

}