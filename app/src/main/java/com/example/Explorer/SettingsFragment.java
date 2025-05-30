package com.example.Explorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.UserEntity;

import java.util.Objects;

public class SettingsFragment extends Fragment {
    private EditText usernameEditText;
    private AppDatabase database;
    UserEntity user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        database = AppDatabase.getDatabase(requireContext());
        usernameEditText = view.findViewById(R.id.editTextText);

        TextView temperatureTextView = view.findViewById(R.id.weather_1);

        if (getActivity() instanceof MainActivity) {
            String weather = ((MainActivity) getActivity()).getWeather();
            if (weather != null) {
                temperatureTextView.setText(weather);
            }
        }

        loadUsername();

        return view;
    }

    private void loadUsername() {
        new Thread(() -> {
            user = database.userDao().getUser();
            if (user != null && usernameEditText != null) {
                usernameEditText.post(() -> usernameEditText.setText(user.username));
            } else {
                user = new UserEntity();
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveUsername();
    }

    private void saveUsername() {
        String username = usernameEditText.getText().toString().trim();
        if (!username.isEmpty() && !Objects.equals(user.username, username)) {
            new Thread(() -> {
                UserEntity user = new UserEntity();
                user.username = username;
                user.lastUpdated = System.currentTimeMillis();
                database.userDao().insert(user);
            }).start();
        }
    }
}