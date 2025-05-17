package com.example.myapplication3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button btnGo = view.findViewById(R.id.btn_go);
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
}