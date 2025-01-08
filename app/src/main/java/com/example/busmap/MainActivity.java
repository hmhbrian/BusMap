package com.example.busmap;

import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.busmap.Route.RouteListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Gán view cho DrawerLayout, Toolbar và BottomNavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load fragment đầu tiên nếu chưa có
        if (savedInstanceState == null) {
            //Hiện Map đầu tiên
            replaceFragment(new MapsFragment());
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
        //Thay thế Fragment theo lựa chọn của NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                replaceFragment(new MapsFragment());
            }else if(item.getItemId() == R.id.nav_share){
                replaceFragment(new ShareFragment());
            }else if(item.getItemId() == R.id.nav_about){
                replaceFragment(new AboutFragment());
            }else if(item.getItemId() == R.id.nav_logout){
                replaceFragment(new MapsFragment());
            }else if(item.getItemId() == R.id.nav_home){
                replaceFragment(new MapsFragment());
            }
            return true;
        });

        // Thay thế Fragment theo lựa chọn của BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.home)
            {
                replaceFragment(new MapsFragment());
            } else if (item.getItemId() == R.id.shorts) {
                replaceFragment(new MapsFragment());
            }else if (item.getItemId() == R.id.route) {
                replaceFragment(new RouteListFragment());
            }else if (item.getItemId() == R.id.library) {
                replaceFragment(new MapsFragment());
            }
            return true;
        });
    }

    // Hàm thay thế Fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_layout, fragment);
        fragmentTransaction.commit();
    }
}
