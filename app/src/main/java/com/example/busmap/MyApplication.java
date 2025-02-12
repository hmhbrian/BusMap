package com.example.busmap;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();        // Bật chế độ offline cho Firebase Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
