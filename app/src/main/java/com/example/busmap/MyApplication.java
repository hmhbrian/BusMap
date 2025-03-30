package com.example.busmap;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();        // Bật chế độ offline cho Firebase Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
