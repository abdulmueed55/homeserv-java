package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserv.R;
import com.example.homeserv.data.Offer;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.stream.Collectors;

public class BookingConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_OFFER_ID = "extra_offer_id";

    private DBHelper db;
    private Offer offer;
    private List<User> customers;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        db = new DBHelper(this);
        currentUser = db.getUserById(new SessionManager(this).getUserId());
        offer = db.getOfferById(getIntent().getIntExtra(EXTRA_OFFER_ID, -1));

        if (currentUser == null || offer == null) { finish(); return; }
        if (!Roles.CUSTOMER.equals(currentUser.role) && !Roles.ADMIN.equals(currentUser.role)) {
            Toast.makeText(this, R.string.only_customers_book, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvConfirmTitle)).setText(offer.title);
        ((TextView) findViewById(R.id.tvConfirmProvider)).setText(getString(R.string.provider_format, offer.providerName));
        ((TextView) findViewById(R.id.tvConfirmPrice)).setText(getString(R.string.price_format, offer.price));
        ((TextView) findViewById(R.id.tvConfirmDuration)).setText(getString(R.string.duration_format, offer.duration));

        Spinner spinner = findViewById(R.id.spinnerCustomer);
        TextView spinnerLabel = findViewById(R.id.tvCustomerLabel);

        if (Roles.ADMIN.equals(currentUser.role)) {
            customers = db.getCustomers();
            spinner.setVisibility(View.VISIBLE);
            if (spinnerLabel != null) spinnerLabel.setVisibility(View.VISIBLE);
            spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                    customers.stream().map(u -> u.name).collect(Collectors.toList())));
        } else {
            customers = List.of(currentUser);
            spinner.setVisibility(View.GONE);
            if (spinnerLabel != null) spinnerLabel.setVisibility(View.GONE);
        }

        ((MaterialButton) findViewById(R.id.btnConfirmBooking)).setOnClickListener(v -> confirm());
    }

    private void confirm() {
        User customer;
        if (Roles.ADMIN.equals(currentUser.role)) {
            Spinner spinner = findViewById(R.id.spinnerCustomer);
            int pos = spinner.getSelectedItemPosition();
            if (pos < 0 || pos >= customers.size()) {
                toast(getString(R.string.no_customer_found));
                return;
            }
            customer = customers.get(pos);
        } else {
            customer = currentUser;
        }
        TextInputEditText etNotes = findViewById(R.id.etBookingNotes);
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        if (db.addBooking(offer.id, customer.id, notes) > 0) {
            toast(getString(R.string.booking_confirmed));
            startActivity(new Intent(this, MyBookingsActivity.class));
            finish();
        } else {
            toast(getString(R.string.something_wrong));
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
