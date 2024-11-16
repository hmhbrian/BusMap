package com.example.busmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
//    private void addBusMarkers(){
//        List<LatLng> busLocations = new ArrayList<>();
//        LatLng latLng1 = new LatLng(10.98335004747691, 106.67431075125997);
//        LatLng latLng2 = new LatLng(10.980180239846662, 106.6756130977606);
//        LatLng latLng3 = new LatLng(10.978769631402264, 106.6757348698315);
//        busLocations.add(latLng1);
//        busLocations.add(latLng2);
//        busLocations.add(latLng3);
//
//        for (LatLng location : busLocations) {
//            mMap.addMarker(new MarkerOptions()
//                    .position(location)
//                    .title("Điểm Xe Bus")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Tùy chỉnh icon nếu cần
//        }
//
//        PolylineOptions polylineOptions = new PolylineOptions()
//                .add(latLng1).add(latLng2).add(latLng3)
//                .color(Color.GREEN)
//                .width(7);
//        Polyline polyline = mMap.addPolyline(polylineOptions);
//        // Di chuyển camera đến vị trí đầu tiên
//        if (!busLocations.isEmpty()) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busLocations.get(0), 12));
//        }
//    }

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

    private void loadBusStations(){
        databaseReference.child("station").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LatLng> busStations = new ArrayList<>();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    double lat = snapshot1.child("lat").getValue(Double.class);
                    double lng = snapshot1.child("lng").getValue(Double.class);
                    String name = snapshot1.child("name").getValue(String.class);

                    //thêm station vào List
                    LatLng stationLocation = new LatLng(lat,lng);
                    busStations.add(stationLocation);
                    //thêm marker
                    mMap.addMarker(new MarkerOptions()
                            .position(stationLocation)
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
//                if (!busStations.isEmpty()) {
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStations.get(0), 12));
//                }
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