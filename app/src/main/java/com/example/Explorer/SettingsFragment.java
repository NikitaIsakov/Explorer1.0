package com.example.Explorer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.Explorer.database.AppDatabase;
import com.example.Explorer.database.UserEntity;

public class SettingsFragment extends Fragment {
    private EditText usernameEditText;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        database = AppDatabase.getDatabase(requireContext());
        usernameEditText = view.findViewById(R.id.editTextText);

        loadUsername();

        return view;
    }

    private void loadUsername() {
        new Thread(() -> {
            UserEntity user = database.userDao().getUser();
            if (user != null && usernameEditText != null) {
                usernameEditText.post(() -> usernameEditText.setText(user.username));
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
        if (!username.isEmpty()) {
            new Thread(() -> {
                UserEntity user = new UserEntity();
                user.username = username;
                user.lastUpdated = System.currentTimeMillis();
                database.userDao().insert(user);
            }).start();
        }
    }
}