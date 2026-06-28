package com.example.homeserv.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserv.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseNav {

    public static void setupBottomNavigation(AppCompatActivity activity, int selectedItemId) {
        BottomNavigationView bottomNavigation = activity.findViewById(R.id.bottomNavigation);
        if (bottomNavigation == null) return;
        bottomNavigation.setSelectedItemId(selectedItemId);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;
            if (id == R.id.navHome) {
                activity.startActivity(new Intent(activity, DashboardActivity.class));
            } else if (id == R.id.navPostOffer) {
                activity.startActivity(new Intent(activity, PostOfferActivity.class));
            } else if (id == R.id.navBookings) {
                activity.startActivity(new Intent(activity, MyBookingsActivity.class));
            } else if (id == R.id.navProfile) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
            }
            return true;
        });
    }
}
