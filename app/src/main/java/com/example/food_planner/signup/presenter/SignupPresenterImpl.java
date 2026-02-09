package com.example.food_planner.signup.presenter;

import android.content.Intent;
import android.util.Patterns;

import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.signup.view.SignupView;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignupPresenterImpl implements SignupPresenter {

    private final SignupView view;
    private final UserRepository userRepository;
    private final SharedPrefManager sharedPrefManager;

    public SignupPresenterImpl(SignupView view, UserRepository userRepository, SharedPrefManager sharedPrefManager) {
        this.view = view;
        this.userRepository = userRepository;
        this.sharedPrefManager = sharedPrefManager;
    }

    @Override
    public void doSignUp(String username, String email, String password, String confirmPass) {
        if (!validateSignUp(username, email, password, confirmPass)) {
            return;
        }

        if (!view.isNetworkAvailable()) {
            view.showError("No internet connection");
            return;
        }

        view.showLoading();
        userRepository.signUp(username, email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                view.showSuccess("Account created successfully");
                view.navigateToHome();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError("Error: " + message);
            }
        });
    }

    @Override
    public void doGuestLogin() {
        // FIX: Prevent Guest Login if no internet to avoid HomeActivity crash
        if (!view.isNetworkAvailable()) {
            view.showError("Internet connection required for Guest Mode");
            return;
        }

        try {
            sharedPrefManager.saveGuestSession();
            view.showSuccess("Entering as Guest...");
            view.navigateToHome();
        } catch (Exception e) {
            view.showError("Error entering as guest");
            e.printStackTrace();
        }
    }

    @Override
    public void onGoogleSignInClick(GoogleSignInClient client) {
        if (!view.isNetworkAvailable()) {
            view.showError("No internet connection");
            return;
        }
        if (client != null) {
            view.launchGoogleSignIn(client.getSignInIntent());
        }
    }

    @Override
    public void handleGoogleResult(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            view.showError("Google sign up failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (!view.isNetworkAvailable()) {
            view.showError("No internet connection");
            return;
        }

        view.showLoading();
        userRepository.firebaseAuthWithGoogle(idToken, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                view.showSuccess("Account created successfully");
                view.navigateToHome();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError("Authentication Failed: " + message);
            }
        });
    }

    private boolean validateSignUp(String username, String email, String password, String confirmPass) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            view.showError("Please fill in all fields");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Please enter a valid email address");
            return false;
        }
        if (password.length() < 6) {
            view.showError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPass)) {
            view.showError("Passwords do not match");
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
    }
}