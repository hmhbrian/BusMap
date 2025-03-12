package com.example.busmap.Route.FindRoute;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.FindRouteHelper.LocationData;
import com.example.busmap.FindRouteHelper.LocationManager;
import com.example.busmap.R;
import com.example.busmap.entities.station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailMoveFragment extends Fragment {
    private RecyclerView rV_BusStop;
    private RouteInstructionAdapter RouteInstructionAdapter;
    //private DatabaseReference databaseRef;
    private Map<String, List<station>> stationOfRoute;
//    private LatLng userLocation;
//    private FusedLocationProviderClient fusedLocationClient;

    public static DetailMoveFragment newInstance(Map<String, List<station>> stationOfRoute) {
        DetailMoveFragment fragment = new DetailMoveFragment();
        Bundle args = new Bundle();
        args.putSerializable("stationOfRoute", (Serializable) stationOfRoute);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //databaseRef = FirebaseDatabase.getInstance().getReference();
        if (getArguments() != null) {
            stationOfRoute = (Map<String, List<station>>) getArguments().getSerializable("stationOfRoute");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_list, container, false);
        rV_BusStop = view.findViewById(R.id.recycler_view);
        rV_BusStop.setLayoutManager(new LinearLayoutManager(getContext()));
        List<String> routeNames = new ArrayList<>(stationOfRoute.keySet());

        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        LatLng userLocation = LocationManager.getInstance().getLatLng();
        LocationData to = LocationManager.getInstance().getToLocation();

        getPricesForRoutes(routeNames, new PriceListCallback() {
            @Override
            public void onPricesReceived(Map<String, Integer> priceMap) {
                if(userLocation != null && to != null){
                    Log.e("DETAILMOVE","userLocation: "+ userLocation.latitude);
                    Log.e("DETAILMOVE","to: "+ to.getName());
                    RouteInstructionAdapter = new RouteInstructionAdapter(getContext(),stationOfRoute,userLocation,to,priceMap);
                    rV_BusStop.setAdapter(RouteInstructionAdapter);
                    RouteInstructionAdapter.notifyDataSetChanged();
                }
                else{
                    Log.e("DETAILMOVE","userLocation và to rỗng");
                }
            }
        });

        return view;
    }

    public void getPricesForRoutes(List<String> routeNames, PriceListCallback callback) {
        Map<String, Integer> priceMap = new HashMap<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("route");

        for (String routeName : routeNames) {
            databaseReference.orderByChild("name").equalTo(routeName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot routeSnapshot : snapshot.getChildren()) {
                                Integer price = routeSnapshot.child("price").getValue(Integer.class);
                                if (price != null) {
                                    priceMap.put(routeName,price);
                                }
                            }

                            // Khi đủ số lượng giá trị, gọi callback
                            if (priceMap.size() == routeNames.size()) {
                                callback.onPricesReceived(priceMap);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "Error: " + error.getMessage());
                        }
                    });
        }
    }

    public interface PriceListCallback {
        void onPricesReceived(Map<String, Integer> priceMap);
    }

//    private void getCurrentLocation() {
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//
//        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
//            if (location != null) {
//                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
//            }
//        });
//    }

}
