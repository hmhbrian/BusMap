package com.example.busmap.Route.FindRoute;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.busmap.Route.RouteDetail.TimeTableFragment;
import com.example.busmap.entities.station;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutePagerFindAdapter extends FragmentStateAdapter {
    private Map<String, List<station>> stationOfRoute;

    public RoutePagerFindAdapter(@NonNull FragmentActivity fragmentActivity,Map<String, List<station>> stationOfRoute ) {
        super(fragmentActivity);
        this.stationOfRoute = stationOfRoute;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return DetailMoveFragment.newInstance(stationOfRoute);
            case 1:
                return BusStopPassFragment.newInstance(stationOfRoute);
            default:
                return DetailMoveFragment.newInstance(stationOfRoute);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
