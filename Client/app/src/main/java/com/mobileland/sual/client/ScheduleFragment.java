package com.mobileland.sual.client;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {
        // Required empty public constructor
    }

    private List<String> loadScheduleFromCSV(Context context) {
        List<String> scheduleList = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open("school_schedule.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                scheduleList.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scheduleList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.scheduleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> data = loadScheduleFromCSV(getContext());
        ScheduleAdapter adapter = new ScheduleAdapter(data);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
