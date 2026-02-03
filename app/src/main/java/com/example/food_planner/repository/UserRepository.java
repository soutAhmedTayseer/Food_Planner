package com.example.food_planner.repository;

import android.content.Context;

import com.example.food_planner.db.FoodPlannerDatabase;
import com.example.food_planner.db.UserDao;
import com.example.food_planner.model.UserEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserDao userDao;

    public UserRepository(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userDao = FoodPlannerDatabase.getInstance(context).userDao();
    }

    // --- FIREBASE ACTIONS ---

    // 1. Sign Up
    public void signUp(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Save extra data to Firestore
                            saveUserToFirestore(firebaseUser.getUid(), email);

                            // Save to Room (Local Cache)
                            UserEntity localUser = new UserEntity(firebaseUser.getUid(), email);
                            saveUserLocally(localUser);

                            callback.onSuccess();
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // 2. Login
    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Sync: Save to Room so app knows we are logged in locally
                            UserEntity localUser = new UserEntity(firebaseUser.getUid(), email);
                            saveUserLocally(localUser);
                            callback.onSuccess();
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // --- HELPERS ---

    private void saveUserToFirestore(String uid, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("uid", uid);

        db.collection("users").document(uid).set(userMap);
    }

    private void saveUserLocally(UserEntity user) {
        // Fire and forget RxJava call to save to Room
        userDao.insertUser(user)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    // Simple interface to communicate back to Activity
    public interface AuthCallback {
        void onSuccess();

        void onError(String message);
    }
}