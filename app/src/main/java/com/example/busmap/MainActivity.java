package com.example.busmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.busmap.Route.BusnearActivity;
import com.example.busmap.User.Login;
import com.example.busmap.User.ProfileActivity;
import com.example.busmap.Route.RouteListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra trạng thái đăng nhập
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Log.d("FirebaseAuth", "Người dùng vẫn đăng nhập: " + user.getEmail());
            } else {
                Log.d("FirebaseAuth", "Phiên đăng nhập đã hết hạn");
                startActivity(new Intent(MainActivity.this, Login.class));
            }
        }

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
                Logout();
                startActivity(new Intent(MainActivity.this, Login.class)); // Chuyển về màn hình đăng nhập
                finish();
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
            } else if (item.getItemId() == R.id.near_station) {
                Intent intent = new Intent (MainActivity.this, TestMap.class);
                startActivity(intent);
            }else if (item.getItemId() == R.id.route) {
                replaceFragment(new RouteListFragment());
            }else if (item.getItemId() == R.id.ìnfo) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("USER_EMAIL", getIntent().getStringExtra("USER_EMAIL")); // Truyền email
                startActivity(intent);
                return true;
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

    public void Logout(){
        FirebaseAuth.getInstance().signOut();
        // Xóa trạng thái đăng nhập
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();
    }
}
