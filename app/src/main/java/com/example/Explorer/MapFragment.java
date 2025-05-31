package com.example.Explorer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.POIEntity;
import com.example.Explorer.database.VisitedLocationEntity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapFragment extends Fragment implements MainActivity.WeatherUpdateListener {
    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private List<POIEntity> currentPOIs = new ArrayList<>();
    private AppDatabase database;
    private OverpassAPIService overpassService;

    private Drawable unvisitedIcon;
    private Drawable visitedIcon;

    private boolean isLocationReady = false;
    private Handler locationCheckHandler = new Handler(Looper.getMainLooper());
    private static final int LOCATION_TIMEOUT = 10000;
    private static final int LOCATION_CHECK_INTERVAL = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);

        database = ((MainActivity) requireActivity()).getDatabase();
        overpassService = new OverpassAPIService();

        setupMap();
        setupIcons();
        waitForLocationAndLoadPOIs();

        return view;
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        locationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);

        locationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isLocationReady = true;
                        GeoPoint myLocation = locationOverlay.getMyLocation();
                        if (myLocation != null) {
                            mapView.getController().setCenter(myLocation);
                            loadPOIs();
                        }
                    }
                });
            }
        });

        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);
    }

    private void setupIcons() {
        unvisitedIcon = ContextCompat.getDrawable(requireContext(), R.drawable.question);
        visitedIcon = ContextCompat.getDrawable(requireContext(), R.drawable.done);
    }

    private void waitForLocationAndLoadPOIs() {
        locationCheckHandler.postDelayed(new Runnable() {
            private int attempts = 0;
            private final int maxAttempts = LOCATION_TIMEOUT / LOCATION_CHECK_INTERVAL;

            @Override
            public void run() {
                Location location = locationOverlay.getLastFix();

                if (location != null) {
                    isLocationReady = true;
                    loadPOIs();
                } else if (attempts < maxAttempts) {
                    attempts++;
                    locationCheckHandler.postDelayed(this, LOCATION_CHECK_INTERVAL);
                } else {
                    handleLocationTimeout();
                }
            }
        }, LOCATION_CHECK_INTERVAL);
    }

    private void handleLocationTimeout() {
        Toast.makeText(getContext(),
                "Не удалось получить местоположение. Проверьте настройки GPS.",
                Toast.LENGTH_LONG).show();

        loadPOIsWithoutLocation();
    }

    private void loadPOIsWithoutLocation() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        String currentCity = mainActivity.getCurrentCity();

        if (currentCity != null && !currentCity.equals("Unknown")) {
            new Thread(() -> {
                List<POIEntity> savedPOIs = database.poiDao().getPOIsByCity(currentCity);

                requireActivity().runOnUiThread(() -> {
                    if (!savedPOIs.isEmpty()) {
                        displayPOIs(savedPOIs);
                    } else {
                        Toast.makeText(getContext(),
                                "Нет сохраненных мест для этого города",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }

    private void loadPOIs() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        String currentCity = mainActivity.getCurrentCity();

        if (currentCity != null && !currentCity.equals("Unknown")) {
            new Thread(() -> {
                List<POIEntity> savedPOIs = database.poiDao().getPOIsByCity(currentCity);

                requireActivity().runOnUiThread(() -> {
                    if (savedPOIs.isEmpty()) {
                        loadPOIsFromAPI(currentCity);
                    } else {
                        displayPOIs(savedPOIs);
                    }
                });
            }).start();
        } else {
            Toast.makeText(getContext(), "Ошибка определения города", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPOIsFromAPI(String city) {
        Location lastLocation = locationOverlay.getLastFix();

        if (lastLocation == null) {
            locationCheckHandler.postDelayed(() -> {
                Location retryLocation = locationOverlay.getLastFix();
                if (retryLocation != null) {
                    performAPIRequest(city, retryLocation);
                } else {
                    Toast.makeText(getContext(),
                            "Не удалось получить местоположение для загрузки новых мест",
                            Toast.LENGTH_LONG).show();
                }
            }, 2000);
            return;
        }

        performAPIRequest(city, lastLocation);
    }

    private void performAPIRequest(String city, Location location) {
        overpassService.getPOIsInCity(city,
                location.getLatitude(),
                location.getLongitude(),
                new OverpassAPIService.OverpassCallback() {
                    @Override
                    public void onSuccess(List<POIEntity> pois) {
                        new Thread(() -> {
                            for (POIEntity poi : pois) {
                                database.poiDao().insertPOI(poi);
                            }
                        }).start();

                        Toast.makeText(getContext(), "Места успешно загрузились ", Toast.LENGTH_SHORT).show();
                        displayPOIs(pois);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Ошибка загрузки мест: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayPOIs(List<POIEntity> pois) {
        currentPOIs = pois;

        for (POIEntity poi : pois) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(poi.latitude, poi.longitude));
            marker.setTitle(poi.name);
            marker.setSnippet(poi.description);

            marker.setIcon(poi.isVisited ? visitedIcon : unvisitedIcon);

            marker.setOnMarkerClickListener((marker1, mapView1) -> {
                if (!poi.isVisited) {
                    checkIfUserIsNearPOI(poi, marker);
                } else {
                    showPOIDetails(poi);
                }
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private void checkIfUserIsNearPOI(POIEntity poi, Marker marker) {
        Location userLocation = locationOverlay.getLastFix();

        if (userLocation == null) {
            Toast.makeText(getContext(),
                    "Не удалось определить ваше местоположение. Попробуйте позже.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        float[] distance = new float[1];
        Location.distanceBetween(
                userLocation.getLatitude(), userLocation.getLongitude(),
                poi.latitude, poi.longitude,
                distance
        );

        if (distance[0] <= 50.0f) {
            markPOIAsVisited(poi, marker);
        } else {
            Toast.makeText(getContext(),
                    "Подойдите ближе к месту (осталось " + Math.round(distance[0]) + " м)",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void markPOIAsVisited(POIEntity poi, Marker marker) {
        poi.isVisited = true;
        poi.visitTimestamp = System.currentTimeMillis();

        new Thread(() -> {
            database.poiDao().updatePOI(poi);

            VisitedLocationEntity visit = new VisitedLocationEntity();
            visit.poiId = poi.id;
            visit.visitTimestamp = poi.visitTimestamp;

            Location userLocation = locationOverlay.getLastFix();
            if (userLocation != null) {
                visit.userLatitude = userLocation.getLatitude();
                visit.userLongitude = userLocation.getLongitude();
            }

            database.visitedLocationDao().insert(visit);
        }).start();

        marker.setIcon(visitedIcon);
        mapView.invalidate();

        showPOIDiscoveredDialog(poi);
    }

    private void showPOIDiscoveredDialog(POIEntity poi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Место открыто! 🎉")
                .setMessage("Вы открыли: " + poi.name)
                .setPositiveButton("Отлично!", null)
                .show();
    }

    private void showPOIDetails(POIEntity poi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(poi.name)
                .setMessage(poi.description)
                .setPositiveButton("Закрыть", null)
                .show();
    }

    @Override
    public void onWeatherUpdated() {
        if (isLocationReady) {
            loadPOIs();
        } else {
            waitForLocationAndLoadPOIs();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCheckHandler != null) {
            locationCheckHandler.removeCallbacksAndMessages(null);
        }
    }
}