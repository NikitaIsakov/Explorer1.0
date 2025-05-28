package com.example.Explorer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.LocationEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    public BottomNavigationView bottomNav;
    private AppDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "explorer.db"
        ).build();

        // тестовое сохранение в бд
        new Thread(() -> {
            try {
                LocationEntity location = new LocationEntity();
                location.latitude = 55.751574;
                location.longitude = 37.573856;
                location.name = "Москва";

                database.locationDao().insert(location);
            } catch (Exception e) {
                Log.e("DB", "Error saving location", e);
            }

        }).start();

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout_fragment, new ProfileFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.navigation_profile);
        }
    };

    public void selectBottomNavItem(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    private BottomNavigationView.OnItemSelectedListener navListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_profile) {
                        selectedFragment = new ProfileFragment();
                    } else if (itemId == R.id.navigation_map) {
                        selectedFragment = new MapFragment();
                    } else if (itemId == R.id.navigation_star) {
                        selectedFragment = new StarFragment();
                    } else if (itemId == R.id.navigation_settings) {
                        selectedFragment = new SettingsFragment();
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_layout_fragment, selectedFragment).commit();
                    return true;
                };
            };

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
};
