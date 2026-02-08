package com.example.food_planner.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.food_planner.model.MealDetail;
import java.util.List;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFav(MealDetail meal);

    @Delete
    Completable deleteFav(MealDetail meal);

    @Query("SELECT * FROM fav_meals WHERE userId = :userId")
    Flowable<List<MealDetail>> getFavMeals(String userId);

    @Query("SELECT EXISTS (SELECT 1 FROM fav_meals WHERE id = :mealId AND userId = :userId)")
    Single<Boolean> isFav(String mealId, String userId);
}