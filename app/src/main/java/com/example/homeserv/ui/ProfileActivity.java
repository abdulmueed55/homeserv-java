package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserv.R;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SessionManager session = new SessionManager(this);
        User user = new DBHelper(this).getUserById(session.getUserId());
        if (user == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tvProfileName)).setText(user.name);
        ((TextView) findViewById(R.id.tvProfilePhone)).setText(getString(R.string.phone_format, user.phone));
        ((TextView) findViewById(R.id.tvProfileRole)).setText(getString(R.string.role_format, user.role));

        ((MaterialButton) findViewById(R.id.btnLogout)).setOnClickListener(v -> {
            session.clear();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        BaseNav.setupBottomNavigation(this, R.id.navProfile);
    }
}
