package com.mobileland.sual.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MealFragment extends Fragment {
    private String mealType;
    private TextView textView;
    private TextView dateText;
    private Calendar calendar = Calendar.getInstance();

    private List<String> availableDates = new ArrayList<>();
    private int currentIndex = 0;
    private static final String ARG_TYPE = "meal_type";

    public static MealFragment newInstance(String mealType) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateDateView() {
        String formatted = new SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA).format(calendar.getTime());
        dateText.setText(formatted + " 11:30~14:00");
    }

    private void updateCalendarToAvailableDate() {
        try {
            String selectedDate = availableDates.get(currentIndex);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(selectedDate);
            calendar.setTime(date);
            updateDateView();
            loadMealData();
        } catch (Exception e) {
            Log.e("MealFragment", "날짜 이동 오류", e);
        }
    }

    private void loadMealData() {
        try {
            InputStream is = requireContext().getAssets().open("meal_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONObject root = new JSONObject(jsonBuilder.toString());

            // JSON 키 목록 저장
            availableDates.clear();
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                availableDates.add(keys.next());
            }
            Collections.sort(availableDates);

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            currentIndex = availableDates.indexOf(today);

            if (root.has(today)) {
                JSONObject todayMeals = root.getJSONObject(today);
                if (todayMeals.has(mealType)) {
                    JSONObject mealObj = todayMeals.getJSONObject(mealType);
                    JSONArray menuArray = mealObj.getJSONArray("menu");
                    StringBuilder menuBuilder = new StringBuilder();
                    for (int i = 0; i < menuArray.length(); i++) {
                        menuBuilder.append("• ").append(menuArray.getString(i)).append("\n");
                    }
                    String menu = menuBuilder.toString();
                    String price = mealObj.optString("price", "-");

                    textView.setText("\uD83D\uDCA1 메뉴\n\n" + menu + "\n\n\uD83D\uDCB0 가격: " + price + "원");
                    return;
                }
            }

            textView.setText("\uD83D\uDE25 오늘은 해당 식사 정보가 없습니다.");

        } catch (Exception e) {
            textView.setText("\u26A0\uFE0F 오류 발생: " + e.getMessage());
            Log.e("MealFragment", "식단 JSON 파싱 오류", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal, container, false);

        mealType = getArguments() != null ? getArguments().getString(ARG_TYPE) : "unknown";
        textView = view.findViewById(R.id.mealText);
        dateText = view.findViewById(R.id.mealDate);

        ImageButton leftBtn = view.findViewById(R.id.leftArrow);
        ImageButton rightBtn = view.findViewById(R.id.rightArrow);

        leftBtn.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
                updateCalendarToAvailableDate();
            }
        });

        rightBtn.setOnClickListener(v -> {
            if (currentIndex < availableDates.size() - 1) {
                currentIndex++;
                updateCalendarToAvailableDate();
            }
        });

        TextView title = view.findViewById(R.id.mealTitle);
        String displayName;

        switch (mealType) {
            case "breakfast":
                displayName = "조식";
                break;
            case "lunch_korean":
                displayName = "중식(한식)";
                break;
            case "lunch_special":
                displayName = "중식(특식)";
                break;
            case "dinner":
                displayName = "석식";
                break;
            default:
                displayName = "식사 정보";
        }

        title.setText(displayName);

        updateDateView();
        loadMealData();

        return view;
    }
}