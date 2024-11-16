package com.example.busmap.Route;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.busmap.R;
import com.example.busmap.directionhelper.TaskLoadedCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import com.example.busmap.directionhelper.FetchURL;

public class BusRoute extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private GoogleMap mMap;
    private MarkerOptions place1, place2, place3;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_route);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maproute);
        mapFragment.getMapAsync(this);
        place1 = new MarkerOptions().position(new LatLng(10.98335004747691, 106.67431075125997)).title("Location 1");
        place2 = new MarkerOptions().position(new LatLng(10.980180239846662, 106.6756130977606)).title("Location 2");
        place3 = new MarkerOptions().position(new LatLng(10.978769631402264, 106.6757348698315)).title("Location 3");
        //new FetchURL(BusRoute.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "bus"), "bus");
        getDirections(place1.getPosition(), place2.getPosition());
        getDirections(place2.getPosition(), place3.getPosition());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("mylog", "Added Markers");
        mMap.addMarker(place1);
        mMap.addMarker(place2);
        mMap.addMarker(place3);
        //Áp dụng custom map style
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }
        //Lấy v trí hện tại
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        //Bật nút zoomout/zoomin
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Thêm các marker cho các điểm xe bus
    }

    private void getDirections(LatLng origin, LatLng destination) {
        String url = getDirectionsUrl(origin, destination);
        // Thực hiện yêu cầu API
        new FetchURL(BusRoute.this).execute(url, "driving");
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = origin.latitude + "," + origin.longitude;
        String str_dest = dest.latitude + "," + dest.longitude;
        String url = "http://router.project-osrm.org/route/v1/driving/" + str_origin + ";" + str_dest + "?overview=full&geometries=geojson";
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        // Assuming values[0] is the list of LatLngs
        ArrayList<LatLng> latLngs = (ArrayList<LatLng>) values[0];

        for (LatLng point : latLngs) {
            polylineOptions.add(point);
        }

        polylineOptions.width(10).color(Color.BLUE);
        currentPolyline = mMap.addPolyline(polylineOptions);
    }
}

//    private String getDirectionsUrl(LatLng origin, LatLng dest) {
//        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
//        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        String mode = "mode=driving";
//        String parameters = str_origin + "&" + str_dest + "&" + mode;
//        String output = "json";
//        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyCn9kI97-3ktdV8HESIOQsYb5ULODunK8A";
//        return url;
//    }

//    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
//        // Origin of route
//        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
//        // Destination of route
//        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        // Mode
//        String mode = "mode=" + directionMode;
//        // Building the parameters to the web service
//        String parameters = str_origin + "&" + str_dest + "&" + mode;
//        // Output format
//        String output = "json";
//        // Building the url to the web service
//        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key = AIzaSyCn9kI97-3ktdV8HESIOQsYb5ULODunK8A";
//        return url;
//    }

    // Đoạn mã này sẽ vẽ Polyline sau khi nhận được dữ liệu từ Directions API
//    @Override
//    public void onTaskDone(Object... values) {
//        if (currentPolyline != null)
//            currentPolyline.remove();
//        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
//    }
