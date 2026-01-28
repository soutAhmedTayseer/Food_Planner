package com.example.food_planner.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class MealDetail implements Parcelable {
    private String id;
    private String name;
    private String category;
    private String area;
    private String instructions;
    private String thumbUrl;
    private String youtubeUrl;

    // The clean list we want
    private List<Ingredient> ingredients = new ArrayList<>();

    // Empty Constructor
    public MealDetail() {}

    // --- GETTERS & SETTERS ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
    public List<Ingredient> getIngredients() { return ingredients; }
    public void addIngredient(Ingredient ingredient) { this.ingredients.add(ingredient); }

    // --- PARCELABLE IMPLEMENTATION (Required for Navigation) ---
    protected MealDetail(Parcel in) {
        id = in.readString();
        name = in.readString();
        category = in.readString();
        area = in.readString();
        instructions = in.readString();
        thumbUrl = in.readString();
        youtubeUrl = in.readString();
        ingredients = in.createTypedArrayList(Ingredient.CREATOR);
    }

    public static final Creator<MealDetail> CREATOR = new Creator<MealDetail>() {
        @Override
        public MealDetail createFromParcel(Parcel in) { return new MealDetail(in); }
        @Override
        public MealDetail[] newArray(int size) { return new MealDetail[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(area);
        dest.writeString(instructions);
        dest.writeString(thumbUrl);
        dest.writeString(youtubeUrl);
        dest.writeTypedList(ingredients);
    }

    // --- INNER CLASS: INGREDIENT ---
    public static class Ingredient implements Parcelable {
        private String name;
        private String measure;

        public Ingredient(String name, String measure) {
            this.name = name;
            this.measure = measure;
        }

        public String getName() { return name; }
        public String getMeasure() { return measure; }
        public String getImageUrl() {
            return "https://www.themealdb.com/images/ingredients/" + name + "-Small.png";
        }

        protected Ingredient(Parcel in) {
            name = in.readString();
            measure = in.readString();
        }

        public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {
            @Override
            public Ingredient createFromParcel(Parcel in) { return new Ingredient(in); }
            @Override
            public Ingredient[] newArray(int size) { return new Ingredient[size]; }
        };

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(measure);
        }
    }
}