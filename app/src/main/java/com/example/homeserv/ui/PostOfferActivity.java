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
import com.example.homeserv.data.Provider;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class PostOfferActivity extends AppCompatActivity {

    private DBHelper db;
    private User user;
    private List<Provider> providers;
    private Spinner providerSpinner;
    private MaterialButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_offer);
        db = new DBHelper(this);
        user = db.getUserById(new SessionManager(this).getUserId());
        if (user == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        providerSpinner = findViewById(R.id.spinnerProvider);
        submitButton = findViewById(R.id.btnSubmitOffer);

        if (!Roles.PROVIDER.equals(user.role) && !Roles.ADMIN.equals(user.role)) {
            ((TextView) findViewById(R.id.tvPostOfferNote)).setText(getString(R.string.provider_only_note));
            submitButton.setEnabled(false);
        }

        findViewById(R.id.btnGoAddProvider).setOnClickListener(v ->
                startActivity(new Intent(this, AddProviderActivity.class)));

        submitButton.setOnClickListener(v -> submitOffer());

        BaseNav.setupBottomNavigation(this, R.id.navPostOffer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProviders();
    }

    private void loadProviders() {
        providers = db.getProvidersForUser(user.id, user.role);
        providerSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, providers));
        findViewById(R.id.tvProviderEmpty).setVisibility(providers.isEmpty() ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!providers.isEmpty() &&
                (Roles.PROVIDER.equals(user.role) || Roles.ADMIN.equals(user.role)));
    }

    private void submitOffer() {
        Provider provider = (Provider) providerSpinner.getSelectedItem();
        TextInputEditText etTitle = findViewById(R.id.etOfferTitle);
        TextInputEditText etDesc = findViewById(R.id.etOfferDescription);
        TextInputEditText etPrice = findViewById(R.id.etOfferPrice);
        TextInputEditText etDuration = findViewById(R.id.etOfferDuration);

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString() : "";
        String duration = etDuration.getText() != null ? etDuration.getText().toString().trim() : "";

        Double price = null;
        try { price = Double.parseDouble(priceStr); } catch (NumberFormatException ignored) {}

        if (provider == null || title.isEmpty() || desc.isEmpty() || price == null || duration.isEmpty()) {
            toast(getString(R.string.fill_all_fields));
            return;
        }
        if (db.addOffer(provider.id, title, desc, price, duration) > 0) {
            toast(getString(R.string.offer_posted));
            finish();
        } else {
            toast(getString(R.string.something_wrong));
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
