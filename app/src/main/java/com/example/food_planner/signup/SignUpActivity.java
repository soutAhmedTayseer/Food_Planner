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
import com.example.food_planner.repository.UserRepository;
import com.example.food_planner.utils.ViewUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPass;
    private View rootView;
    private UserRepository userRepository; // Use the Repo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Repository
        userRepository = new UserRepository(this);

        etEmail = findViewById(R.id.etEmailSign);
        etPassword = findViewById(R.id.etPasswordSign);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        rootView = findViewById(android.R.id.content);

        btnSignUp.setOnClickListener(v -> {
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
            String confirmPass = Objects.requireNonNull(etConfirmPass.getText()).toString().trim();

            if (validateSignUp(email, password, confirmPass)) {
                // Show loading state here if you want

                // Call Firebase/Room via Repository
                userRepository.signUp(email, password, new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        ViewUtils.showSuccess(rootView, getString(R.string.account_created_successfully));
                        rootView.postDelayed(() -> navigateToHome(), 1000);
                    }

                    @Override
                    public void onError(String message) {
                        ViewUtils.showError(rootView, "Error: " + message);
                    }
                });
            }
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private boolean validateSignUp(String email, String password, String confirmPass) {
        if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            ViewUtils.showError(rootView, getString(R.string.please_fill_in_all_fields));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ViewUtils.showError(rootView, getString(R.string.please_enter_a_valid_email_address));
            return false;
        }
        if (password.length() < 6) {
            ViewUtils.showError(rootView, getString(R.string.password_must_be_at_least_6_characters));
            return false;
        }
        if (!password.equals(confirmPass)) {
            ViewUtils.showError(rootView, getString(R.string.passwords_do_not_match));
            return false;
        }
        return true;
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}