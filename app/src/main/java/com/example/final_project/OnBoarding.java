package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class OnBoarding extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(OnBoarding.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}