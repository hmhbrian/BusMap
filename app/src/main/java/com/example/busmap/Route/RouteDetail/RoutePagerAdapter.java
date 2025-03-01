package com.example.busmap.Route.RouteDetail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RoutePagerAdapter extends FragmentStateAdapter {
    private String routeId; // Thêm biến routeId

    public RoutePagerAdapter(@NonNull FragmentActivity fragmentActivity, String routeId) {
        super(fragmentActivity);
        this.routeId = routeId; // Gán routeId
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return TimeTableFragment.newInstance(routeId); // Truyền routeId vào Fragment
            case 1:
                return StationListFragment.newInstance(routeId); // Truyền routeId vào Fragment
            case 2:
                return RouteInfoFragment.newInstance(routeId);
            case 3:
                return RouteRatingFragment.newInstance(routeId);
            default:
                return TimeTableFragment.newInstance(routeId);
        }
    }

    @Override
    public int getItemCount() {
        return 4; // 3 tab: Biểu đồ giờ, Trạm dừng, Thông tin
    }
}
