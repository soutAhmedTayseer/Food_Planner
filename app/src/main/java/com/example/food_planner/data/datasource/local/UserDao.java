package com.example.food_planner.data.datasource.local;

import androidx.room.*;
import com.example.food_planner.model.UserEntity;
import io.reactivex.rxjava3.core.Completable;

@Dao
public interface UserDao {
    // Save the user. Replace if they already exist.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(UserEntity user);

    // Fetch the single logged-in user. LIMIT 1 ensures we just get the first record.
    @Query("SELECT * FROM user_table LIMIT 1")
    UserEntity getUser();

    // Logout: Wipe the user table.
    @Query("DELETE FROM user_table")
    Completable clearUser();
}