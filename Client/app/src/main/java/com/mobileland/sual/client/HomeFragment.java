package com.mobileland.sual.client;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.*;
import android.content.res.Configuration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class HomeFragment extends Fragment {

    private final Calendar calendar = Calendar.getInstance();

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
                chip.setClickable(true);
                chip.setCheckable(false);
                chip.setChipStrokeWidth(0f);
                chip.setChipStrokeColorResource(android.R.color.transparent);
                chip.setOnClickListener(v -> showAlertDialog(event));
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

    private void showAlertDialog(String eventName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("알림 설정")
                .setMessage(eventName + " 일정 알림을 언제 보내 드릴까요?")
                .setNegativeButton("+ 날짜/시간 설정", (dialog, which) -> showDatePickerDialog(eventName))
                .setNeutralButton("취소", null)
                .show();
    }

    private void showDatePickerDialog(String eventName) {
        DatePickerDialog dateDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> showTimePickerDialog(year, month, dayOfMonth, eventName),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dateDialog.show();
    }

    private void showTimePickerDialog(int year, int month, int day, String eventName) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (timePicker, hour, minute) -> {
                    Calendar alarmCal = Calendar.getInstance();
                    alarmCal.set(year, month, day, hour, minute);
                    String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(alarmCal.getTime());
                    registerNotification(eventName, formatted);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void registerNotification(String title, String datetime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = sdf.parse(datetime);
            long timeInMillis = date.getTime();

            Intent intent = new Intent(requireContext(), NotificationReceiver.class);
            intent.putExtra("notification_title", "📌 일정 알림: " + title);
            intent.putExtra("notification_content", title + " 일정이 도착했습니다!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    (int) timeInMillis, // 고유 요청코드
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }

            Toast.makeText(requireContext(), "✅ 알림이 예약되었습니다", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "❌ 날짜 형식 오류", Toast.LENGTH_SHORT).show();
        }

        Log.d("알림등록", title + " → " + datetime + "에 알림 예약됨");

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // 한국어 Locale 강제 설정
        Locale locale = Locale.KOREA;
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    private String getCurrentMealType() {
        String currentTime = "";
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            currentTime = "breakfast";
        }
        else if (hour < 15) {
            currentTime = "lunch_special";
        }
        else {
            currentTime = "dinner";
        }

        return currentTime;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        ChipGroup chipGroup = view.findViewById(R.id.scheduleChipGroup);
        TextView todayDateText = view.findViewById(R.id.todayDateText);

        Map<String, List<String>> scheduleMap = readScheduleData(getContext());

        // ✅ 오늘 날짜 관련 설정
        Calendar todayCal = Calendar.getInstance();
        String todayDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH) + 1, todayCal.get(Calendar.DAY_OF_MONTH));

        // ✅ 위쪽 날짜 텍스트뷰에 오늘 날짜 표시
        String dayOfWeek = getDayOfWeekKor(
                todayCal.get(Calendar.YEAR),
                todayCal.get(Calendar.MONTH),
                todayCal.get(Calendar.DAY_OF_MONTH)
        );
        String todayString = String.format(Locale.getDefault(), "%04d년 %d월 %d일 (%s)",
                todayCal.get(Calendar.YEAR),
                todayCal.get(Calendar.MONTH) + 1,
                todayCal.get(Calendar.DAY_OF_MONTH),
                dayOfWeek
        );
        todayDateText.setText(todayString);

        // 달력에서 오늘 날짜 선택 상태로 보이게
        calendarView.setDate(todayCal.getTimeInMillis(), true, true);

        // 칩 업데이트
        updateChipsForDate(todayDate, chipGroup, scheduleMap);

        // 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            updateChipsForDate(selectedDate, chipGroup, scheduleMap);
            todayDateText.setText(String.format(Locale.getDefault(), "%04d년 %d월 %d일 (%s)",
                    year, month + 1, dayOfMonth, getDayOfWeekKor(year, month, dayOfMonth)));
        });

        // 식단표 버튼 리스너
        MaterialButton mealBtn = view.findViewById(R.id.campusMealsButton);
        mealBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MealActivity.class);
            startActivity(intent);
        });

        // 오늘 메뉴 박스
        TextView mealPreviewText = view.findViewById(R.id.mealPreviewText);



        try {
            InputStream is = requireContext().getAssets().open("meal_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject root = new JSONObject(jsonBuilder.toString());
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (root.has(today)) {
                JSONObject todayMeals = root.getJSONObject(today);

                String mealType = getCurrentMealType();
                if (todayMeals.has(mealType)) {
                    JSONArray menuArray = todayMeals.getJSONObject(mealType).getJSONArray("menu");

                    String timeRange = "";
                    String mealName = "";
                    switch (mealType) {
                        case "breakfast":
                            timeRange = "08:00~09:30";
                            mealName = "조식 ";
                            break;
                        case "lunch_korean":
                            timeRange = "11:30~14:00";
                            mealName = "중식(한식) ";
                            break;
                        case "dinner":
                            timeRange = "17:30~18:30";
                            mealName = "석식 ";
                            break;
                    }
                    TextView mealInfoText = view.findViewById(R.id.mealInfoText);
                    mealInfoText.setText(mealName + timeRange);
                    StringBuilder preview = new StringBuilder();
                    for (int i = 0; i < menuArray.length(); i++) {
                        preview.append(menuArray.getString(i));
                        if (i != menuArray.length() - 1) preview.append(", ");
                    }

                    mealPreviewText.setText(preview.toString());
                } else {
                    mealPreviewText.setText("오늘 " + mealType + " 정보가 없습니다.");
                }
            } else {
                mealPreviewText.setText("오늘 학식 정보 없음");
            }

        } catch (Exception e) {
            mealPreviewText.setText("학식 로드 오류");
            Log.e("HomeFragment", "meal_preview 에러", e);
        }
        // 공지 버튼 리스너
        MaterialButton scholarBtn = view.findViewById(R.id.scholarshipButton);
        MaterialButton eventBtn = view.findViewById(R.id.eventNoticeButton);
        MaterialButton academicBtn = view.findViewById(R.id.academicNoticeButton);

        scholarBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), ScholarshipActivity.class)));
        eventBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), EventActivity.class)));
        academicBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), AcademicActivity.class)));

        return view;
    }
}

