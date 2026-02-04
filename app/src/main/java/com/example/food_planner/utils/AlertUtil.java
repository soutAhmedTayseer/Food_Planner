package com.example.food_planner.utils;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AlertUtil {

    public interface OnPositiveClickListener {
        void onPositiveClick();
    }

    public static void showConfirmationDialog(Context context, String title, String message, OnPositiveClickListener listener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    listener.onPositiveClick();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}