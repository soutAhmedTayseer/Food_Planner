package com.example.food_planner.userscreen.presenter;

import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.userscreen.view.ProfileView;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfilePresenterImpl implements ProfilePresenter {

    private final ProfileView view;
    private final UserRepository userRepository;
    private final SharedPrefManager sharedPrefManager;
    private final FirebaseAuth mAuth;

    public ProfilePresenterImpl(ProfileView view, UserRepository userRepository, SharedPrefManager sharedPrefManager) {
        this.view = view;
        this.userRepository = userRepository;
        this.sharedPrefManager = sharedPrefManager;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void loadUserProfile() {
        if (sharedPrefManager.isGuest()) {
            view.showGuestMode();
        } else {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String name = user.getDisplayName();
                String email = user.getEmail();
                view.showUserMode((name != null && !name.isEmpty()) ? name : "User", email);
            }
        }
    }

    @Override
    public void updateName(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userRepository.updateNameInFirestore(newName);
                    view.showSuccess("Name updated successfully");
                    // Refresh UI
                    loadUserProfile();
                } else {
                    view.showError("Failed to update name");
                }
            });
        }
    }

    @Override
    public void updatePassword(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            // Re-authenticate first
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update Password
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            view.showSuccess("Password updated");
                        } else {
                            view.showError("Failed to update password");
                        }
                    });
                } else {
                    view.showError("Authentication failed. Check current password.");
                }
            });
        }
    }

    @Override
    public void deleteAccount(String password) {
        // Using your repository's logic for consistency
        userRepository.deleteAccount(password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                view.showSuccess("Account permanently deleted");
                // Delay handled in View or here? MVP usually handles flow here,
                // but for simplicity with handlers, we can signal view to navigate.
                view.navigateToLogin();
            }

            @Override
            public void onError(String message) {
                view.showError("Delete Failed: " + message);
            }
        });
    }

    @Override
    public void logout() {
        userRepository.logout();
        view.navigateToLogin();
    }

    @Override
    public void onDestroy() {
        // Cleanup if needed
    }
}