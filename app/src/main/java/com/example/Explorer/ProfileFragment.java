package com.example.Explorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.UserEntity;

public class ProfileFragment extends Fragment implements MainActivity.WeatherUpdateListener {
    private TextView temperatureTextView;
    private TextView greetingTextView;
    private AppDatabase database;
    private Button btnGo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getWeather();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        database = AppDatabase.getDatabase(requireContext());
        temperatureTextView = view.findViewById(R.id.weather_0);
        greetingTextView = view.findViewById(R.id.greeting);
        btnGo = view.findViewById(R.id.btn_go);

        if (getActivity() instanceof MainActivity) {
            String weather = ((MainActivity) getActivity()).getWeather();
            if (weather != null) {
                temperatureTextView.setText(weather);
            }
        }

        loadUsername();

        btnGo.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.main_layout_fragment, new MapFragment());
            transaction.addToBackStack(null);
            transaction.commit();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectBottomNavItem(R.id.navigation_map);
            }
        });

        return view;
    }

    private void loadUsername() {
        new Thread(() -> {
            UserEntity user = database.userDao().getUser();
            if (user != null && greetingTextView != null) {
                String greeting = "Привет, " + user.username + "!\nПродолжи исследовать город и открывать карту";
                greetingTextView.post(() -> greetingTextView.setText(greeting));
            }
        }).start();
    }

    @Override
    public void onWeatherUpdated() {
        if (getActivity() != null) {
            String weather = ((MainActivity) getActivity()).getWeather();
            if (weather != null && temperatureTextView != null) {
                temperatureTextView.post(() -> temperatureTextView.setText(weather));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getWeather();
        }
    }
}