package com.example.food_planner.signin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.R;
import com.example.food_planner.signup.SignUpActivity;
import com.example.food_planner.homescreen.HomeActivity;
import com.example.food_planner.utils.ViewUtils; // Import your new helper
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private View rootView; // Needed for Snackbar

    // Dummy Credentials for Testing
    private static final String DUMMY_EMAIL = "admin";
    private static final String DUMMY_PASSWORD = "123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        ImageButton btnGuest = findViewById(R.id.btnGuest);

        // Grab the root view for showing Snackbars
        rootView = findViewById(android.R.id.content);

        // 1. Navigation to Sign Up
        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // 2. Login Logic (Dummy Data)
        btnLogin.setOnClickListener(v -> {
            String emailInput = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String passwordInput = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (validateLogin(emailInput, passwordInput)) {
                // SUCCESS SNACKBAR
                ViewUtils.showSuccess(rootView, getString(R.string.login_successful_welcome_back));

                // Delay navigation slightly so user sees the message
                rootView.postDelayed(this::navigateToHome, 1000);
            }
        });

        // 3. Guest Logic
        btnGuest.setOnClickListener(v -> {
            ViewUtils.showSuccess(rootView, getString(R.string.entering_as_guest));
            // TODO: Save 'isGuest = true' in SharedPrefs
            rootView.postDelayed(this::navigateToHome, 500);
        });
    }

    // Helper method to validate inputs
    private boolean validateLogin(String email, String password) {
        // Check for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            // ERROR SNACKBAR
            ViewUtils.showError(rootView, getString(R.string.please_fill_in_all_fields));
            return false;
        }

        // Check against dummy credentials
        if (email.equals(DUMMY_EMAIL) && password.equals(DUMMY_PASSWORD)) {
            return true;
        } else {
            // ERROR SNACKBAR
            ViewUtils.showError(rootView, getString(R.string.invalid_email_or_password));
            return false;
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}