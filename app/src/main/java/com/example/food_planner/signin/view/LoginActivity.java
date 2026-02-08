package com.example.food_planner.signin.view;

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
import com.example.food_planner.signin.presenter.LoginPresenter;
import com.example.food_planner.signin.presenter.LoginPresenterImpl;
import com.example.food_planner.signup.SignUpActivity;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements LoginView {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private View rootView;
    private LoginPresenter presenter;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupGoogleClient();

        presenter = new LoginPresenterImpl(this, new UserRepository(this), new SharedPrefManager(this));
        presenter.checkSession();

        setupListeners();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
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
        btnLogin.setOnClickListener(v -> presenter.doLogin(
                Objects.requireNonNull(etEmail.getText()).toString().trim(),
                Objects.requireNonNull(etPassword.getText()).toString().trim()
        ));
        findViewById(R.id.btnGuest).setOnClickListener(v -> presenter.doGuestLogin());
        findViewById(R.id.btnGoogle).setOnClickListener(v -> presenter.onGoogleSignInClick(mGoogleSignInClient));
        findViewById(R.id.tvGoToSignUp).setOnClickListener(v -> navigateToSignUp());
    }

    @Override
    public void showLoading() { btnLogin.setEnabled(false); }

    @Override
    public void hideLoading() { btnLogin.setEnabled(true); }

    @Override
    public void showSuccess(String message) { SnackbarUtil.showSuccess(rootView, message); }

    @Override
    public void showError(String message) { SnackbarUtil.showError(rootView, message); }

    @Override
    public void navigateToHome() {
        startActivity(new Intent(this, HomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    public void navigateToSignUp() { startActivity(new Intent(this, SignUpActivity.class)); }

    @Override
    public void launchGoogleSignIn(Intent signInIntent) { googleSignInLauncher.launch(signInIntent); }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}