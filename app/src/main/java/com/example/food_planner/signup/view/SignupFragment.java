package com.example.food_planner.signup.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class SignupFragment extends Fragment implements SignupView {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPass;
    private Button btnSignUp;

    private SignupPresenter presenter;
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
        return inflater.inflate(R.layout.activity_signup, container, false); // Reusing existing layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        // Initialize Presenter
        presenter = new SignupPresenterImpl(
                this,
                new UserRepository(requireContext()),
                new SharedPrefManager(requireContext())
        );

        setupListeners(view);
    }

    private void initViews(View view) {
        etUsername = view.findViewById(R.id.etUsernameSign);
        etEmail = view.findViewById(R.id.etEmailSign);
        etPassword = view.findViewById(R.id.etPasswordSign);
        etConfirmPass = view.findViewById(R.id.etConfirmPass);
        btnSignUp = view.findViewById(R.id.btnSignUp);
    }

    private void setupListeners(View view) {
        btnSignUp.setOnClickListener(v -> presenter.doSignUp(
                Objects.requireNonNull(etUsername.getText()).toString().trim(),
                Objects.requireNonNull(etEmail.getText()).toString().trim(),
                Objects.requireNonNull(etPassword.getText()).toString().trim(),
                Objects.requireNonNull(etConfirmPass.getText()).toString().trim()
        ));

        View btnGuest = view.findViewById(R.id.btnGuest);
        if (btnGuest != null) {
            btnGuest.setOnClickListener(v -> presenter.doGuestLogin());
        }

        View btnGoogle = view.findViewById(R.id.btnGoogle);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> presenter.onGoogleSignInClick(mGoogleSignInClient));
        }

        View tvGoToLogin = view.findViewById(R.id.tvGoToLogin);
        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> navigateToLogin());
        }
    }

    // --- View Interface Implementation ---

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
        if (getView() != null) {
            SnackbarUtil.showSuccess(getView(), message);
        }
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarUtil.showError(getView(), message);
        }
    }

    @Override
    public void navigateToHome() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                Intent intent = new Intent(requireActivity(), HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        }, 1000);
    }

    @Override
    public void navigateToLogin() {
        // If using Navigation Component, prefer: Navigation.findNavController(getView()).navigate(...)
        // Otherwise, standard Intent:
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    @Override
    public void launchGoogleSignIn(Intent signInIntent) {
        googleSignInLauncher.launch(signInIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}