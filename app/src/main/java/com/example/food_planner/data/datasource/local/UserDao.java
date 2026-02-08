package com.example.food_planner.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.food_planner.model.UserEntity;
import io.reactivex.rxjava3.core.Completable;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertUser(UserEntity user);

    @Query("SELECT * FROM user_table LIMIT 1")
    UserEntity getUser();

    @Query("DELETE FROM user_table")
    Completable clearUser();
}