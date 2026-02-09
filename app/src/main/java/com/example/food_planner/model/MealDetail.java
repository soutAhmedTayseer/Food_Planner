package com.example.food_planner.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// Primary Keys: Combining ID + UserId means two different users can favorite the same meal without conflict.
@Entity(tableName = "fav_meals", primaryKeys = {"id", "userId"})
public class MealDetail implements Parcelable {

    @NonNull
    @SerializedName("idMeal") // Maps JSON "idMeal" to Java "id"
    private String id;

    @NonNull
    private String userId = ""; // Default empty to avoid nulls

    @SerializedName("strMeal")
    private String name;

    @SerializedName("strCategory")
    private String category;

    @SerializedName("strArea")
    private String area;

    @SerializedName("strInstructions")
    private String instructions;

    @SerializedName("strMealThumb")
    private String thumbUrl;

    @SerializedName("strYoutube")
    private String youtubeUrl;

    // List of ingredients (Handled by our Custom Deserializer later)
    private List<Ingredient> ingredients = new ArrayList<>();

    public MealDetail() {
    }

    // --- GETTERS & SETTERS ---
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    // Parcelable Implementation:
    // Allows us to pass this entire object from one Activity/Fragment to another safely.
    protected MealDetail(Parcel in) {
        id = in.readString();
        userId = in.readString(); // Read userId
        name = in.readString();
        category = in.readString();
        area = in.readString();
        instructions = in.readString();
        thumbUrl = in.readString();
        youtubeUrl = in.readString();
        ingredients = in.createTypedArrayList(Ingredient.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId); // Write userId
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(area);
        dest.writeString(instructions);
        dest.writeString(thumbUrl);
        dest.writeString(youtubeUrl);
        dest.writeTypedList(ingredients);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MealDetail> CREATOR = new Creator<MealDetail>() {
        @Override
        public MealDetail createFromParcel(Parcel in) {
            return new MealDetail(in);
        }

        @Override
        public MealDetail[] newArray(int size) {
            return new MealDetail[size];
        }
    };

    // Inner class for Ingredients
    public static class Ingredient implements Parcelable {
        private String name;
        private String measure;

        public Ingredient(String name, String measure) {
            this.name = name;
            this.measure = measure;
        }

        public String getName() {
            return name;
        }

        public String getMeasure() {
            return measure;
        }

        // Construct the image URL dynamically using the ingredient name
        public String getImageUrl() {
            return "https://www.themealdb.com/images/ingredients/" + name + "-Small.png";
        }

        protected Ingredient(Parcel in) {
            name = in.readString();
            measure = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(measure);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
            @Override
            public Ingredient createFromParcel(Parcel in) {
                return new Ingredient(in);
            }

            @Override
            public Ingredient[] newArray(int size) {
                return new Ingredient[size];
            }
        };
    }
}