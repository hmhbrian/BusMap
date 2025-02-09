package com.example.busmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.busmap.entities.user;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class UserManager {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String USER_KEY = "UserData";

    public static void fetchAndSaveUser(Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("User").child(userId);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    user user = snapshot.getValue(user.class);
                    saveUserToSharedPreferences(context, user); // Lưu vào SharedPreferences
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FirebaseData", "Lỗi khi lấy dữ liệu: " + error.getMessage());
            }
        });
    }
    public static void saveUserToSharedPreferences(Context context, user user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString(USER_KEY, userJson);
        editor.apply();
    }

    public static user getUserFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = sharedPreferences.getString(USER_KEY, null);
        return gson.fromJson(userJson, user.class);
    }
}
