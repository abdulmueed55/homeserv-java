package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserv.R;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;
import com.example.homeserv.db.DBHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;

public class AddProviderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_provider);

        DBHelper db = new DBHelper(this);
        User user = db.getUserById(new SessionManager(this).getUserId());
        if (user == null || !Roles.ADMIN.equals(user.role)) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        Spinner spinner = findViewById(R.id.spinnerServiceType);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Plumber", "Electrician", "Cleaner", "Carpenter", "Painter")));

        ((MaterialButton) findViewById(R.id.btnRegisterProvider)).setOnClickListener(v -> {
            TextInputEditText etName = findViewById(R.id.etProviderName);
            TextInputEditText etPhone = findViewById(R.id.etProviderPhone);
            TextInputEditText etPassword = findViewById(R.id.etProviderPassword);

            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }
            for (char ch : name.toCharArray()) {
                if (!Character.isLetter(ch) && !Character.isWhitespace(ch)) {
                    Toast.makeText(this, "Name can only contain letters.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            long result = db.addProvider(name, phone, spinner.getSelectedItem().toString(), password);
            if (result > 0) {
                Toast.makeText(this, R.string.provider_added, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Phone already exists.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
