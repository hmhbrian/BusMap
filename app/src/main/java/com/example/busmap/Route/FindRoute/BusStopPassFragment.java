package com.example.busmap.Route.FindRoute;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class BusStopPassFragment extends Fragment {
    private RecyclerView rV_BusStop;
    private BusStopAdapter BusStopAdapter;
    private DatabaseReference databaseRef;
    private String routeId;
    private ArrayList<station> BusStopList = new ArrayList<>();

    public static BusStopPassFragment newInstance(ArrayList<station> StationList, String routeId) {
        BusStopPassFragment fragment = new BusStopPassFragment();
        Bundle args = new Bundle();
        args.putSerializable("BusStopList", StationList);
        args.putString("route_id", routeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeId = getArguments().getString("route_id");
            BusStopList = (ArrayList<station>)getArguments().getSerializable("BusStopList");
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

        BusStopAdapter = new BusStopAdapter(BusStopList,routeId);
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
}
