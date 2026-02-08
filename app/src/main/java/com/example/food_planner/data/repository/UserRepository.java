package com.example.food_planner.data.repository;

import android.content.Context;

import com.example.food_planner.data.datasource.local.LocalDataSource;
import com.example.food_planner.data.datasource.local.LocalDataSourceImpl;
import com.example.food_planner.model.UserEntity;
import com.example.food_planner.utils.SharedPrefManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
    private LocalDataSource localDataSource; // Replaced UserDao
    private SharedPrefManager sharedPrefManager;

    public UserRepository(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Initialize LocalDataSource via its Implementation
        localDataSource = LocalDataSourceImpl.getInstance(context);
        sharedPrefManager = new SharedPrefManager(context);
    }

    // --- DELETE ACCOUNT ---
    public void deleteAccount(String password, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // 1. Re-authenticate User
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                if (authTask.isSuccessful()) {
                    String uid = user.getUid();

                    // 2. Delete from Firestore
                    db.collection("users").document(uid).delete().addOnCompleteListener(firestoreTask -> {

                        // 3. Delete from Firebase Auth (Permanent)
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                // 4. Clear Local Data via DataSource
                                logout();
                                callback.onSuccess();
                            } else {
                                callback.onError("Account deleted from DB but Auth deletion failed: " + deleteTask.getException().getMessage());
                            }
                        });
                    });
                } else {
                    callback.onError("Incorrect password. Re-authentication failed.");
                }
            });
        } else {
            callback.onError("User not logged in.");
        }
    }

    // --- EXISTING METHODS ---

    public void signUp(String username, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    firebaseUser.updateProfile(profileUpdates);
                    saveUserToFirestore(uid, email, username);
                    saveUserLocally(new UserEntity(uid, email));
                    sharedPrefManager.saveUserSession(email, uid);
                    callback.onSuccess();
                }
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

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

    public void logout() {
        mAuth.signOut();
        sharedPrefManager.logoutUser();
        // Use localDataSource instead of direct DAO access
        localDataSource.clearUser().subscribeOn(Schedulers.io()).subscribe();
    }

    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    String email = user.getEmail();
                    String name = user.getDisplayName();
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

    private void saveUserToFirestore(String uid, String email, String username) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("uid", uid);
        userMap.put("username", username);
        db.collection("users").document(uid).set(userMap);
    }

    public void updateNameInFirestore(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).update("username", newName);
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
        // Use localDataSource instead of direct DAO access
        localDataSource.insertUser(user).subscribeOn(Schedulers.io()).subscribe();
    }

    public interface AuthCallback {
        void onSuccess();

        void onError(String message);
    }
}