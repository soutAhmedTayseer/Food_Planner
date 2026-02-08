package com.example.food_planner.signin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.R;
import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.homescreen.view.HomeActivity;
import com.example.food_planner.signup.SignUpActivity;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private View rootView;
    private UserRepository userRepository;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. CHECK SESSION BEFORE LOADING VIEW ---
        SharedPrefManager sessionManager = new SharedPrefManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            return; // Stop loading this activity
        }

        setContentView(R.layout.activity_login);

        // Initialize Repository
        userRepository = new UserRepository(this);
        rootView = findViewById(android.R.id.content);

        // --- GOOGLE SIGN-IN SETUP ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Register the Activity Result Launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            SnackbarUtil.showError(rootView, "Google sign in failed: " + e.getMessage());
                        }
                    }
                }
        );

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
        ImageButton btnGuest = findViewById(R.id.btnGuest);
        ImageButton btnGoogle = findViewById(R.id.btnGoogle);

        // --- LISTENERS ---

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String emailInput = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String passwordInput = Objects.requireNonNull(etPassword.getText()).toString().trim();

            if (validateLogin(emailInput, passwordInput)) {
                userRepository.login(emailInput, passwordInput, new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        SnackbarUtil.showSuccess(rootView, getString(R.string.login_successful_welcome_back));
                        rootView.postDelayed(() -> navigateToHome(), 1000);
                    }

                    @Override
                    public void onError(String message) {
                        SnackbarUtil.showError(rootView, "Login Failed: " + message);
                    }
                });
            }
        });

        btnGuest.setOnClickListener(v -> {
            SharedPrefManager guestManager = new SharedPrefManager(this);
            guestManager.saveGuestSession();

            SnackbarUtil.showSuccess(rootView, getString(R.string.entering_as_guest));
            rootView.postDelayed(this::navigateToHome, 500);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        userRepository.firebaseAuthWithGoogle(idToken, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                SnackbarUtil.showSuccess(rootView, getString(R.string.login_successful_welcome_back));
                rootView.postDelayed(() -> navigateToHome(), 1000);
            }

            @Override
            public void onError(String message) {
                SnackbarUtil.showError(rootView, "Authentication Failed: " + message);
            }
        });
    }

    private boolean validateLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            SnackbarUtil.showError(rootView, getString(R.string.please_fill_in_all_fields));
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