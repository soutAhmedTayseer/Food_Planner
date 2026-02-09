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

// Repository Pattern: This class handles "Who is using the app?"
// It coordinates between Firebase Auth (Login), Firestore (Cloud Data), and Room (Local Cache).
public class UserRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LocalDataSource localDataSource;
    private SharedPrefManager sharedPrefManager;

    public UserRepository(Context context) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // We initialize the Local Data Source so we can cache user details
        localDataSource = LocalDataSourceImpl.getInstance(context);
        sharedPrefManager = new SharedPrefManager(context);
    }

    // --- DELETE ACCOUNT (Critical Security Feature) ---
    public void deleteAccount(String password, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {

            // 1. Re-authenticate: Prove it's really the user right now.
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential).addOnCompleteListener(authTask -> {
                if (authTask.isSuccessful()) {
                    String uid = user.getUid();

                    // 2. Delete from Cloud Database (Firestore) FIRST.
                    db.collection("users").document(uid).delete().addOnCompleteListener(firestoreTask -> {

                        // 3. Delete the Login Account (Firebase Auth) permanently.
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                // 4. Wipe all local data on the phone so the app is fresh.
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

    // --- SIGN UP ---
    public void signUp(String username, String email, String password, AuthCallback callback) {
        // 1. Create the account in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();

                    // 2. Add the Username to the Auth Profile
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    firebaseUser.updateProfile(profileUpdates);

                    // 3. Save details to Cloud Database
                    saveUserToFirestore(uid, email, username);

                    // 4. Save details to Local Database (Room)
                    saveUserLocally(new UserEntity(uid, email));

                    // 5. Start the Session
                    sharedPrefManager.saveUserSession(email, uid);
                    callback.onSuccess();
                }
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    // --- LOGIN ---
    public void login(String email, String password, AuthCallback callback) {
        // 1. Verify credentials with Firebase
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();

                    // 2. Save Session locally
                    sharedPrefManager.saveUserSession(email, uid);

                    // 3. Fetch the latest user details from the Cloud to update the Local DB
                    syncUserFromFirestore(uid);
                    callback.onSuccess();
                }
            } else {
                callback.onError(task.getException().getMessage());
            }
        });
    }

    // --- LOGOUT ---
    public void logout() {
        // 1. Sign out of Firebase
        mAuth.signOut();

        // 2. Clear Shared Preferences
        sharedPrefManager.logoutUser();

        // 3. Wipe the User Table in the Local Database
        localDataSource.clearUser().subscribeOn(Schedulers.io()).subscribe();
    }

    // --- GOOGLE SIGN IN ---
    // Handles both Login AND Registration for Google Users.
    public void firebaseAuthWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    String email = user.getEmail();
                    String name = user.getDisplayName();

                    // Always update Firestore and Local DB to ensure data is fresh
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

    // --- HELPER: Cloud Storage ---
    // Saves user info to the "users" collection in Firestore
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

    // --- HELPER: Synchronization ---
    private void syncUserFromFirestore(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");
                UserEntity user = new UserEntity(uid, email);
                saveUserLocally(user);
            }
        });
    }

    // --- HELPER: Local Storage ---
    // Uses RxJava to insert into Room Database on a background thread.
    private void saveUserLocally(UserEntity user) {
        localDataSource.insertUser(user).subscribeOn(Schedulers.io()).subscribe();
    }

    public interface AuthCallback {
        void onSuccess();

        void onError(String message);
    }
}