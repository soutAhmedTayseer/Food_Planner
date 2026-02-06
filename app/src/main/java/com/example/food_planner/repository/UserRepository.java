package com.example.food_planner.repository;

import android.content.Context;

import com.example.food_planner.db.FoodPlannerDatabase;
import com.example.food_planner.db.UserDao;
import com.example.food_planner.model.UserEntity;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
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

    // 1. Sign Up (Updated to include Username)
    public void signUp(String username, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();

                    // A. Update Auth Profile with Username
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();
                    firebaseUser.updateProfile(profileUpdates);

                    // B. Save to Firestore (Cloud) with Username
                    saveUserToFirestore(uid, email, username);

                    // C. Save to Room & Session
                    saveUserLocally(new UserEntity(uid, email));
                    sharedPrefManager.saveUserSession(email, uid);

                    callback.onSuccess();
                }
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    // 2. Login (Unchanged)
    public void login(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    sharedPrefManager.saveUserSession(email, uid);
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
        userDao.clearUser().subscribeOn(Schedulers.io()).subscribe();
    }

    // GOOGLE SIGN-IN
    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    String email = user.getEmail();
                    String name = user.getDisplayName(); // Google provides name

                    // Save/Update User in Firestore
                    saveUserToFirestore(uid, email, name != null ? name : "Google User");

                    sharedPrefManager.saveUserSession(email, uid);
                    saveUserLocally(new UserEntity(uid, email));
                    syncUserFromFirestore(uid);

                    callback.onSuccess();
                }
            } else {
                callback.onError(task.getException() != null ? task.getException().getMessage() : "Google Sign-In failed");
            }
        });
    }

    // Helper: Save user info to Firestore (Updated with username)
    private void saveUserToFirestore(String uid, String email, String username) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("uid", uid);
        userMap.put("username", username);
        db.collection("users").document(uid).set(userMap);
    }

    // Helper: Update only the username in Firestore (used by ProfileFragment)
    public void updateNameInFirestore(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("username", newName);
        }
    }

    private void syncUserFromFirestore(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");
                UserEntity user = new UserEntity(uid, email);
                saveUserLocally(user);
            }
        });
    }

    private void saveUserLocally(UserEntity user) {
        userDao.insertUser(user).subscribeOn(Schedulers.io()).subscribe();
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }
}