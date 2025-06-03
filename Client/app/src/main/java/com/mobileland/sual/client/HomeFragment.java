package com.mobileland.sual.client;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.*;

import java.io.*;
import java.util.*;

public class HomeFragment extends Fragment {

    private Map<String, List<String>> readScheduleData(Context context) {
        Map<String, List<String>> scheduleMap = new HashMap<>();

        try {
            InputStream is = context.getAssets().open("school_schedule.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String datePart = parts[0].trim();
                    String event = parts[1].trim();

                    if (datePart.contains("~")) {
                        String[] dateRange = datePart.split("~");
                        String startDateStr = dateRange[0].trim();
                        String endDateStr = dateRange[1].trim();

                        Calendar start = parseDate(startDateStr);
                        Calendar end = parseDate(endDateStr);

                        while (!start.after(end)) {
                            String currentDateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                    start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH));

                            scheduleMap.computeIfAbsent(currentDateStr, k -> new ArrayList<>()).add(event);
                            start.add(Calendar.DAY_OF_MONTH, 1);
                        }
                    } else {
                        scheduleMap.computeIfAbsent(datePart, k -> new ArrayList<>()).add(event);
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scheduleMap;
    }

    private Calendar parseDate(String dateStr) {
        String[] parts = dateStr.split("-");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
        return cal;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        Map<String, List<String>> scheduleMap = readScheduleData(getContext());

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                ChipGroup chipGroup = getView().findViewById(R.id.scheduleChipGroup);
                chipGroup.removeAllViews();

                List<String> events = scheduleMap.get(selectedDate);
                if (events != null) {
                    for (String event : events) {
                        Chip chip = new Chip(getContext());
                        chip.setText(event);
                        chip.setChipBackgroundColorResource(R.color.primary500); // 원하는 색상
                        chip.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                        chip.setClickable(false);
                        chip.setCheckable(false);
                        chipGroup.addView(chip);
                    }
                } else {
                    Chip noEventChip = new Chip(getContext());
                    noEventChip.setText("일정 없음");
                    noEventChip.setChipBackgroundColorResource(R.color.gray600);
                    noEventChip.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                    noEventChip.setClickable(false);
                    noEventChip.setCheckable(false);
                    chipGroup.addView(noEventChip);
                }
            }
        });

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