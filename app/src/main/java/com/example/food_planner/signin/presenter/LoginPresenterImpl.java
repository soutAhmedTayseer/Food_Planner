package com.example.food_planner.signin.presenter;

import android.content.Intent;
import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.signin.view.LoginView;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginPresenterImpl implements LoginPresenter {

    private final LoginView view;
    private final UserRepository userRepository;
    private final SharedPrefManager sharedPrefManager;

    public LoginPresenterImpl(LoginView view, UserRepository userRepository, SharedPrefManager sharedPrefManager) {
        this.view = view;
        this.userRepository = userRepository;
        this.sharedPrefManager = sharedPrefManager;
    }

    @Override
    public void checkSession() {
        if (sharedPrefManager.isLoggedIn()) {
            view.navigateToHome();
        }
    }

    @Override
    public void doLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showError("Please fill in all fields");
            return;
        }

        view.showLoading();
        userRepository.login(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                view.showSuccess("Login Successful!");
                view.navigateToHome();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError("Login Failed: " + message);
            }
        });
    }

    @Override
    public void doGuestLogin() {
        sharedPrefManager.saveGuestSession();
        view.showSuccess("Entering as Guest...");
        view.navigateToHome();
    }

    @Override
    public void onGoogleSignInClick(GoogleSignInClient client) {
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
            view.showError("Google sign in failed: " + e.getMessage());
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        view.showLoading();
        userRepository.firebaseAuthWithGoogle(idToken, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                view.showSuccess("Login Successful!");
                view.navigateToHome();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                view.showError("Authentication Failed: " + message);
            }
        });
    }

    @Override
    public void onDestroy() {
        // Cleanup if needed
    }
}