package com.example.food_planner.utils;

import android.content.Context;

import com.example.food_planner.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AlertUtil {

    public interface OnPositiveClickListener {
        void onPositiveClick();
    }

    public static void showConfirmationDialog(Context context, String title, String message, OnPositiveClickListener listener) {
        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(message).setPositiveButton(R.string.yes, (dialog, which) -> {
            listener.onPositiveClick();
            dialog.dismiss();
        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    public static void showLoginRequiredDialog(Context context, OnPositiveClickListener onLoginClicked) {
        new MaterialAlertDialogBuilder(context).setTitle(R.string.login_required)
                .setMessage(R.string.you_need_to_sign_in_to_access_this_feature)
                .setCancelable(false)
                .setPositiveButton(R.string.login, (dialog, which) -> {
                    onLoginClicked.onPositiveClick();
                    dialog.dismiss();
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }
}