package com.example.food_planner.data.repository;

import android.content.Context;
import com.example.food_planner.data.datasource.local.LocalDataSource;
import com.example.food_planner.data.datasource.local.LocalDataSourceImpl;
import com.example.food_planner.data.datasource.remote.RemoteDataSource;
import com.example.food_planner.data.datasource.remote.RemoteDataSourceImpl;
import com.example.food_planner.model.*;
import com.example.food_planner.utils.SharedPrefManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepository {

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final SharedPrefManager sharedPrefManager;

    private final DatabaseReference firebaseRoot;
    private DatabaseReference favoritesRef;
    private DatabaseReference plannedRef;

    private ChildEventListener favoritesListener;
    private ChildEventListener plannedListener;

    private static MealRepository instance;

    public static MealRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MealRepository(context);
        }
        return instance;
    }

    private MealRepository(Context context) {
        // Use Application Context to be safe
        Context appContext = context.getApplicationContext();
        this.localDataSource = LocalDataSourceImpl.getInstance(appContext);
        this.remoteDataSource = RemoteDataSourceImpl.getInstance();
        this.sharedPrefManager = new SharedPrefManager(appContext);
        this.firebaseRoot = FirebaseDatabase.getInstance().getReference();
    }

    private String getCurrentUserId() {
        String uid = sharedPrefManager.getUserUid();
        return uid.isEmpty() ? "guest" : uid;
    }

    // --- SYNC: Firebase -> Room ---

    public void syncFavoritesFromFirebase(String userId) {
        if (userId == null || userId.equals("guest"))
            return;

        removeFirebaseListeners();
        favoritesRef = firebaseRoot.child("users").child(userId).child("favorites");

        favoritesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                MealDetail meal = snapshot.getValue(MealDetail.class);
                if (meal != null) {
                    meal.setUserId(userId);
                    // Force update local DB
                    localDataSource.insertFav(meal)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                            }, Throwable::printStackTrace);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                onChildAdded(snapshot, previousChildName);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                MealDetail meal = snapshot.getValue(MealDetail.class);
                if (meal != null) {
                    meal.setUserId(userId);
                    localDataSource.deleteFav(meal)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                            }, Throwable::printStackTrace);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
        favoritesRef.addChildEventListener(favoritesListener);
    }

    public void syncPlannedMealsFromFirebase(String userId) {
        if (userId == null || userId.equals("guest"))
            return;

        plannedRef = firebaseRoot.child("users").child(userId).child("plannedMeals");

        plannedListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                PlanMeal plan = snapshot.getValue(PlanMeal.class);
                if (plan != null) {
                    plan.setUserId(userId);
                    localDataSource.insertPlan(plan)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                            }, Throwable::printStackTrace);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                onChildAdded(snapshot, previousChildName);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                PlanMeal plan = snapshot.getValue(PlanMeal.class);
                if (plan != null) {
                    localDataSource.deletePlan(plan)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                            }, Throwable::printStackTrace);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
        plannedRef.addChildEventListener(plannedListener);
    }

    public void removeFirebaseListeners() {
        if (favoritesListener != null && favoritesRef != null) {
            favoritesRef.removeEventListener(favoritesListener);
            favoritesListener = null;
        }
        if (plannedListener != null && plannedRef != null) {
            plannedRef.removeEventListener(plannedListener);
            plannedListener = null;
        }
    }

    // --- OPERATIONS: Room -> Firebase ---

    public Completable addToFavorites(MealDetail meal) {
        String uid = getCurrentUserId();
        meal.setUserId(uid);

        return localDataSource.insertFav(meal)
                .subscribeOn(Schedulers.io())
                .andThen(Completable.defer(() -> {
                    if (!uid.equals("guest")) {
                        return Completable.create(emitter -> firebaseRoot.child("users").child(uid)
                                .child("favorites").child(meal.getId())
                                .setValue(meal)
                                .addOnSuccessListener(aVoid -> emitter.onComplete())
                                .addOnFailureListener(emitter::onError));
                    }
                    return Completable.complete();
                }));
    }

    public Completable removeFromFavorites(MealDetail meal) {
        String uid = getCurrentUserId();
        meal.setUserId(uid);

        return localDataSource.deleteFav(meal)
                .subscribeOn(Schedulers.io())
                .andThen(Completable.defer(() -> {
                    if (!uid.equals("guest")) {
                        return Completable.create(emitter -> firebaseRoot.child("users").child(uid)
                                .child("favorites").child(meal.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> emitter.onComplete())
                                .addOnFailureListener(emitter::onError));
                    }
                    return Completable.complete();
                }));
    }

    public Completable addToPlan(MealDetail meal, String date) {
        String uid = getCurrentUserId();
        PlanMeal planMeal = PlanMeal.fromMealDetail(meal, date, uid);
        String firebaseKey = planMeal.getMealId() + "_" + planMeal.getPlanDate();

        return localDataSource.insertPlan(planMeal)
                .subscribeOn(Schedulers.io())
                .andThen(Completable.defer(() -> {
                    if (!uid.equals("guest")) {
                        return Completable.create(emitter -> firebaseRoot.child("users").child(uid)
                                .child("plannedMeals").child(firebaseKey)
                                .setValue(planMeal)
                                .addOnSuccessListener(aVoid -> emitter.onComplete())
                                .addOnFailureListener(emitter::onError));
                    }
                    return Completable.complete();
                }));
    }

    public Completable removeFromPlan(PlanMeal planMeal) {
        String uid = getCurrentUserId();
        String firebaseKey = planMeal.getMealId() + "_" + planMeal.getPlanDate();

        return localDataSource.deletePlan(planMeal)
                .subscribeOn(Schedulers.io())
                .andThen(Completable.defer(() -> {
                    if (!uid.equals("guest")) {
                        return Completable.create(emitter -> firebaseRoot.child("users").child(uid)
                                .child("plannedMeals").child(firebaseKey)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> emitter.onComplete())
                                .addOnFailureListener(emitter::onError));
                    }
                    return Completable.complete();
                }));
    }

    // --- READ OPERATIONS (From Room) ---
    public Flowable<List<MealDetail>> getStoredMeals() {
        return localDataSource.getFavMeals(getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Single<Boolean> isFavorite(String mealId) {
        return localDataSource.isFav(mealId, getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlanMeal>> getPlansByDate(String date) {
        return localDataSource.getPlansByDate(date, getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    public Flowable<List<PlanMeal>> getAllPlans() {
        return localDataSource.getAllPlans(getCurrentUserId()).subscribeOn(Schedulers.io());
    }

    // --- REMOTE API DELEGATES ---
    public MealDetail getValidDailyMeal() {
        return sharedPrefManager.getValidDailyMeal();
    }

    public void saveDailyMeal(MealDetail meal) {
        sharedPrefManager.saveDailyMeal(meal);
    }

    public Single<MealResponse> getRandomMeal() {
        return remoteDataSource.getRandomMeal();
    }

    public Single<Meal> getCategories() {
        return remoteDataSource.getCategories();
    }

    public Single<Meal> getAreas() {
        return remoteDataSource.getAreas();
    }

    public Single<Meal> getIngredients() {
        return remoteDataSource.getIngredients();
    }

    public Single<Meal> filterByCategory(String c) {
        return remoteDataSource.filterByCategory(c);
    }

    public Single<Meal> filterByArea(String a) {
        return remoteDataSource.filterByArea(a);
    }

    public Single<Meal> filterByIngredient(String i) {
        return remoteDataSource.filterByIngredient(i);
    }

    public Single<MealResponse> getMealById(String id) {
        return remoteDataSource.getMealById(id);
    }

    // --- CACHE DELEGATES ---
    public void saveInspirationMeals(List<MealDetail> meals) {
        sharedPrefManager.saveInspirationMeals(meals);
    }

    public List<MealDetail> getInspirationMeals() {
        return sharedPrefManager.getInspirationMeals();
    }
}