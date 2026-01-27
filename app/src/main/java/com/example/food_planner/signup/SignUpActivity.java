package com.example.food_planner.signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.R;
import com.example.food_planner.homescreen.HomeActivity;
import com.example.food_planner.utils.ViewUtils; // Import your global Snackbar helper
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPass;
    private View rootView; // Needed for Snackbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Views
        etEmail = findViewById(R.id.etEmailSign);
        etPassword = findViewById(R.id.etPasswordSign);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Grab the root view for showing Snackbars
        rootView = findViewById(android.R.id.content);

        // 1. Sign Up Logic
        btnSignUp.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
            String confirmPass = Objects.requireNonNull(etConfirmPass.getText()).toString().trim();

            if (validateSignUp(email, password, confirmPass)) {
                // SUCCESS SNACKBAR
                ViewUtils.showSuccess(rootView, getString(R.string.account_created_successfully));

                // TODO: Save user to Database here later

                // Delay navigation slightly so user sees the success message
                rootView.postDelayed(this::navigateToHome, 1000);
            }
        });

        // 2. Navigation to Login
        tvGoToLogin.setOnClickListener(v -> {
            // Just finish() to return to the previous Login Activity
            finish();
        });
    }

    // Helper method to validate inputs
    private boolean validateSignUp(String email, String password, String confirmPass) {
        // Check for empty fields
        if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            ViewUtils.showError(rootView, getString(R.string.please_fill_in_all_fields));
            return false;
        }

        // Check for valid email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ViewUtils.showError(rootView, getString(R.string.please_enter_a_valid_email_address));
            return false;
        }

        // Check password length (Optional but good practice)
        if (password.length() < 6) {
            ViewUtils.showError(rootView, getString(R.string.password_must_be_at_least_6_characters));
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPass)) {
            ViewUtils.showError(rootView, getString(R.string.passwords_do_not_match));
            return false;
        }

        return true;
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        // Clear back stack so user can't go back to Sign Up
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}