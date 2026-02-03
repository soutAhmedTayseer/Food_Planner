package com.example.food_planner.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_table")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String uid; // Matches Firebase UID
    public String email;

    public UserEntity(@NonNull String uid, String email) {
        this.uid = uid;
        this.email = email;
    }
}