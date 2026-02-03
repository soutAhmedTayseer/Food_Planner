package com.example.food_planner.userscreen.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_planner.R;
import com.example.food_planner.repository.UserRepository;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileFragment extends Fragment {

    private UserRepository userRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Repository to handle logout logic
        userRepository = new UserRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. Initialize Views ---
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        MaterialButton btnEditProfile = view.findViewById(R.id.btnEditProfile);
        LinearLayout btnChangePassword = view.findViewById(R.id.btnChangePassword);
        LinearLayout btnBackup = view.findViewById(R.id.btnBackup);
        MaterialSwitch switchTheme = view.findViewById(R.id.switchTheme);
        MaterialButtonToggleGroup toggleLanguage = view.findViewById(R.id.toggleLanguage);

        // --- 2. Animations ---
        animateEntry(view);

        // --- 3. Setup Listeners ---

        // Theme Toggle
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? getString(R.string.dark_mode_enabled) : getString(R.string.light_mode_enabled);
            ViewUtils.showMessage(getView(), msg);
        });

        // Language Toggle
        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnLangEn) {
                    ViewUtils.showMessage(getView(), getString(R.string.language_changed_to_english));
                } else if (checkedId == R.id.btnLangAr) {
                    ViewUtils.showMessage(getView(), getString(R.string.language_changed_to_arabic));
                }
            }
        });

        // Profile Actions
        btnEditProfile.setOnClickListener(v -> ViewUtils.showMessage(getView(), getString(R.string.edit_profile_clicked)));
        btnChangePassword.setOnClickListener(v -> ViewUtils.showMessage(getView(), getString(R.string.change_password_clicked)));
        btnBackup.setOnClickListener(v -> ViewUtils.showSuccess(getView(), getString(R.string.data_sync_started)));

        // Logout Action -> Shows Dialog
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void animateEntry(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        view.startAnimation(anim);
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
        // 1. Clear Session (Firebase + SharedPrefs + Room)
        userRepository.logout();

        // 2. Navigate to Login Activity
        if (getActivity() != null) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            // Clear the back stack so user cannot go back to Profile
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }
}