package com.example.food_planner.repository;

import android.content.Context;

import com.example.food_planner.db.FoodPlannerDatabase;
import com.example.food_planner.db.UserDao;
import com.example.food_planner.model.UserEntity;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserDao userDao;
    private SharedPrefManager sharedPrefManager;

    public UserRepository(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userDao = FoodPlannerDatabase.getInstance(context).userDao();
        sharedPrefManager = new SharedPrefManager(context);
    }

    // --- FIREBASE ACTIONS ---

    // 1. Sign Up
    public void signUp(String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // 1. Save to Firestore (Cloud)
                            saveUserToFirestore(uid, email);

                            // 2. Save to Room (Local)
                            UserEntity localUser = new UserEntity(uid, email);
                            saveUserLocally(localUser);

                            // 3. Save Session (SharedPrefs)
                            sharedPrefManager.saveUserSession(email, uid);

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
                            String uid = firebaseUser.getUid();

                            // 1. Save Session (SharedPrefs)
                            sharedPrefManager.saveUserSession(email, uid);

                            // 2. SYNC: Download Data from Firestore to Room
                            syncUserFromFirestore(uid);

                            callback.onSuccess();
                        }
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // 3. Logout
    public void logout() {
        mAuth.signOut();
        sharedPrefManager.logoutUser();
        // Optionally clear Room DB here
        userDao.clearUser().subscribeOn(Schedulers.io()).subscribe();
    }

    // --- HELPERS ---

    private void saveUserToFirestore(String uid, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("uid", uid);
        db.collection("users").document(uid).set(userMap);
    }

    // Retrieves archived data from server and puts it in local DB
    private void syncUserFromFirestore(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        // Add more fields here if you have them (e.g. name, preferences)

                        UserEntity user = new UserEntity(uid, email);
                        saveUserLocally(user);
                    }
                });
    }

    private void saveUserLocally(UserEntity user) {
        userDao.insertUser(user)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public interface AuthCallback {
        void onSuccess();

        void onError(String message);
    }
}