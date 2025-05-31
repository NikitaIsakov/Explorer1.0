package com.example.Explorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.POIEntity;
import com.squareup.picasso.Picasso;

public class StarFragment extends Fragment {

    private TextView visitedPOIsText;
    private TextView stepsText;
    private TextView lastVisitedText;
    private ImageView lastVisitedImage;

    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_star, container, false);

        database = ((MainActivity) requireActivity()).getDatabase();

        initViews(view);
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        visitedPOIsText = view.findViewById(R.id.visitedPOIsText);
        lastVisitedText = view.findViewById(R.id.lastVisitedText);
        lastVisitedImage = view.findViewById(R.id.lastVisitedImage);
    }

    private void loadStatistics() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        String currentCity = mainActivity.getCurrentCity();

        new Thread(() -> {
            // Загружаем статистику из базы данных
            int totalPOIs = database.poiDao().getTotalPOIsInCity(currentCity);
            int visitedPOIs = database.poiDao().getVisitedPOIsInCity(currentCity);
            POIEntity lastVisited = database.poiDao().getLastVisitedPOI();

            // Загружаем количество шагов (здесь нужно интегрировать шагомер)
            int steps = getStepsCount();

            requireActivity().runOnUiThread(() -> {
                visitedPOIsText.setText("Открыто мест: " + visitedPOIs + "/" + totalPOIs);

                if (lastVisited != null) {
                    lastVisitedText.setText("Последнее место: " + lastVisited.name);
                    loadPOIImage(lastVisited);
                } else {
                    lastVisitedText.setText("Места еще не открыты");
                }
            });
        }).start();
    }

    private void loadPOIImage(POIEntity poi) {
        // Здесь можно использовать Picasso/Glide для загрузки изображений
        // Или интегрировать с Wikipedia API для получения фото
        if (poi.imageUrl != null && !poi.imageUrl.isEmpty()) {
            Picasso.get()
                    .load(poi.imageUrl)
                    .placeholder(R.drawable.img)
                    .into(lastVisitedImage);
        }
    }

    private int getStepsCount() {
        // Интеграция с шагомером (Google Fit API или встроенные сенсоры)
        // Пока возвращаем заглушку
        return 1000;
    }
}
