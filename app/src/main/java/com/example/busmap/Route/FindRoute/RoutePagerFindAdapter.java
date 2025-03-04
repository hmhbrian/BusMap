package com.example.busmap.Route.FindRoute;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.busmap.Route.RouteDetail.TimeTableFragment;
import com.example.busmap.entities.station;
import java.util.ArrayList;

public class RoutePagerFindAdapter extends FragmentStateAdapter {
    private ArrayList<station> StationList;
    private String routeId;

    public RoutePagerFindAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<station> StationList,String routeId) {
        super(fragmentActivity);
        this.StationList = StationList;
        this.routeId = routeId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return TimeTableFragment.newInstance(routeId);
            case 1:
                return BusStopPassFragment.newInstance(StationList,routeId);
            default:
                return TimeTableFragment.newInstance(routeId);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
