package com.example.busmap.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmap.Login;
import com.example.busmap.R;

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

        // Textview for local
        TextView localsTextview = findViewById (R.id.txtChangeArea);
        localsTextview.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent (AccountActivity.this, Login.class);
                startActivity (intent);
            }
        });
        // Textview for Update
        TextView UpdateTextview = findViewById (R.id.txtUpdateData);
        UpdateTextview.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (AccountActivity.this, Login.class);
                startActivity (intent);
            }
        });
        // Textview for Rating
        TextView RatingTextview = findViewById (R.id.txtRateApp);
        RatingTextview.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (AccountActivity.this, RatingActivity.class);
                startActivity (intent);
            }
        });
        // Textview for infocompany
        TextView infoTextview = findViewById (R.id.txtCompanyInfo);
        infoTextview.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (AccountActivity.this, infocompany.class);
                startActivity (intent);
            }
        });
    }
}

