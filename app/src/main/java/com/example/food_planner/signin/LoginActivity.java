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
import com.example.food_planner.repository.UserRepository;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.ViewUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private View rootView;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. CHECK SESSION BEFORE LOADING VIEW ---
        SharedPrefManager prefManager = new SharedPrefManager(this);
        if (prefManager.isLoggedIn()) {
            navigateToHome();
            return; // Stop loading this activity
        }

        setContentView(R.layout.activity_login);

        // Initialize Repository
        userRepository = new UserRepository(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        ImageButton btnGuest = findViewById(R.id.btnGuest);
        rootView = findViewById(android.R.id.content);

        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String emailInput = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String passwordInput = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (validateLogin(emailInput, passwordInput)) {
                // Call Firebase/Room via Repository
                userRepository.login(emailInput, passwordInput, new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        ViewUtils.showSuccess(rootView, getString(R.string.login_successful_welcome_back));
                        rootView.postDelayed(() -> navigateToHome(), 1000);
                    }

                    @Override
                    public void onError(String message) {
                        ViewUtils.showError(rootView, "Login Failed: " + message);
                    }
                });
            }
        });

        btnGuest.setOnClickListener(v -> {
            ViewUtils.showSuccess(rootView, getString(R.string.entering_as_guest));
            rootView.postDelayed(this::navigateToHome, 500);
        });
    }

    private boolean validateLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            ViewUtils.showError(rootView, getString(R.string.please_fill_in_all_fields));
            return false;
        }
        return true;
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}