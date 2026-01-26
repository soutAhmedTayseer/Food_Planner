package com.example.food_planner.signup;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.R;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.homescreen.HomeActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPass;
    private Button btnSignUp;
    private TextView tvGoToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmailSign);
        etPassword = findViewById(R.id.etPasswordSign);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnSignUp.setOnClickListener(v -> {
            // Placeholder for MVP Registration Logic
            navigateToHome();
        });

        tvGoToLogin.setOnClickListener(v -> {
            finish(); // Go back to Login
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}