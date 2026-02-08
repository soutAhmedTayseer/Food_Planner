package com.example.food_planner.signin.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_planner.R;
import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.homescreen.view.HomeActivity;
import com.example.food_planner.signin.presenter.LoginPresenter;
import com.example.food_planner.signin.presenter.LoginPresenterImpl;
import com.example.food_planner.signup.view.SignupActivity;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginFragment extends Fragment implements LoginView {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private LoginPresenter presenter;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        presenter.handleGoogleResult(result.getData());
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        presenter = new LoginPresenterImpl(this, new UserRepository(requireContext()), new SharedPrefManager(requireContext()));
        presenter.checkSession();

        setupListeners(view);
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
    }

    private void setupListeners(View view) {
        btnLogin.setOnClickListener(v -> presenter.doLogin(
                Objects.requireNonNull(etEmail.getText()).toString().trim(),
                Objects.requireNonNull(etPassword.getText()).toString().trim()
        ));
        view.findViewById(R.id.btnGuest).setOnClickListener(v -> presenter.doGuestLogin());
        view.findViewById(R.id.btnGoogle).setOnClickListener(v -> presenter.onGoogleSignInClick(mGoogleSignInClient));
        view.findViewById(R.id.tvGoToSignUp).setOnClickListener(v -> navigateToSignUp());
    }

    @Override
    public void showLoading() { btnLogin.setEnabled(false); }

    @Override
    public void hideLoading() { btnLogin.setEnabled(true); }

    @Override
    public void showSuccess(String message) { if (getView() != null) SnackbarUtil.showSuccess(getView(), message); }

    @Override
    public void showError(String message) { if (getView() != null) SnackbarUtil.showError(getView(), message); }

    @Override
    public void navigateToHome() {
        if (getActivity() != null) {
            startActivity(new Intent(requireActivity(), HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            requireActivity().finish();
        }
    }

    @Override
    
    public void navigateToSignUp() { startActivity(new Intent(requireContext(), SignupActivity.class)); }

    @Override
    public void launchGoogleSignIn(Intent signInIntent) { googleSignInLauncher.launch(signInIntent); }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();
        super.onDestroyView();
    }
}