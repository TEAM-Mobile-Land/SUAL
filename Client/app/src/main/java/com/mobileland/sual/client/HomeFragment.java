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

    // 요일 반환 메서드
    private String getDayOfWeekKor(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        return days[cal.get(Calendar.DAY_OF_WEEK) - 1];
    }

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
                        Calendar start = parseDate(dateRange[0].trim());
                        Calendar end = parseDate(dateRange[1].trim());
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

    private void updateChipsForDate(String selectedDate, ChipGroup chipGroup, Map<String, List<String>> scheduleMap) {
        chipGroup.removeAllViews();
        List<String> events = scheduleMap.get(selectedDate);
        if (events != null) {
            for (String event : events) {
                Chip chip = new Chip(getContext());
                chip.setText(event);
                chip.setChipBackgroundColorResource(R.color.primary500);
                chip.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                chip.setClickable(false);
                chip.setCheckable(false);
                chip.setChipStrokeWidth(0f);
                chip.setChipStrokeColorResource(android.R.color.transparent);
                chip.post(() -> chip.setShapeAppearanceModel(
                        chip.getShapeAppearanceModel().toBuilder()
                                .setAllCornerSizes(chip.getHeight() / 2f)
                                .build()));
                chipGroup.addView(chip);
            }
        } else {
            Chip noEventChip = new Chip(getContext());
            noEventChip.setText("일정 없음");
            noEventChip.setChipBackgroundColorResource(R.color.gray600);
            noEventChip.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            noEventChip.setClickable(false);
            noEventChip.setCheckable(false);
            noEventChip.setChipStrokeWidth(0f);
            noEventChip.setChipStrokeColorResource(android.R.color.transparent);
            noEventChip.post(() -> noEventChip.setShapeAppearanceModel(
                    noEventChip.getShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(noEventChip.getHeight() / 2f)
                            .build()));
            chipGroup.addView(noEventChip);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        ChipGroup chipGroup = view.findViewById(R.id.scheduleChipGroup);
        Map<String, List<String>> scheduleMap = readScheduleData(getContext());


        Calendar todayCal = Calendar.getInstance();
        String todayDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH) + 1, todayCal.get(Calendar.DAY_OF_MONTH));
        updateChipsForDate(todayDate, chipGroup, scheduleMap);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            updateChipsForDate(selectedDate, chipGroup, scheduleMap);

            // 홈 화면 상단에 날짜 띄우기
            TextView todayDateText = getView().findViewById(R.id.todayDateText);
            todayDateText.setText(String.format(Locale.getDefault(), "%04d년 %d월 %d일 (%s)",
                    year, month + 1, dayOfMonth, getDayOfWeekKor(year, month, dayOfMonth)));

        });

        MaterialButton scholarBtn = view.findViewById(R.id.scholarshipButton);
        MaterialButton eventBtn = view.findViewById(R.id.eventNoticeButton);
        MaterialButton academicBtn = view.findViewById(R.id.academicNoticeButton);

        scholarBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), ScholarshipActivity.class)));
        eventBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), EventActivity.class)));
        academicBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), AcademicActivity.class)));

        return view;
    }
}