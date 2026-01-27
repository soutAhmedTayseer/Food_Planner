package com.example.food_planner.userscreen.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.food_planner.R;
import com.example.food_planner.signin.LoginActivity;
import com.example.food_planner.utils.ViewUtils; // Import your global Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {

    private Button btnLangEn;
    private Button btnLangAr;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        Button btnLogout = view.findViewById(R.id.btnLogout);
        CardView btnEditProfile = view.findViewById(R.id.btnEditProfile);
        CardView btnChangePassword = view.findViewById(R.id.btnChangePassword);
        CardView btnBackup = view.findViewById(R.id.btnBackup);

        // New Toggles
        SwitchMaterial switchTheme = view.findViewById(R.id.switchTheme);
        btnLangEn = view.findViewById(R.id.btnLangEn);
        btnLangAr = view.findViewById(R.id.btnLangAr);

        // 2. Setup Listeners

        // -- Theme Toggle --
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                ViewUtils.showMessage(getView(), getString(R.string.dark_mode_enabled));
                // TODO: Add your AppCompatDelegate code here later
            } else {
                ViewUtils.showMessage(getView(), getString(R.string.light_mode_enabled));
            }
        });

        // -- Language Toggles --
        btnLangEn.setOnClickListener(v -> setLanguage(getString(R.string.en)));
        btnLangAr.setOnClickListener(v -> setLanguage(getString(R.string.ar)));

        // -- Existing Actions --
        btnLogout.setOnClickListener(v -> performLogout());

        btnEditProfile.setOnClickListener(v ->
                ViewUtils.showMessage(getView(), getString(R.string.edit_profile_clicked))
        );

        btnChangePassword.setOnClickListener(v ->
                ViewUtils.showMessage(getView(), getString(R.string.change_password_clicked))
        );

        btnBackup.setOnClickListener(v ->
                ViewUtils.showSuccess(getView(), getString(R.string.data_sync_started))
        );
    }

    // Helper to visually switch language buttons
    private void setLanguage(String lang) {
        if (getContext() == null) return;

        int activeColor = ContextCompat.getColor(getContext(), R.color.brand_primary); // Green
        int inactiveColor = ContextCompat.getColor(getContext(), R.color.text_secondary); // Grey
        int white = ContextCompat.getColor(getContext(), R.color.white);
        int transparent = Color.TRANSPARENT;

        if (lang.equals("en")) {
            // Highlight EN
            btnLangEn.setBackgroundTintList(ColorStateList.valueOf(white));
            btnLangEn.setTextColor(activeColor);

            // Dim AR
            btnLangAr.setBackgroundTintList(ColorStateList.valueOf(transparent));
            btnLangAr.setTextColor(inactiveColor);

            ViewUtils.showMessage(getView(), getString(R.string.language_changed_to_english));
        } else {
            // Highlight AR
            btnLangAr.setBackgroundTintList(ColorStateList.valueOf(white));
            btnLangAr.setTextColor(activeColor);

            // Dim EN
            btnLangEn.setBackgroundTintList(ColorStateList.valueOf(transparent));
            btnLangEn.setTextColor(inactiveColor);

            ViewUtils.showMessage(getView(), getString(R.string.language_changed_to_arabic));
        }
    }

    private void performLogout() {
        if (getActivity() == null) return;

        // 1. Clear SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(getString(R.string.foodplannerprefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Specifically removing the flags checked in Splash Screen
        editor.remove(getString(R.string.isloggedin));
        editor.remove(getString(R.string.isguest));
        editor.remove(getString(R.string.useremail)); // Clear user info if stored
        editor.apply();

        // 2. Navigate back to LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);

        // 3. Clear the Back Stack so user cannot return to Profile
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        requireActivity().finish(); // Close the HomeActivity
    }
}