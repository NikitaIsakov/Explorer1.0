package com.example.Explorer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.LocationEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "f660a2fb1e4bad108d6160b7f58c555f";
    private static final String CITY_NAME = "Moscow";
    public BottomNavigationView bottomNav;
    private AppDatabase database;
    private String weather;
    private WeatherAPI weatherAPI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getDatabase(this);

        setupRetrofit();

        fetchWeatherData();

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

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WEATHER_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherAPI = retrofit.create(WeatherAPI.class);
    }

    private void fetchWeatherData() {
        Call<WeatherResponse> call = weatherAPI.getCurrentWeather(
                CITY_NAME,
                API_KEY,
                "metric"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    int temperature = (int) Math.round(weatherResponse.main.temp);

                    String temperatureText;
                    if (temperature > 0) {
                        temperatureText = "+" + temperature + "°";
                    } else {
                        temperatureText = temperature + "°";
                    }

                    runOnUiThread(() -> {
                        weather = temperatureText;
                    });

                } else {
                    showError("Ошибка получения данных");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                showError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            // Показываем значение по умолчанию при ошибке
            weather = "0°";
        });
    }

    public void refreshWeather() {
        fetchWeatherData();
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

    public AppDatabase getDatabase() {
        return database;
    }
    @NonNull
    public String getWeather() {
        return weather;
    }

    @Override
    protected void onDestroy() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        super.onDestroy();
    }
};
