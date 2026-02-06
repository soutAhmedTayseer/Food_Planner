package com.example.food_planner.userscreen.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_planner.R;
import com.example.food_planner.repository.UserRepository;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileFragment extends Fragment {

    private UserRepository userRepository;
    private SharedPrefManager sharedPrefManager;
    private FirebaseAuth mAuth;

    private TextView tvUserName, tvUserEmail;
    private Chip chipAccountType;
    private TextView btnEditName, btnChangePassword;
    private MaterialButton btnLogout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository(requireContext());
        sharedPrefManager = new SharedPrefManager(requireContext());
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        loadUserData();
        setupListeners();
    }

    private void initializeViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        chipAccountType = view.findViewById(R.id.chipAccountType);
        btnEditName = view.findViewById(R.id.btnEditName);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        // Removed dark mode, language, sync, backup views
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        if (sharedPrefManager.isGuest()) {
            // Guest Logic
            tvUserName.setText("Guest User");
            tvUserEmail.setText("No Email");
            chipAccountType.setText("Guest Account");

            // Disable Edit Buttons visually
            btnEditName.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
        } else {
            // Logged User Logic
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                // Get name from Firebase Auth Profile
                String name = user.getDisplayName();
                String email = user.getEmail();

                tvUserName.setText((name != null && !name.isEmpty()) ? name : "User");
                tvUserEmail.setText(email);
                chipAccountType.setText("Registered Account");

                btnEditName.setVisibility(View.VISIBLE);
                btnChangePassword.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupListeners() {
        btnEditName.setOnClickListener(v -> showChangeNameDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        // Removed theme/language/sync listeners
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showChangeNameDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Change Name");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) input.setText(user.getDisplayName());

        LinearLayout container = new LinearLayout(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateNameOnFirebase(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateNameOnFirebase(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // 1. Update Auth Profile (used for display in app)
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 2. Update Firestore as well to keep it synced
                            userRepository.updateNameInFirestore(newName);
                            tvUserName.setText(newName);
                            SnackbarUtil.showSuccess(getView(), "Name updated successfully");
                        } else {
                            SnackbarUtil.showError(getView(), "Failed to update name");
                        }
                    });
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etCurrentPass = new EditText(requireContext());
        etCurrentPass.setHint("Current Password");
        etCurrentPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrentPass);

        final EditText etNewPass = new EditText(requireContext());
        etNewPass.setHint("New Password (min 6 chars)");
        etNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPass);

        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String current = etCurrentPass.getText().toString();
            String newPass = etNewPass.getText().toString();

            if (!current.isEmpty() && newPass.length() >= 6) {
                updatePasswordOnFirebase(current, newPass);
            } else {
                SnackbarUtil.showError(getView(), "Invalid input. Password min 6 chars.");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updatePasswordOnFirebase(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            SnackbarUtil.showSuccess(getView(), "Password updated");
                        } else {
                            SnackbarUtil.showError(getView(), "Failed to update password");
                        }
                    });
                } else {
                    SnackbarUtil.showError(getView(), "Authentication failed. Check current password.");
                }
            });
        }
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        userRepository.logout();
        if (getActivity() != null) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }
}