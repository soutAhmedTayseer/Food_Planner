package com.example.food_planner.signin.presenter;

import android.content.Intent;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public interface LoginPresenter {
    void checkSession();
    void doLogin(String email, String password);
    void doGuestLogin();
    void onGoogleSignInClick(GoogleSignInClient client);
    void handleGoogleResult(Intent data);
    void onDestroy();
}