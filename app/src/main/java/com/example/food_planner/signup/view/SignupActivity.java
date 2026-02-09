package com.example.food_planner.signup.view;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.food_planner.signin.view.LoginActivity;
import com.example.food_planner.signup.presenter.SignupPresenter;
import com.example.food_planner.signup.presenter.SignupPresenterImpl;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity implements SignupView {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPass;
    private Button btnSignUp;
    private View rootView;

    private SignupPresenter presenter;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setupGoogleClient();

        presenter = new SignupPresenterImpl(
                this,
                new UserRepository(this),
                new SharedPrefManager(this)
        );

        setupListeners();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        etUsername = findViewById(R.id.etUsernameSign);
        etEmail = findViewById(R.id.etEmailSign);
        etPassword = findViewById(R.id.etPasswordSign);
        etConfirmPass = findViewById(R.id.etConfirmPass);
        btnSignUp = findViewById(R.id.btnSignUp);
    }

    private void setupGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        presenter.handleGoogleResult(result.getData());
                    }
                }
        );
    }

    private void setupListeners() {
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        ImageButton btnGoogle = findViewById(R.id.btnGoogle);
        ImageButton btnGuest = findViewById(R.id.btnGuest);

        btnSignUp.setOnClickListener(v -> presenter.doSignUp(
                Objects.requireNonNull(etUsername.getText()).toString().trim(),
                Objects.requireNonNull(etEmail.getText()).toString().trim(),
                Objects.requireNonNull(etPassword.getText()).toString().trim(),
                Objects.requireNonNull(etConfirmPass.getText()).toString().trim()
        ));

        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> presenter.doGuestLogin());
        }

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> presenter.onGoogleSignInClick(mGoogleSignInClient));
        }

        tvGoToLogin.setOnClickListener(v -> navigateToLogin());
    }

    @Override
    public void showLoading() {
        btnSignUp.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        btnSignUp.setEnabled(true);
    }

    @Override
    public void showSuccess(String message) {
        SnackbarUtil.showSuccess(rootView, message);
    }

    @Override
    public void showError(String message) {
        SnackbarUtil.showError(rootView, message);
    }

    @Override
    public void navigateToHome() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1000);
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void launchGoogleSignIn(Intent signInIntent) {
        googleSignInLauncher.launch(signInIntent);
    }

    // --- NEW: Network Check Implementation ---
    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}