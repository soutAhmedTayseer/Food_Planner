package com.example.food_planner.network;

import com.example.food_planner.model.MealDetail;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

            // 1. Create custom Gson with our Deserializer
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(MealDetail.class, new MealDeserializer())
                    .create();

            // 2. Build Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Use the custom Gson
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}