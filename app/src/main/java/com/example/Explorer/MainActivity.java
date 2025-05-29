package com.example.Explorer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.LocationEntity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "f660a2fb1e4bad108d6160b7f58c555f";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private BottomNavigationView bottomNav;
    private AppDatabase database;
    private String weather = "99°";
    private String currentCity = "Unknown";
    private WeatherAPI weatherAPI;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = AppDatabase.getDatabase(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        setupRetrofit();
        requestLocation();
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_layout_fragment, new ProfileFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.navigation_profile);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            getCityName(location.getLatitude(), location.getLongitude());
                            saveLocationToDatabase(location);
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveLocationToDatabase(Location location) {
        new Thread(() -> {
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.latitude = location.getLatitude();
            locationEntity.longitude = location.getLongitude();
            locationEntity.name = currentCity;
            locationEntity.timestamp = System.currentTimeMillis();

            database.locationDao().insert(locationEntity);
        }).start();
    }
    private void getCityName(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                currentCity = addresses.get(0).getLocality();
                if (currentCity == null) {
                    currentCity = "Unknown";
                }
                fetchWeatherData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            currentCity = "Unknown";
            fetchWeatherData();
        }
    }


    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

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
        if (currentCity == null || currentCity.equals("Unknown")) {
            weather = "-99°";
            return;
        }

        Call<WeatherResponse> call = weatherAPI.getCurrentWeather(
                currentCity,
                API_KEY,
                "metric"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();
                    int temperature = (int) Math.round(weatherResponse.main.temp);
                    String temperatureText = temperature > 0 ? "+" + temperature + "°" : temperature + "°";

                    runOnUiThread(() -> {
                        weather = temperatureText;
                        updateFragments();
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

    private void updateFragments() {
        // Notify all fragments that weather data has changed
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof WeatherUpdateListener) {
                ((WeatherUpdateListener) fragment).onWeatherUpdated();
            }
        }
    }

    interface WeatherUpdateListener {
        void onWeatherUpdated();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            weather = "-99°";
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
