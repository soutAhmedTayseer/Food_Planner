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
import com.example.food_planner.signin.view.LoginActivity;
import com.example.food_planner.userscreen.presenter.ProfilePresenter;
import com.example.food_planner.userscreen.presenter.ProfilePresenterImpl;
import com.example.food_planner.utils.AlertUtil;
import com.example.food_planner.utils.SharedPrefManager;
import com.example.food_planner.utils.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProfileFragment extends Fragment implements ProfileView {

    private TextView tvUserName, tvUserEmail;
    private Chip chipAccountType;
    private TextView btnEditName, btnChangePassword;
    private MaterialButton btnLogout, btnDeleteAccount;

    private ProfilePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        // Initialize Presenter
        presenter = new ProfilePresenterImpl(
                this,
                new UserRepository(requireContext()),
                new SharedPrefManager(requireContext())
        );

        presenter.loadUserProfile();
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

    private void setupListeners() {
        btnEditName.setOnClickListener(v -> showChangeNameDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnLogout.setOnClickListener(v -> AlertUtil.showConfirmationDialog(
                requireContext(),
                getString(R.string.log_out), // Using resource string properly
                "Are you sure you want to log out?",
                () -> presenter.logout()
        ));

        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    // --- MVP View Implementations ---

    @Override
    public void showGuestMode() {
        tvUserName.setText(R.string.guest_user);
        tvUserEmail.setText(R.string.no_email);
        chipAccountType.setText(R.string.guest_account);

        btnEditName.setVisibility(View.GONE);
        btnChangePassword.setVisibility(View.GONE);
        btnDeleteAccount.setVisibility(View.GONE);
    }

    @Override
    public void showUserMode(String name, String email) {
        tvUserName.setText(name);
        tvUserEmail.setText(email);
        chipAccountType.setText(R.string.registered_account);

        btnEditName.setVisibility(View.VISIBLE);
        btnChangePassword.setVisibility(View.VISIBLE);
        btnDeleteAccount.setVisibility(View.VISIBLE);
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
    public void navigateToLogin() {
        // Small delay to allow Toast/Snackbar to be seen if needed, or immediate
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getActivity() != null) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        }, 500);
    }


    // --- Dialogs (View Logic) ---

    private void showChangeNameDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.name);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(tvUserName.getText()); // Pre-fill current name

        LinearLayout container = new LinearLayout(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 0);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                presenter.updateName(newName);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
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
                presenter.updatePassword(current, newPass);
            } else {
                showError("Invalid input. Password min 6 chars.");
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showDeleteAccountDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.delete_account);
        builder.setMessage("This action is permanent. Please enter your password to confirm.");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(R.string.password);

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
                presenter.deleteAccount(password);
            } else {
                showError("Password is required to delete account.");
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.onDestroy();
        }
    }
}