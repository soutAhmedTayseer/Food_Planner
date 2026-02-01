package com.example.food_planner.userscreen.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout; // For the row clicks

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.food_planner.R;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileFragment extends Fragment {

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

        // These are LinearLayouts now because they are rows inside a card
        LinearLayout btnChangePassword = view.findViewById(R.id.btnChangePassword);
        LinearLayout btnBackup = view.findViewById(R.id.btnBackup);

        MaterialSwitch switchTheme = view.findViewById(R.id.switchTheme);
        MaterialButtonToggleGroup toggleLanguage = view.findViewById(R.id.toggleLanguage);

        // --- 2. Animations (Fade In Entrance) ---
        animateEntry(view);

        // --- 3. Setup Listeners ---

        // -- Theme Toggle --
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? getString(R.string.dark_mode_enabled) : getString(R.string.light_mode_enabled);
            ViewUtils.showMessage(getView(), msg);
            // TODO: Apply AppCompatDelegate.setDefaultNightMode(...)
        });

        // -- Language Toggle (New Material Way) --
        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnLangEn) {
                    ViewUtils.showMessage(getView(), getString(R.string.language_changed_to_english));
                    // Update Locale Logic
                } else if (checkedId == R.id.btnLangAr) {
                    ViewUtils.showMessage(getView(), getString(R.string.language_changed_to_arabic));
                    // Update Locale Logic
                }
            }
        });

        // -- Actions --
        btnEditProfile.setOnClickListener(v -> ViewUtils.showMessage(getView(), getString(R.string.edit_profile_clicked)));

        btnChangePassword.setOnClickListener(v -> ViewUtils.showMessage(getView(), getString(R.string.change_password_clicked)));

        btnBackup.setOnClickListener(v -> ViewUtils.showSuccess(getView(), getString(R.string.data_sync_started)));

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void animateEntry(View view) {
        // Simple fade-in animation for the whole content
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }

    private void performLogout() {
        if (getActivity() == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences(getString(R.string.foodplannerprefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(getString(R.string.isloggedin));
        editor.remove(getString(R.string.isguest));
        editor.remove(getString(R.string.useremail));
        editor.apply();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}