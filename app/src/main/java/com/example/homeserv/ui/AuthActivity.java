package com.example.homeserv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homeserv.R;
import com.example.homeserv.data.Roles;
import com.example.homeserv.db.DBHelper;
import com.example.homeserv.data.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class AuthActivity extends AppCompatActivity {

    private DBHelper db;
    private boolean isRegister = false;

    private TextInputLayout nameLayout;
    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private Spinner roleSpinner;
    private TextView serviceTypeLabel;
    private Spinner serviceTypeSpinner;
    private MaterialButton actionButton;
    private TextView title;
    private TextView toggle;

    private final List<String> roles = Arrays.asList(Roles.CUSTOMER, Roles.PROVIDER);
    private final List<String> serviceTypes = Arrays.asList("Plumber", "Electrician", "Cleaner", "Carpenter", "Painter");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        db = new DBHelper(this);

        title = findViewById(R.id.tvAuthTitle);
        nameLayout = findViewById(R.id.layoutName);
        nameInput = findViewById(R.id.etName);
        phoneInput = findViewById(R.id.etPhone);
        passwordLayout = findViewById(R.id.layoutPassword);
        passwordInput = findViewById(R.id.etPassword);
        roleSpinner = findViewById(R.id.spinnerRole);
        serviceTypeLabel = findViewById(R.id.tvServiceTypeLabel);
        serviceTypeSpinner = findViewById(R.id.spinnerServiceType);
        actionButton = findViewById(R.id.btnAuthAction);
        toggle = findViewById(R.id.tvToggleAuth);

        roleSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles));
        serviceTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, serviceTypes));

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateServiceTypeVisibility();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateServiceTypeVisibility();
            }
        });

        updateMode();

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordHelper(s != null ? s.toString() : "");
            }
        });

        actionButton.setOnClickListener(v -> {
            if (isRegister) register();
            else login();
        });

        toggle.setOnClickListener(v -> {
            isRegister = !isRegister;
            updateMode();
        });
    }

    private void updateMode() {
        title.setText(isRegister ? getString(R.string.create_account) : getString(R.string.login_to_homeserv));
        nameLayout.setVisibility(isRegister ? View.VISIBLE : View.GONE);
        roleSpinner.setVisibility(isRegister ? View.VISIBLE : View.GONE);
        actionButton.setText(isRegister ? getString(R.string.register) : getString(R.string.login));
        toggle.setText(isRegister ? getString(R.string.already_account) : getString(R.string.no_account));
        passwordLayout.setError(null);
        passwordInput.setText("");
        updatePasswordHelper("");
        updateServiceTypeVisibility();
    }

    private void updatePasswordHelper(String password) {
        if (!isRegister) {
            passwordLayout.setHelperText("Enter your password");
            passwordLayout.setError(null);
            return;
        }
        String helperText;
        if (password.isBlank()) {
            helperText = "Password: 8-20 chars, uppercase, lowercase, number, special char, no spaces";
        } else if (validateStrongPassword(password) == null) {
            helperText = "Strong password ✅";
        } else {
            helperText = "Weak password — follow the required password rules";
        }
        passwordLayout.setHelperText(helperText);
        if (!password.isBlank()) passwordLayout.setError(null);
    }

    private void updateServiceTypeVisibility() {
        boolean showServiceType = isRegister &&
                Roles.PROVIDER.equals(roleSpinner.getSelectedItem() != null ? roleSpinner.getSelectedItem().toString() : "");
        serviceTypeLabel.setVisibility(showServiceType ? View.VISIBLE : View.GONE);
        serviceTypeSpinner.setVisibility(showServiceType ? View.VISIBLE : View.GONE);
    }

    private void register() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        String pass = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            toast(getString(R.string.fill_all_fields));
            return;
        }
        for (char ch : name.toCharArray()) {
            if (!Character.isLetter(ch) && !Character.isWhitespace(ch)) {
                toast("Name can only contain letters.");
                return;
            }
        }
        String passwordError = validateStrongPassword(pass);
        if (passwordError != null) {
            passwordLayout.setError(passwordError);
            passwordInput.requestFocus();
            return;
        }
        String role = roleSpinner.getSelectedItem().toString();
        String serviceType = Roles.PROVIDER.equals(role)
                ? serviceTypeSpinner.getSelectedItem().toString()
                : null;

        long id = db.registerUser(name, phone, pass, role, serviceType);
        if (id > 0) {
            toast(getString(R.string.register_success));
            isRegister = false;
            updateMode();
            nameInput.setText("");
            passwordInput.setText("");
        } else {
            toast(getString(R.string.user_exists));
        }
    }

    private String validateStrongPassword(String password) {
        if (password.length() < 8) return "Password must be at least 8 characters.";
        if (password.length() > 20) return "Password must be maximum 20 characters.";
        for (char ch : password.toCharArray()) {
            if (Character.isWhitespace(ch)) return "Password cannot contain spaces.";
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            if (Character.isLowerCase(ch)) hasLower = true;
            if (Character.isDigit(ch)) hasDigit = true;
            if (!Character.isLetterOrDigit(ch)) hasSpecial = true;
        }
        if (!hasUpper) return "Add at least 1 uppercase letter.";
        if (!hasLower) return "Add at least 1 lowercase letter.";
        if (!hasDigit) return "Add at least 1 number.";
        if (!hasSpecial) return "Add at least 1 special character.";
        return null;
    }

    private void login() {
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        String pass = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        if (phone.isEmpty() || pass.isEmpty()) {
            toast(getString(R.string.fill_all_fields));
            return;
        }
        User user = db.loginUser(phone, pass);
        if (user != null) {
            new SessionManager(this).saveUser(user);
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            toast(getString(R.string.invalid_login));
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
