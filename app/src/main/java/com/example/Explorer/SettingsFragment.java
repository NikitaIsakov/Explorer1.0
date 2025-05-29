package com.example.Explorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        super.onCreate(savedInstanceState);

        TextView temperatureTextView = view.findViewById(R.id.weather_1);
        String weather = ((MainActivity) getActivity()).getWeather();
        temperatureTextView.setText(weather);

        return view;
    }
}