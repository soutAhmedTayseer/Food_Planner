package com.example.food_planner.model;

import com.google.gson.annotations.SerializedName;

public class MealItem {

    // --- API Fields ---
    @SerializedName("strCategory")
    private String categoryName;

    @SerializedName("strArea")
    private String areaName;

    @SerializedName("strIngredient")
    private String ingredientName;

    @SerializedName("strCategoryThumb")
    private String categoryThumb;

    // --- 1. Smart Name Getter ---
    public String getName() {
        if (categoryName != null && !categoryName.isEmpty()) return categoryName;
        if (areaName != null && !areaName.isEmpty()) return areaName;
        if (ingredientName != null && !ingredientName.isEmpty()) return ingredientName;
        return "Unknown";
    }

    // --- 2. Smart Image Getter ---
    public String getThumbnailUrl() {
        // A. Category Image (Direct from API)
        if (categoryThumb != null && !categoryThumb.isEmpty()) {
            return categoryThumb;
        }

        // B. Category Image (Constructed manually)
        if (categoryName != null && !categoryName.isEmpty()) {
            return "https://www.themealdb.com/images/category/" + categoryName + ".png";
        }

        // C. Ingredient Image
        if (ingredientName != null && !ingredientName.isEmpty()) {
            return "https://www.themealdb.com/images/ingredients/" + ingredientName + ".png";
        }

        // D. Country Flag (Using FlagCDN)
        if (areaName != null && !areaName.isEmpty()) {
            String countryCode = getCountryCode(areaName);
            return "https://flagcdn.com/w320/" + countryCode + ".png";
        }

        return null;
    }

    private String getCountryCode(String areaName) {
        switch (areaName) {
            case "American": return "us";
            case "British": return "gb";
            case "Canadian": return "ca";
            case "Chinese": return "cn";
            case "Croatian": return "hr";
            case "Dutch": return "nl";
            case "Egyptian": return "eg";
            case "Filipino": return "ph";
            case "French": return "fr";
            case "Greek": return "gr";
            case "Indian": return "in";
            case "Irish": return "ie";
            case "Italian": return "it";
            case "Jamaican": return "jm";
            case "Japanese": return "jp";
            case "Kenyan": return "ke";
            case "Malaysian": return "my";
            case "Mexican": return "mx";
            case "Moroccan": return "ma";
            case "Polish": return "pl";
            case "Portuguese": return "pt";
            case "Russian": return "ru";
            case "Spanish": return "es";
            case "Thai": return "th";
            case "Tunisian": return "tn";
            case "Turkish": return "tr";
            case "Vietnamese": return "vn";
            case "Algerian": return "dz";
            case "Argentinian": return "ar";
            case "Australian": return "au";
            case "Norwegian": return "no";
            case "Saudi Arabian": return "sa";
            case "Slovakian": return "sk";
            case "Syrian": return "sy";
            case "Ukrainian": return "ua";
            case "Uruguayan": return "uy";
            case "Venezulan": return "ve";
            case "Unknown": return "un";
            default: return "un";
        }
    }
}