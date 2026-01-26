package com.example.food_planner.homescreen;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.food_planner.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // TODO: Setup NavigationController here in the next step
    }
}