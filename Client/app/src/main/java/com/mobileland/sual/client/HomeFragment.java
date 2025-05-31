package com.mobileland.sual.client;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        MaterialButton scholarshipButton = view.findViewById(R.id.scholarshipButton);

        scholarshipButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScholarshipActivity.class);
            startActivity(intent);
        });

        return view;
    }
}