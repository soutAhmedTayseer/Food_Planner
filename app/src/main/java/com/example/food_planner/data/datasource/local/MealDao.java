package com.example.food_planner.data.datasource.local;

import androidx.room.*;
import com.example.food_planner.model.MealDetail;
import java.util.List;
import io.reactivex.rxjava3.core.*;

@Dao // Data Access Object: Room uses this interface to generate SQL code.
public interface MealDao {

    // REPLACE: If we try to insert a meal that already exists (same ID), overwrite it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFav(MealDetail meal);

    @Delete
    Completable deleteFav(MealDetail meal);

    // Get all favorites for a specific user. Returns a Flowable (streams updates).
    @Query("SELECT * FROM fav_meals WHERE userId = :userId")
    Flowable<List<MealDetail>> getFavMeals(String userId);

    // Efficient SQL check: Returns 1 (true) if the meal exists, rather than fetching the whole object.
    @Query("SELECT EXISTS (SELECT 1 FROM fav_meals WHERE id = :mealId AND userId = :userId)")
    Single<Boolean> isFav(String mealId, String userId);
}