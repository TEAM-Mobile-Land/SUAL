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
import java.util.*;

public class MealFragment extends Fragment {

    private static final String ARG_TYPE = "meal_type";

    private String mealType;
    private TextView textView;
    private TextView priceText;
    private TextView dateText;
    private Calendar calendar = Calendar.getInstance();
    private List<String> availableDates = new ArrayList<>();
    private int currentIndex = 0;

    public static MealFragment newInstance(String mealType) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateDateView() {
        String formatted = new SimpleDateFormat("yyyy년 M월 d일 (E)", Locale.KOREA)
                .format(calendar.getTime());
        dateText.setText(formatted);
    }

    private void updateCalendarToAvailableDate() {
        try {
            String dateStr = availableDates.get(currentIndex);
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
            calendar.setTime(date);
            updateDateView();
            loadMealData(dateStr);
        } catch (Exception e) {
            Log.e("MealFragment", "날짜 이동 오류", e);
        }
    }

    private void loadMealData(String dateKey) {
        try {
            InputStream is = requireContext().getAssets().open("meal_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            is.close();

            JSONObject root = new JSONObject(jsonBuilder.toString());

            // 날짜 목록 초기화
            availableDates.clear();
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                availableDates.add(keys.next());
            }
            Collections.sort(availableDates);
            currentIndex = availableDates.indexOf(dateKey);

            if (root.has(dateKey)) {
                JSONObject todayMeals = root.getJSONObject(dateKey);
                if (todayMeals.has(mealType)) {
                    JSONObject mealObj = todayMeals.getJSONObject(mealType);
                    JSONArray menuArray = mealObj.getJSONArray("menu");

                    StringBuilder menuBuilder = new StringBuilder("\n");
                    for (int i = 0; i < menuArray.length(); i++) {
                        menuBuilder.append(menuArray.getString(i)).append("\n");
                    }

                    String menu = menuBuilder.toString();
                    String price = mealObj.optString("price");


                    // 파인하우스가 정상 영업/휴무 일 때 따라서 텍스트를 다르게
                    String menuTxt = "";
                    String priceTxt = "";

                    if(price.isEmpty()) {
                        menuTxt = menu + "일정으로 식단 정보가 없습니다\n";
                        priceTxt = "";
                    }
                    else {
                        menuTxt = menu;
                        priceTxt = price + " 원";
                    }

                    textView.setText(menuTxt);
                    priceText.setText(priceTxt);
                    return;
                }
            }

            // 가격과 메뉴 초기화
            textView.setText("");
            priceText.setText("");

        } catch (Exception e) {
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
        priceText = view.findViewById(R.id.priceText);
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

        // 식사 종류 한글로 변환
        TextView title = view.findViewById(R.id.mealTitle);
        String displayName;
        switch (mealType) {
            case "breakfast":
                title.setText("조식\n(08:00~09:30)");
                break;
            case "lunch_korean":
                title.setText("중식(한식)\n(11:30~14:00)");
                break;
            case "lunch_special":
                title.setText("중식(특식)\n(11:30~14:00)");
                break;
            case "dinner":
                title.setText("석식\n(17:30~18:30)");
                break;
            default:
                title.setText("식사 정보");
                break;
        }
        // 초기 로딩
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        updateDateView();
        loadMealData(todayKey);

        return view;
    }
}