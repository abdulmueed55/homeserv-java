package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserv.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> next = new SessionManager(this).getUserId() > 0
                    ? DashboardActivity.class
                    : AuthActivity.class;
            startActivity(new Intent(this, next));
            finish();
        }, 2000);
    }
}
