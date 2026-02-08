package com.example.food_planner.signup.presenter;

import android.content.Intent;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public interface SignupPresenter {
    void doSignUp(String username, String email, String password, String confirmPass);
    void doGuestLogin();
    void onGoogleSignInClick(GoogleSignInClient client);
    void handleGoogleResult(Intent data);
    void onDestroy();
}