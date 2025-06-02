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
        MaterialButton scholarBtn = view.findViewById(R.id.scholarshipButton);
        MaterialButton eventBtn = view.findViewById(R.id.eventNoticeButton);
        MaterialButton academicBtn = view.findViewById(R.id.academicNoticeButton);

        scholarBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ScholarshipActivity.class);
            startActivity(intent);
        });

        eventBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EventActivity.class);
            startActivity(intent);
        });

        academicBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AcademicActivity.class);
            startActivity(intent);
        });




        return view;
    }
}