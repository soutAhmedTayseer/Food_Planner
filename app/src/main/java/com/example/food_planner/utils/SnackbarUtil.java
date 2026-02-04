package com.example.food_planner.utils;

import android.view.View;

import com.example.food_planner.R;

public class SnackbarUtil {

    // General Message (Default Style)
    public static void showMessage(View view, String message) {
        if (view != null) {
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(view, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(view.getContext().getColor(R.color.brand_primary));
            snackbar.setTextColor(view.getContext().getColor(R.color.white));
            snackbar.show();
        }
    }

    // Success Message (Green Background)
    public static void showSuccess(View view, String message) {
        if (view != null) {
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(view, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(view.getContext().getColor(R.color.success_green));
            snackbar.setTextColor(view.getContext().getColor(R.color.white));
            snackbar.show();
        }
    }

    // Error Message (Red Background)
    public static void showError(View view, String message) {
        if (view != null) {
            com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(view, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(view.getContext().getColor(R.color.error_red));
            snackbar.setTextColor(view.getContext().getColor(R.color.white));
            snackbar.show();
        }
    }
}