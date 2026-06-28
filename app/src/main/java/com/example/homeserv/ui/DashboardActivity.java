package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserv.R;
import com.example.homeserv.adapters.OfferAdapter;
import com.example.homeserv.data.DashboardCounts;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;
import com.google.android.material.button.MaterialButton;

public class DashboardActivity extends AppCompatActivity {

    private DBHelper db;
    private OfferAdapter adapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        db = new DBHelper(this);
        user = db.getUserById(new SessionManager(this).getUserId());
        if (user == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvGreeting)).setText(getString(R.string.hello_user, user.name));

        boolean canBook = Roles.CUSTOMER.equals(user.role) || Roles.ADMIN.equals(user.role);
        adapter = new OfferAdapter(null, canBook, offer -> {
            Intent intent = new Intent(this, BookingConfirmationActivity.class);
            intent.putExtra(BookingConfirmationActivity.EXTRA_OFFER_ID, offer.id);
            startActivity(intent);
        });

        RecyclerView rv = findViewById(R.id.rvOffers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setNestedScrollingEnabled(false);
        rv.setHasFixedSize(false);
        rv.setAdapter(adapter);

        boolean isAdmin = Roles.ADMIN.equals(user.role);

        MaterialButton btnAddProvider = findViewById(R.id.btnAddProvider);
        MaterialButton btnAdminBookings = findViewById(R.id.btnAdminBookings);
        MaterialButton btnManageUsers = findViewById(R.id.btnManageUsers);

        btnAddProvider.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnAdminBookings.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnManageUsers.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        btnAddProvider.setOnClickListener(v -> startActivity(new Intent(this, AddProviderActivity.class)));
        btnAdminBookings.setOnClickListener(v -> startActivity(new Intent(this, MyBookingsActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUsersActivity.class)));

        BaseNav.setupBottomNavigation(this, R.id.navHome);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) load();
    }

    private void load() {
        DashboardCounts counts = db.getDashboardCounts();
        ((TextView) findViewById(R.id.tvTotalOffers)).setText(String.valueOf(counts.totalOffers));
        ((TextView) findViewById(R.id.tvTotalProviders)).setText(String.valueOf(counts.totalProviders));
        ((TextView) findViewById(R.id.tvTotalBookings)).setText(String.valueOf(counts.totalBookings));
        adapter.updateData(db.getOffers());
    }
}
