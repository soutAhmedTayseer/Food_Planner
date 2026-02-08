package com.example.food_planner.userscreen.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.food_planner.R;
import com.example.food_planner.data.repository.UserRepository;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.AlertUtil;
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
    private MaterialButton btnLogout, btnDeleteAccount;

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
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
    }

    private void loadUserData() {
        if (sharedPrefManager.isGuest()) {
            tvUserName.setText("Guest User");
            tvUserEmail.setText("No Email");
            chipAccountType.setText("Guest Account");

            btnEditName.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
            btnDeleteAccount.setVisibility(View.GONE);
        } else {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String name = user.getDisplayName();
                String email = user.getEmail();

                tvUserName.setText((name != null && !name.isEmpty()) ? name : "User");
                tvUserEmail.setText(email);
                chipAccountType.setText("Registered Account");

                btnEditName.setVisibility(View.VISIBLE);
                btnChangePassword.setVisibility(View.VISIBLE);
                btnDeleteAccount.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupListeners() {
        btnEditName.setOnClickListener(v -> showChangeNameDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Generic Logout Dialog
        btnLogout.setOnClickListener(v -> AlertUtil.showConfirmationDialog(
                requireContext(),
                R.string.log_out,
                "Are you sure you want to log out?",
                this::performLogout
        ));

        // Specific Delete Account Dialog
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    // --- DELETE ACCOUNT LOGIC (Local to Fragment) ---

    private void showDeleteAccountDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.delete_account);
        builder.setMessage("This action is permanent. Please enter your password to confirm.");

        // Input Field
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(R.string.password);

        // Layout Container
        LinearLayout container = new LinearLayout(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("DELETE PERMANENTLY", (dialog, which) -> {
            String password = input.getText().toString();
            if (!password.isEmpty()) {
                performDeleteAccount(password);
            } else {
                SnackbarUtil.showError(getView(), "Password is required to delete account.");
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Color the Positive button RED
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red));
    }

    private void performDeleteAccount(String password) {
        userRepository.deleteAccount(password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                SnackbarUtil.showSuccess(getView(), getString(R.string.account_permanently_deleted));
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) {
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                }, 2000);
            }

            @Override
            public void onError(String message) {
                SnackbarUtil.showError(getView(), "Delete Failed: " + message);
            }
        });
    }

    // --- OTHER DIALOGS ---

    private void performLogout() {
        userRepository.logout();
        if (getActivity() != null) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    private void showChangeNameDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.name);

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
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateNameOnFirebase(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(newName).build();
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.change_password);

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
        builder.setNegativeButton(R.string.cancel, null);
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
}