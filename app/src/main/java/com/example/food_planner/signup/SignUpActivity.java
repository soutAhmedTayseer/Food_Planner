package com.example.food_planner.signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_planner.R;
import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.homescreen.HomeActivity;
import com.example.food_planner.signin.LoginActivity;
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

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPass;
    private View rootView;
    private UserRepository userRepository;

    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Repository
        userRepository = new UserRepository(this);
        rootView = findViewById(android.R.id.content);

        // --- GOOGLE SIGN-IN SETUP ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            SnackbarUtil.showError(rootView, "Google sign up failed: " + e.getMessage());
                        }
                    }
                }
        );

        // Initialize Views
        etUsername = findViewById(R.id.etUsernameSign); // New Field
        etEmail = findViewById(R.id.etEmailSign);
        etPassword = findViewById(R.id.etPasswordSign);
        etConfirmPass = findViewById(R.id.etConfirmPass);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        ImageButton btnGoogle = findViewById(R.id.btnGoogle);
        ImageButton btnGuest = findViewById(R.id.btnGuest);

        // --- LISTENERS ---

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        }

        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> {
                SharedPrefManager prefManager = new SharedPrefManager(this);
                prefManager.saveGuestSession();

                SnackbarUtil.showSuccess(rootView, getString(R.string.entering_as_guest));
                rootView.postDelayed(this::navigateToHome, 500);
            });
        }

        btnSignUp.setOnClickListener(v -> {
            String username = Objects.requireNonNull(etUsername.getText()).toString().trim();
            String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
            String confirmPass = Objects.requireNonNull(etConfirmPass.getText()).toString().trim();

            if (validateSignUp(username, email, password, confirmPass)) {
                // IMPORTANT: Ensure UserRepository.signUp accepts 'username' as the first argument
                userRepository.signUp(username, email, password, new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        SnackbarUtil.showSuccess(rootView, getString(R.string.account_created_successfully));
                        rootView.postDelayed(() -> navigateToHome(), 1000);
                    }

                    @Override
                    public void onError(String message) {
                        SnackbarUtil.showError(rootView, "Error: " + message);
                    }
                });
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        userRepository.firebaseAuthWithGoogle(idToken, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                SnackbarUtil.showSuccess(rootView, getString(R.string.account_created_successfully));
                rootView.postDelayed(() -> navigateToHome(), 1000);
            }

            @Override
            public void onError(String message) {
                SnackbarUtil.showError(rootView, "Authentication Failed: " + message);
            }
        });
    }

    private boolean validateSignUp(String username, String email, String password, String confirmPass) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            SnackbarUtil.showError(rootView, getString(R.string.please_fill_in_all_fields));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            SnackbarUtil.showError(rootView, getString(R.string.please_enter_a_valid_email_address));
            return false;
        }
        if (password.length() < 6) {
            SnackbarUtil.showError(rootView, getString(R.string.password_must_be_at_least_6_characters));
            return false;
        }
        if (!password.equals(confirmPass)) {
            SnackbarUtil.showError(rootView, getString(R.string.passwords_do_not_match));
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