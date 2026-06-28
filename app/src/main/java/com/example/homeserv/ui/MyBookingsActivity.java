package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserv.R;
import com.example.homeserv.adapters.BookingAdapter;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;

import java.util.List;

import com.example.homeserv.data.BookingItem;

public class MyBookingsActivity extends AppCompatActivity {

    private DBHelper db;
    private BookingAdapter adapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);
        db = new DBHelper(this);
        user = db.getUserById(new SessionManager(this).getUserId());
        if (user == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvBookingsTitle)).setText(
                Roles.ADMIN.equals(user.role) ? getString(R.string.all_bookings) : getString(R.string.my_bookings));

        boolean canComplete = Roles.PROVIDER.equals(user.role) || Roles.ADMIN.equals(user.role);
        adapter = new BookingAdapter(null, canComplete, booking -> {
            db.markBookingCompleted(booking.id);
            Toast.makeText(this, R.string.booking_completed, Toast.LENGTH_SHORT).show();
            load();
        });

        RecyclerView rv = findViewById(R.id.rvBookings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        BaseNav.setupBottomNavigation(this, R.id.navBookings);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) load();
    }

    private void load() {
        List<BookingItem> bookings = db.getBookingsForUser(user);
        findViewById(R.id.tvBookingsEmpty).setVisibility(bookings.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateData(bookings);
    }
}
