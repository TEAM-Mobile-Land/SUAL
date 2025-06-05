package com.mobileland.sual.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MealFragment extends Fragment {

    private static final String ARG_TYPE = "meal_type";

    public static MealFragment newInstance(String mealType) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal, container, false);

        String type = getArguments() != null ? getArguments().getString(ARG_TYPE) : "unknown";

        TextView title = view.findViewById(R.id.mealTitle);
        title.setText("선택된 식사: " + type);

        // TODO: JSON 로드 후 날짜 및 식단 정보 표시

        return view;
    }
}