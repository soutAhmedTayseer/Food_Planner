package com.example.food_planner.utils;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.example.food_planner.R;

public class ViewUtils {

    // General Message (Default Style)
    public static void showMessage(View view, String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(view.getContext().getColor(R.color.brand_secondary));
            snackbar.setTextColor(view.getContext().getColor(R.color.white));
            snackbar.show();
        }
    }

    // Success Message (Green Background)
    public static void showSuccess(View view, String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(view.getContext().getColor(R.color.brand_primary));
            snackbar.setTextColor(view.getContext().getColor(R.color.white));
            snackbar.show();
        }
    }

    // Error Message (Red Background)
    public static void showError(View view, String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(view.getContext().getColor(R.color.error_red));
            snackbar.setTextColor(view.getContext().getColor(R.color.white));
            snackbar.show();
        }
    }
}