package com.example.Explorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProfileFragment extends Fragment implements MainActivity.WeatherUpdateListener {
    private TextView temperatureTextView;
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

        temperatureTextView = view.findViewById(R.id.weather_0);
        btnGo = view.findViewById(R.id.btn_go);

        // Get initial weather value
        if (getActivity() instanceof MainActivity) {
            String weather = ((MainActivity) getActivity()).getWeather();
            if (weather != null) {
                temperatureTextView.setText(weather);
            }
        }

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