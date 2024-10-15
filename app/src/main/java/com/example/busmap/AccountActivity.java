package com.example.busmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // TextView for personal information
        TextView personalInfoTextView = findViewById(R.id.txtPersonalInfo);
        personalInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start new Activity after the ripple effect
                Intent intent = new Intent(AccountActivity.this, Login.class);
                startActivity(intent);
            }
        });

        // TextView for settings
        TextView settingsTextView = findViewById(R.id.txtSettings);
        settingsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, Login.class);
                startActivity(intent);
            }
        });

        // Add setOnClickListener for other TextViews in the same way
    }
}

