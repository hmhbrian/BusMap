package com.example.busmap.Favorites;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.Arrays;
import java.util.List;

public class FavoritePagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments = Arrays.asList(new FavoriteRoutesFragment(), new FavoriteStationsFragment());

    public FavoritePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
