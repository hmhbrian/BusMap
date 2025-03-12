package com.example.busmap.Route.FindRoute;

import static com.example.busmap.FindRouteHelper.Tranfers.StringNumberExtractor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmap.R;
import com.example.busmap.Route.RouteDetail.BusRouteActivity;
import com.example.busmap.Route.RouteDetail.StationAdapter;
import com.example.busmap.Route.RouteDetail.StationListFragment;
import com.example.busmap.entities.station;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import java.util.ArrayList;

public class BusStopPassFragment extends Fragment {
    private RecyclerView rV_BusStop;
    private BusStopAdapter BusStopAdapter;
    private DatabaseReference databaseRef;
    private Map<String, List<station>> stationOfRoute;
    private String routeId;
    private ArrayList<station> BusStopList = new ArrayList<>();
    private String routename;

    public static BusStopPassFragment newInstance(Map<String, List<station>> stationOfRoute) {
        BusStopPassFragment fragment = new BusStopPassFragment();
        Bundle args = new Bundle();
        args.putSerializable("stationOfRoute", (Serializable) stationOfRoute);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseRef = FirebaseDatabase.getInstance().getReference();
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

        //loadRouteStations(routeId);
        //BusStopAdapter = new BusStopAdapter(BusStopList,StringNumberExtractor(routename));
        BusStopAdapter = new BusStopAdapter(stationOfRoute);
        rV_BusStop.setAdapter(BusStopAdapter);

        BusStopAdapter.notifyDataSetChanged();

        if (BusStopList == null || BusStopList.isEmpty()) {
            Log.d("DEBUG", "Danh sách trạm rỗng!");
        } else {
            Log.d("DEBUG", "Danh sách trạm có " + BusStopList.size() + " phần tử.");
            for(station sta : BusStopList)
                Log.d("BUSSTOP",sta.getName().toString());
        }
        return view;
    }

//    public void loadRouteStations(String routeId) {
//        if (routeId == null) {
//            Log.e("BusRouteActivity", "Route ID is null.");
//        }
//
//        databaseRef.child("route").orderByChild("id").equalTo(routeId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot RouteSnapshot) {
//                        for (DataSnapshot snapshot : RouteSnapshot.getChildren()) {
//                            routename = snapshot.child("name").getValue(String.class);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.e("Firebase", "Failed to fetch bus stops", error.toException());
//                    }
//                });
//
//        Log.e("Routename", routename);
//    }


}
